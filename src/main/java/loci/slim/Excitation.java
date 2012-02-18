//
// Excitation.java
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

/**
 * This class is a container for values for the instrument response function,
 * aka excitation, aka prompt, aka lamp function.
 *
 * @author Aivar Grislis
 */
public class Excitation {
    private final String m_fileName;
    private final double[] m_values;
    private final double m_timeInc;
    private int m_start;
    private int m_stop;
    private double m_base;

    /**
     * Creates an excitation with given filename and values.
     *
     * @param fileName
     * @param values
     */
    public Excitation(String fileName, double[] values, double timeInc) {
        m_fileName = fileName;
        m_values = values;
        m_timeInc = timeInc;
    }

    /**
     * Gets the file name.
     *
     * @return
     */
    public String getFileName() {
        return m_fileName;
    }

    /**
     * Gets the values of the excitation curve.
     *
     * @return
     */
    public double[] getValues() {
        return m_values;
    }

    /**
     * Gets the horizontal time increment for the excitation curve.
     *
     * @return
     */
    public double getTimeInc() {
        return m_timeInc;
    }

    /**
     * Sets start cursor.
     *
     * @param start
     */
    public void setStart(int start) {
        m_start = start;
    }

    /**
     * Gets start cursor.
     *
     * @return
     */
    public int getStart() {
        return m_start;
    }

    /**
     * Sets the stop cursor.
     * 
     * @param stop
     */
    public void setStop(int stop) {
        m_stop = stop;
    }

    /**
     * Gets the stop cursor.
     *
     * @return
     */
    public int getStop() {
        return m_stop;
    }

    /**
     * Sets the base cursor.
     *
     * @param base
     */
    public void setBase(double base) {
        m_base = base;
    }

    /**
     * Gets the base cursor.
     *
     * @return
     */
    public double getBase() {
        return m_base;
    }

}
