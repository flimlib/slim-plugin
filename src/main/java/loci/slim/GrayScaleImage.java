/*
 * #%L
 * SLIM Curve plugin for combined spectral-lifetime image analysis.
 * %%
 * Copyright (C) 2010 - 2015 Board of Regents of the University of
 * Wisconsin-Madison.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package loci.slim;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.ImageCanvas;
import ij.gui.ImageRoi;
import ij.gui.Overlay;
import ij.gui.Roi;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.Preferences;

import loci.slim.mask.IMaskGroup;
import loci.slim.mask.IMaskNode;
import loci.slim.mask.IMaskNodeListener;
import loci.slim.mask.Mask;
import loci.slim.mask.MaskNode;
import net.imagej.ImgPlus;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;

/**
 * The GrayScaleImage shows a grayscale representation of the input data. It
 * also allows the user to look at all the channels and pick a channel for the
 * fit.
 *
 * @author Aivar Grislis
 */
public class GrayScaleImage<T extends RealType<T>> implements IGrayScaleImage {

	private static final String ZOOM_KEY = "zoom";
	private static final int CURSOR_WIDTH = 11;
	private static final int CURSOR_HEIGHT = 11;
	private static final Color CURSOR_COLOR = Color.WHITE;
	private static final Color THRESHOLD_COLOR = Color.RED;
	private static final Color HIDDEN_COLOR = Color.BLUE;
	private static final Color ERROR_COLOR = Color.GREEN.brighter();
	private static final int TRANSPARENT = 0x00;
	private static final int THRESHOLD_TRANSPARENCY = 0xa0;
	private static final int HIDDEN_TRANSPARENCY = 0xa0;
	private static final int ERROR_TRANSPARENCY = 0xff;
	private final int _width;
	private final int _height;
	private final ImagePlus _imagePlus;
	private final ImageStack _imageStack;
	private MyStackWindow _stackWindow;
	private ISelectListener _listener;
	private final short[][] _saveOutPixels;
	private double _minNonZeroPhotonCount;
	private double _maxTotalPhotons;
	private int[] _brightestPoint;
	private BufferedImage _cursorImage;
	private BufferedImage _errorImage;
	private BufferedImage _hiddenImage;
	private BufferedImage _thresholdImage;
	private Roi _cursorRoi;
	private Roi _errorRoi;
	private Roi _hiddenRoi;
	private Roi _thresholdRoi;
	private final Overlay _overlay;
	private final Set<IMaskGroup> _maskGroupSet;

	public GrayScaleImage(final ImgPlus<T> image) {
		String title = image.getName();
		final int spaceIndex = title.indexOf(" ");
		if (0 < spaceIndex) {
			title = title.substring(0, spaceIndex);
		}

		final int numDimensions = image.numDimensions();
		final long dimensions[] = new long[numDimensions];
		image.dimensions(dimensions);
		_width = (int) dimensions[0];
		_height = (int) dimensions[1];
		final int bins = (int) dimensions[2];
		int channels = 1;
		if (numDimensions > 3) {
			channels = (int) dimensions[3];
		}

		// building an image stack
		_imageStack = new ImageStack(_width, _height);
		_saveOutPixels = new short[channels][];

		final RandomAccess cursor = image.randomAccess();
		final double[][] pixels = new double[_width][_height];
		final int[] position = new int[numDimensions];

		// keep track of minimum count; usually 1.0 but can be 10.0, etc.
		_minNonZeroPhotonCount = Double.MAX_VALUE;
		for (int c = 0; c < channels; ++c) {
			if (numDimensions > 3) {
				position[3] = c;
			}
			final short[] outPixels = new short[_width * _height];

			// sum photon counts
			double maxPixel = 0.0;
			for (int x = 0; x < _width; ++x) {
				position[0] = x;
				for (int y = 0; y < _height; ++y) {
					position[1] = y;
					pixels[x][y] = 0.0;
					for (int b = 0; b < bins; ++b) {
						position[2] = b;

						cursor.setPosition(position);
						final double photonCount =
							((ComplexType) cursor.get()).getRealDouble();
						pixels[x][y] += photonCount;

						// keep track of minimum
						if (0.0 < photonCount && photonCount < _minNonZeroPhotonCount) {
							_minNonZeroPhotonCount = photonCount;
						}
					}
					// keep track of maximum value and its coordinates
					if (pixels[x][y] > maxPixel) {
						maxPixel = pixels[x][y];
						if (maxPixel > _maxTotalPhotons) {
							_maxTotalPhotons = maxPixel;
						}
						_brightestPoint = new int[] { x, y };
					}
				}
			}

			// convert to short
			for (int x = 0; x < _width; ++x) {
				for (int y = 0; y < _height; ++y) {
					int value = (int) (pixels[x][y] / _minNonZeroPhotonCount);
					if (value > Short.MAX_VALUE) {
						value = Short.MAX_VALUE;
					}
					outPixels[y * _width + x] = (short) value;
				}
			}
			// add a slice
			_imageStack.addSlice("" + c, outPixels);
			_saveOutPixels[c] = outPixels;
		}
		_imagePlus = new ImagePlus(title, _imageStack);
		_stackWindow = new MyStackWindow(_imagePlus);
		_stackWindow.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(final WindowEvent e) {
				final float zoomFactor = _stackWindow.getZoomFactor();
				saveZoomFactorInPreferences(zoomFactor);
			}
		});
		final float zoomFactor = getZoomFactorFromPreferences();
		while (zoomFactor < _stackWindow.getZoomFactor()) {
			IJ.run("In");
		}
		_stackWindow.setVisible(true);

		// IJ.log("minNonZeroPhotonCount is " + _minNonZeroPhotonCount);

		// IJ.log("Channel selector " + _stackWindow.getChannelSelector());
		// IJ.log("Slice selector " + _stackWindow.getSliceSelector());
		// IJ.log("Frame selector " + _stackWindow.getFrameSelector());

		// hook up mouse listener
		final ImageCanvas canvas = _stackWindow.getCanvas();
		canvas.addMouseListener(new MouseListener() {

			@Override
			public void mousePressed(final MouseEvent e) {}

			@Override
			public void mouseExited(final MouseEvent e) {}

			@Override
			public void mouseClicked(final MouseEvent e) {}

			@Override
			public void mouseEntered(final MouseEvent e) {}

			@Override
			public void mouseReleased(final MouseEvent e) {
				// note if you are in zoom tool mode and this click is
				// also zooming you in, x and y will be wrong
				if (null != _listener) {
					final float zoomFactor = _stackWindow.getZoomFactor();
					final int x = (int) (e.getX() * zoomFactor);
					final int y = (int) (e.getY() * zoomFactor);
					_listener.selected(getChannel(), x, y);
				}
			}
		});

		_overlay = createOverlay(_imagePlus);
		_imagePlus.setOverlay(_overlay);

		final int threshold = estimateThreshold();
		updateThreshold(threshold);

		_maskGroupSet = new HashSet<IMaskGroup>();
	}

	@Override
	public void close() {
		// run once
		if (null != _stackWindow) {
			final float zoomFactor = _stackWindow.getZoomFactor();
			saveZoomFactorInPreferences(zoomFactor);
			_stackWindow.close();
			_stackWindow = null;
		}
	}

	@Override
	public void hideCursor() {
		_cursorRoi.setLocation(-CURSOR_WIDTH, -CURSOR_HEIGHT);
		_imagePlus.draw();
	}

	@Override
	public void showCursor(int x, int y) {
		x -= CURSOR_WIDTH / 2;
		y -= CURSOR_HEIGHT / 2;
		_cursorRoi.setLocation(x, y);
		_imagePlus.draw();
	}

	private Overlay createOverlay(final ImagePlus imagePlus) {
		final int width = imagePlus.getWidth();
		final int height = imagePlus.getHeight();

		_cursorImage = createCursorImage();
		_errorImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		_hiddenImage =
			new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		_thresholdImage =
			new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		final Overlay overlay = new Overlay();
		_errorRoi = new ImageRoi(0, 0, _errorImage);
		overlay.add(_errorRoi);
		_thresholdRoi = new ImageRoi(0, 0, _thresholdImage);
		overlay.add(_thresholdRoi);
		_hiddenRoi = new ImageRoi(0, 0, _hiddenImage);
		overlay.add(_hiddenRoi);
		_cursorRoi = new ImageRoi(0, 0, _cursorImage);
		overlay.add(_cursorRoi);

		return overlay;
	}

	private BufferedImage createCursorImage() {
		final int color = getColor(CURSOR_COLOR, 0xff);
		final int black = getColor(Color.GRAY, 0xff);
		final BufferedImage cursorImage =
			new BufferedImage(CURSOR_WIDTH, CURSOR_HEIGHT,
				BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < CURSOR_HEIGHT; ++y) {
			for (int x = 0; x < CURSOR_WIDTH; ++x) {
				if (x == CURSOR_WIDTH / 2 - 1 || x == CURSOR_WIDTH / 2 + 1) {
					if (y <= CURSOR_HEIGHT / 2 - 1 || y >= CURSOR_HEIGHT / 2 + 1) {
						cursorImage.setRGB(x, y, black);
					}
					else if (y == CURSOR_HEIGHT / 2) {
						cursorImage.setRGB(x, y, color);
					}
				}
				else if (x == CURSOR_WIDTH / 2) {
					if (y != CURSOR_HEIGHT / 2) {
						cursorImage.setRGB(x, y, color);
					}
				}
				else if (y == CURSOR_HEIGHT / 2 - 1) {
					cursorImage.setRGB(x, y, black);
				}
				else if (y == CURSOR_HEIGHT / 2) {
					cursorImage.setRGB(x, y, color);
				}
				else if (y == CURSOR_HEIGHT / 2 + 1) {
					cursorImage.setRGB(x, y, black);
				}
			}
		}
		return cursorImage;
	}

	private int getColor(final Color color, final int alpha) {
		final int red = color.getRed();
		final int green = color.getGreen();
		final int blue = color.getBlue();
		return (alpha << 24) | (red << 16) | (green << 8) | blue;
	}

	/**
	 * Sets a listener for when the user clicks on the image.
	 *
	 * @param listener
	 */
	@Override
	public void setListener(final ISelectListener listener) {
		_listener = listener;
	}

	/**
	 * Gets the channel slider selection.
	 *
	 * @return channel
	 */
	@Override
	public int getChannel() {
		// covert 1...n to 0...n-1
		return _stackWindow.getSlice() - 1;
	}

	/**
	 * Disables and enables channel selection, during and after a fit.
	 *
	 * @param enable
	 */
	@Override
	public void enable(final boolean enable) {
		_stackWindow.setEnabled(enable);
	}

	/**
	 * Gets a grayscale pixel value, to test against a threshold.
	 *
	 * @param channel
	 * @param x
	 * @param y
	 * @return unsigned byte expressed as an integer, 0...255
	 */
	@Override
	public int getGrayValue(final int channel, final int x, final int y) {
		// (a * 255 / b) is 255 only when a == b, so we need to multiply by 256
		int returnValue =
			_saveOutPixels[channel][y * _width + x] * 256 /
				(int) getMaxTotalPhotons();
		if (returnValue > 255) {
			returnValue = 255;
		}
		return returnValue &= 0xff;
	}

	@Override
	public double getMinNonZeroPhotonCount() {
		return _minNonZeroPhotonCount;
	}

	@Override
	public double getMaxTotalPhotons() {
		return _maxTotalPhotons / _minNonZeroPhotonCount;
	}

	@Override
	public int[] getBrightestPoint() {
		return _brightestPoint;
	}

	@Override
	public int estimateThreshold() {
		return _imagePlus.getProcessor().getAutoThreshold();
	}

	@Override
	public void updateThreshold(final int threshold) {
		for (int y = 0; y < _height; ++y) {
			for (int x = 0; x < _width; ++x) {
				int alpha = TRANSPARENT;
				if (_saveOutPixels[getChannel()][y * _width + x] < threshold) {
					alpha = THRESHOLD_TRANSPARENCY;
				}
				_thresholdImage.setRGB(x, y, getColor(THRESHOLD_COLOR, alpha));
			}
		}
		_imagePlus.draw();
	}

	@Override
	public void resetErrorMask(final int channel) {
		updateErrorMask(new Mask(_width, _height), channel);
	}

	@Override
	public void updateErrorMask(final Mask mask, final int channel) {
		for (int y = 0; y < _height; ++y) {
			for (int x = 0; x < _width; ++x) {
				int alpha = TRANSPARENT;
				if (mask.test(x, y)) {
					alpha = ERROR_TRANSPARENCY;
				}
				_errorImage.setRGB(x, y, getColor(ERROR_COLOR, alpha));
			}
		}
		_imagePlus.draw();
	}

	@Override
	public void listenToMaskGroup(final IMaskGroup maskGroup) {
		// apply the group mask
		applyMask(maskGroup.getMask());

		// each mask group should have a listener
		if (!_maskGroupSet.contains(maskGroup)) {
			_maskGroupSet.add(maskGroup);

			// create a new mask node that listens to the group
			final IMaskNode maskNode =
				new MaskNode(maskGroup, new IMaskNodeListener() {

					// listen for mask changes
					@Override
					public void updateMasks(final Mask otherMask, final Mask totalMask) {
						applyMask(otherMask);
					}
				});
		}
	}

	private void applyMask(final Mask mask) {
		for (int y = 0; y < _height; ++y) {
			for (int x = 0; x < _width; ++x) {
				int alpha = HIDDEN_TRANSPARENCY;
				// a null mask is the same as all bits set
				if (null == mask || mask.test(x, y)) {
					alpha = TRANSPARENT;
				}
				_hiddenImage.setRGB(x, y, getColor(HIDDEN_COLOR, alpha));
			}
		}
		_imagePlus.draw();
	}

	/**
	 * Restores zoom factor from Java Preferences.
	 *
	 * @return String with path name
	 */
	private float getZoomFactorFromPreferences() {
		final Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		return prefs.getFloat(ZOOM_KEY, 1.0f);
	}

	/**
	 * Saves the zoom factor to Java Preferences.
	 *
	 * @param path
	 */
	private void saveZoomFactorInPreferences(final float zoomFactor) {
		final Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		prefs.putFloat(ZOOM_KEY, zoomFactor);
	}
}
