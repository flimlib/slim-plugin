//
// GrayScaleImage.java
//

/*
SLIMPlugin for combined spectral-lifetime image analysis.

Copyright (c) 2010, UW-Madison LOCI
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the UW-Madison LOCI nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package loci.slim;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.ImageCanvas;
import ij.gui.ImageRoi;
import ij.gui.Roi;
import ij.gui.Overlay;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.Set;

import loci.slim.mask.IMaskGroup;
import loci.slim.mask.IMaskNode;
import loci.slim.mask.IMaskNodeListener;

import loci.slim.mask.Mask;
import loci.slim.mask.MaskNode;

import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.ComplexType;
import mpicbg.imglib.type.numeric.RealType;

/**
 * The GrayScaleImage shows a grayscale representation of the input data.  It
 * also allows the user to look at all the channels and pick a channel for the
 * fit.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/software/browser/trunk/projects/slim-plugin/src/main/java/loci/slim/GrayScaleImage.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/software/trunk/projects/slim-plugin/src/main/java/loci/slim/GrayScaleImage.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis grislis at wisc dot edu
 */
public class GrayScaleImage<T extends RealType<T>> implements IGrayScaleImage {
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
	//TODO ARG 10/3/12 was formerly using a ByteProcessor, switch to ShortProcessor; _8bit switch temporary for backward compatibility
	private boolean _8bit = false;
    private int _width;
    private int _height;
	private ImagePlus _imagePlus;
    private ImageStack _imageStack;
    private MyStackWindow _stackWindow;
    private ISelectListener _listener;
    private byte[][] _saveOutPixels;
	private short[][] _saveOutPixels2;
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
	private Overlay _overlay;
	private Set<IMaskGroup> _maskGroupSet;

    public GrayScaleImage(Image<T> image) {
        String title = image.getName();
        int spaceIndex = title.indexOf(" ");
        if (0 < spaceIndex) {
            title = title.substring(0, spaceIndex);
        }
        int dimensions[] = image.getDimensions();
        _width = dimensions[0];
        _height = dimensions[1];
        int bins = dimensions[2];
        int channels = 1;
        if (dimensions.length > 3) {
            channels = dimensions[3];
        }

        // building an image stack
        _imageStack = new ImageStack(_width, _height);
		if (_8bit) {
            _saveOutPixels = new byte[channels][];
		}
		else {
            _saveOutPixels2 = new short[channels][];
        }

        LocalizableByDimCursor cursor = image.createLocalizableByDimCursor();
        double[][] pixels = new double[_width][_height];
        int[] position = (channels > 1) ? new int[4] : new int[3];

        _minNonZeroPhotonCount = Double.MAX_VALUE;
        for (int c = 0; c < channels; ++c) {
            if (channels > 1) {
                position[3] = c;
            }
            byte[] outPixels = new byte[_width * _height];
			short[] outPixels2 = new short[_width * _height];

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
                        double photonCount = ((ComplexType) cursor.getType()).getRealDouble();
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
                        _brightestPoint = new int[] { x , y };
                    }
                }
            }
		
			if (_8bit) {
				// convert to grayscale
				for (int x = 0; x < _width; ++x) {
					for (int y = 0; y < _height; ++y) {
						outPixels[y * _width + x] = (byte) (pixels[x][y] * 255 / maxPixel);
					}
				}
				// add a slice
				// _imageStack.addSlice("" + c, true, outPixels); // stopped working 12/1/10
				_imageStack.addSlice("" + c, outPixels);
				_saveOutPixels[c] = outPixels;
			}
			else {
				// convert to short
				for (int x = 0; x < _width; ++x) {
					for (int y = 0; y < _height; ++y) {
						outPixels2[y * _width + x] = (short) (pixels[x][y] / _minNonZeroPhotonCount);
					}
				}
				// add a slice
				_imageStack.addSlice("" + c, outPixels2);
				_saveOutPixels2[c] = outPixels2;
			}


        }
        _imagePlus = new ImagePlus(title, _imageStack);
        _stackWindow = new MyStackWindow(_imagePlus);
        _stackWindow.setVisible(true);
		
        //System.out.println("minNonZeroPhotonCount is " + _minNonZeroPhotonCount);

        //System.out.println("Channel selector " + _stackWindow.getChannelSelector());
        //System.out.println("Slice selector " + _stackWindow.getSliceSelector());
        //System.out.println("Frame selector " + _stackWindow.getFrameSelector());

        // hook up mouse listener
        ImageCanvas canvas = _stackWindow.getCanvas();
        canvas.addMouseListener(
            new MouseListener() {
                @Override
                public void mousePressed(MouseEvent e) {}
                @Override
                public void mouseExited(MouseEvent e) {}
                @Override
                public void mouseClicked(MouseEvent e) {}
                @Override
                public void mouseEntered(MouseEvent e) {}
                @Override
                public void mouseReleased(MouseEvent e) {
					// note if you are in zoom tool mode and this click is
					// also zooming you in, x and y will be wrong
                    if (null != _listener) {
						float zoomFactor = _stackWindow.getZoomFactor();
						int x = (int)(e.getX() * zoomFactor);
						int y = (int)(e.getY() * zoomFactor);
						_listener.selected(getChannel(), x, y);
                    }
                }
            }
        );
		
		_overlay = createOverlay(_imagePlus);
		_imagePlus.setOverlay(_overlay);
		
		int threshold = estimateThreshold();
		updateThreshold(threshold);
		
		_maskGroupSet = new HashSet<IMaskGroup>();
    }
	
	public void hideCursor() {
		_cursorRoi.setLocation(-CURSOR_WIDTH, -CURSOR_HEIGHT);
		_imagePlus.draw();
	}
	
	public void showCursor(int x, int y) {
		x -= CURSOR_WIDTH / 2;
		y -= CURSOR_HEIGHT / 2;
		_cursorRoi.setLocation(x, y);
		_imagePlus.draw();
	}
	
	private Overlay createOverlay(ImagePlus imagePlus) {
		int width = imagePlus.getWidth();
		int height = imagePlus.getHeight();
		
        _cursorImage = createCursorImage();
		_errorImage  = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		_hiddenImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		_thresholdImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		Overlay overlay = new Overlay();
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
		int color = getColor(CURSOR_COLOR, 0xff);
		int black = getColor(Color.GRAY, 0xff);
		BufferedImage cursorImage = new BufferedImage(CURSOR_WIDTH, CURSOR_HEIGHT, BufferedImage.TYPE_INT_ARGB);
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
		
	private int getColor(Color color, int alpha) {
		int red   = color.getRed();
		int green = color.getGreen();
		int blue  = color.getBlue();
		return (alpha << 24) | (red << 16) | (green << 8) | blue;
	}

    /**
     * Sets a listener for when the user clicks on the image.
     *
     * @param listener
     */
    @Override
    public void setListener(ISelectListener listener) {
        _listener = listener;
    }

    /**
     * Gets the channel slider selection.
     *
     * @return channel
     */
    @Override
    public int getChannel(){
        // covert 1...n to 0...n-1
        return _stackWindow.getSlice() - 1;
    }

    /**
     * Disables and enables channel selection, during and after a fit.
     *
     * @param enable
     */
    @Override
    public void enable(boolean enable) {
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
    public int getPixel(int channel, int x, int y) {
        int returnValue = 0;
		if (_8bit) {
			//TODO this consistently results in "OutOfMemoryError: Java heap space"
			// getPixels calls getProcessor.
			// byte pixels[] = (byte [])_imageStack.getPixels(channel + 1);
			byte pixels[] = _saveOutPixels[channel];
			returnValue |= pixels[y * _width + x] & 0xff;
		}
		else {
			// a * 255 / b == 255 only when a == b, so we need to multiply by 256
			returnValue = _saveOutPixels2[channel][y * _width + x] * 256 / (int) getMaxTotalPhotons();
			if (returnValue > 255) {
				returnValue = 255;
			}
			returnValue &= 0xff;
		}
        return returnValue;
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
	public void updateThreshold(int threshold) {
		for (int y = 0; y < _height; ++y) {
		    for (int x = 0; x < _width; ++x) {
			   int alpha = TRANSPARENT;
               if (_saveOutPixels2[getChannel()][y * _width + x] < threshold) {
				   alpha = THRESHOLD_TRANSPARENCY;
			   }
			   _thresholdImage.setRGB(x, y, getColor(THRESHOLD_COLOR, alpha));
		    }
		}
		_imagePlus.draw();
	}
	
	@Override
	public void resetErrorMask(int channel) {
        updateErrorMask(new Mask(_width, _height), channel);
	}

	@Override
	public void updateErrorMask(Mask mask, int channel) {
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
    public void listenToMaskGroup(IMaskGroup maskGroup) {
		// apply the group mask
		applyMask(maskGroup.getMask());
		
		// each mask group should have a listener
		if (!_maskGroupSet.contains(maskGroup)) {
			_maskGroupSet.add(maskGroup);
			
			// create a new mask node that listens to the group
			IMaskNode maskNode = new MaskNode(maskGroup, new IMaskNodeListener () {
				// listen for mask changes
				@Override
				public void updateMasks(Mask otherMask, Mask totalMask) {
					applyMask(otherMask);
				}
			});
		}
    }
	
	private void applyMask(Mask mask) {
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
}
