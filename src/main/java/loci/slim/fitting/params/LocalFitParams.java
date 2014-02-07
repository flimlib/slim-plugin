/*
 * #%L
 * SLIM Plugin for combined spectral-lifetime image analysis.
 * %%
 * Copyright (C) 2010 - 2014 Board of Regents of the University of
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

package loci.slim.fitting.params;

/**
 * Container for  the local fitting parameters, i.e. those valid for the current
 * pixel.
 * 
 * @author Aivar Grislis
 */
public class LocalFitParams implements ILocalFitParams {
    private double[] _y;
    private double[] _sig;
    private int _fitStart;
    private int _fitStop;
    private double[] _params;
    private double[] _yFitted;
    
    @Override
    public void setY(double[] y) {
        _y = y;
    }
    
    @Override
    public double[] getY() {
        return _y;
    }
 
    @Override
    public void setSig(double[] sig) {
        _sig = sig;
    }

    @Override
    public double[] getSig() {
        return _sig;
    }
    
    @Override
    public void setParams(double[] params) {
        _params = params;
    }
    
    @Override
    public double[] getParams() {
        return _params;
    }
    
    @Override
    public void setYFitted(double[] yFitted) {
        _yFitted = yFitted;
    }
    
    @Override
    public double[] getYFitted() {
        return _yFitted;
    }
}
