/*
 * #%L
 * SLIM plugin for combined spectral-lifetime image analysis.
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

package loci.slim;

/**
 * This class is a container for values for the instrument response function,
 * aka IRF, aka excitation, aka prompt, aka lamp function.
 *
 * @author Aivar Grislis
 */
public class Excitation {
    private final String _fileName;
    private final double[] _values;
    private final double _timeInc;
    private int _start;
    private int _stop;
    private double _base;

    /**
     * Creates an excitation with given filename and values.
     *
     * @param fileName
     * @param values
     */
    public Excitation(String fileName, double[] values, double timeInc) {
        _fileName = fileName;
        _values = values;
        _timeInc = timeInc;
    }

    /**
     * Gets the file name.
     *
     * @return
     */
    public String getFileName() {
        return _fileName;
    }

    /**
     * Gets the values of the excitation curve.
     *
     * @return
     */
    public double[] getValues() {
        return _values;
    }

    /**
     * Gets the horizontal time increment for the excitation curve.
     *
     * @return
     */
    public double getTimeInc() {
        return _timeInc;
    }

    /**
     * Sets start cursor.
     *
     * @param start
     */
    public void setStart(int start) {
        _start = start;
    }

    /**
     * Gets start cursor.
     *
     * @return
     */
    public int getStart() {
        return _start;
    }

    /**
     * Sets the stop cursor.
     * 
     * @param stop
     */
    public void setStop(int stop) {
        _stop = stop;
    }

    /**
     * Gets the stop cursor.
     *
     * @return
     */
    public int getStop() {
        return _stop;
    }

    /**
     * Sets the base cursor.
     *
     * @param base
     */
    public void setBase(double base) {
        _base = base;
    }

    /**
     * Gets the base cursor.
     *
     * @return
     */
    public double getBase() {
        return _base;
    }

}
