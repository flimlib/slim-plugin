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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

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
    private int _width;
    private int _height;
    private ImageStack _imageStack;
    private MyStackWindow _stackWindow;
    private ISelectListener _listener;
    private byte[] _saveOutPixels[];
    private double _minNonZeroPhotonCount;
    private int[] _brightestPoint;

    public GrayScaleImage(Image<T> image) {
        String title = image.getName();
        int spaceIndex = title.indexOf(" ");
        if (0 < spaceIndex) {
            title = title.substring(0, spaceIndex);
        }
        int dimensions[] = image.getDimensions();
        //for (int i = 0; i < dimensions.length; ++i) {
        //    System.out.println("dim[" + i + "] " + dimensions[i]);
        //}
        _width = dimensions[0];
        _height = dimensions[1];
        int bins = dimensions[2];
        int channels = 1;
        if (dimensions.length > 3) {
            channels = dimensions[3];
        }

        // building an image stack
        _imageStack = new ImageStack(_width, _height);
        _saveOutPixels = new byte[channels][];

        LocalizableByDimCursor cursor = image.createLocalizableByDimCursor();
        double[][] pixels = new double[_width][_height];
        int[] position = (channels > 1) ? new int[4] : new int[3];

        _minNonZeroPhotonCount = Double.MAX_VALUE;
        for (int c = 0; c < channels; ++c) {
            if (channels > 1) {
                position[3] = c;
            }
            byte[] outPixels = new byte[_width * _height];

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
                        _brightestPoint = new int[] { x , y };
                    }
                }
            }

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
        ImagePlus imagePlus = new ImagePlus(title, _imageStack);
        _stackWindow = new MyStackWindow(imagePlus);
        _stackWindow.setVisible(true);
        
        System.out.println("minNonZeroPhotonCount is " + _minNonZeroPhotonCount);

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
                    if (null != _listener) {
                        _listener.selected(getChannel(), e.getX(), e.getY());
                    }
                }
            }
        );
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

    @Override
    public float getZoomFactor() {
        return _stackWindow.getZoomFactor();
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
        //TODO this consistently results in "OutOfMemoryError: Java heap space"
        // getPixels calls getProcessor.
        // byte pixels[] = (byte [])_imageStack.getPixels(channel + 1);
        byte pixels[] = _saveOutPixels[channel];
        returnValue |= pixels[y * _width + x] & 0xff;
        return returnValue;
    }
    
    @Override
    public double getMinNonZeroPhotonCount() {
        return _minNonZeroPhotonCount;
    }
    
    @Override
    public int[] getBrightestPoint() {
        return _brightestPoint;
    }
}
