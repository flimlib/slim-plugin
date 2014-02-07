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

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import loci.formats.FormatException;
import loci.formats.in.ICSReader;
import loci.formats.meta.MetadataRetrieve;
import loci.formats.out.ICSWriter;

/**
 * Loads and saves excitation files.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/software/browser/trunk/projects/slim-plugin/src/main/java/loci/slim/ExcitationFileHandler.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/software/trunk/projects/slim-plugin/src/main/java/loci/slim/ExcitationFileHandler.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis
 */
public class ExcitationFileHandler {
    private static final String ICS = ".ics";
    private static final String IRF = ".irf";
    private static ExcitationFileHandler s_instance = null;

    public static synchronized ExcitationFileHandler getInstance() {
        if (null == s_instance) {
            s_instance = new ExcitationFileHandler();
        }
        return s_instance;
    }

    public Excitation loadExcitation(String fileName, double timeInc) {
        Excitation excitation = null;
        double values[] = null;
        if (fileName.toLowerCase().endsWith(ICS)) {
            values = loadICSExcitationFile(fileName);
        }
        else {
            if (!fileName.toLowerCase().endsWith(IRF)) {
                fileName += IRF;
            }
            values = loadIRFExcitationFile(fileName);
        }
        if (null != values) {
            excitation = new Excitation(fileName, values, timeInc);
        }
        return excitation;
    }

    public Excitation createExcitation(String fileName, double[] values, double timeInc) {
        Excitation excitation = null;
        boolean success = false;
        if (fileName.endsWith(ICS)) {
            success = saveICSExcitationFile(fileName, values);
        }
        else {
            if (!fileName.endsWith(IRF)) {
                fileName += IRF;
            }
            success = saveIRFExcitationFile(fileName, values);
        }
        if (success) {
            excitation = new Excitation(fileName, values, timeInc);
        }
        return excitation;
    }

    private double[] loadICSExcitationFile(String fileName) {
        double[] results = null;
        ICSReader icsReader = new ICSReader();
        try {
            icsReader.setId(fileName);
            int bitsPerPixel = icsReader.getBitsPerPixel();
            int bytesPerPixel = bitsPerPixel / 8;
            boolean littleEndian = icsReader.isLittleEndian();
            boolean interleaved = icsReader.isInterleaved();
            int bins = icsReader.getSizeC();
            if (1 == bins) {
                // hack for lifetime ICS that reader doesn't recognize as such
                bins = icsReader.getSizeZ();
            }
            results = new double[bins];
            byte bytes[];
            if (false || icsReader.isInterleaved()) { //TODO ARG interleaved does not read the whole thing; was 130K, now 32767
                // this returns the whole thing
                bytes = icsReader.openBytes(0);
                System.out.println("INTERLEAVED reads # bytes: " + bytes.length);
                for (int bin = 0; bin < bins; ++bin) {
                    results[bin] = convertBytesToDouble(littleEndian, bitsPerPixel, bytes, bytesPerPixel * bin);
                }
            }
            else {
                for (int bin = 0; bin < bins; ++bin) {
                    bytes = icsReader.openBytes(bin);
                    results[bin] = convertBytesToDouble(littleEndian, bitsPerPixel, bytes, 0);
                }
            }
            icsReader.close();
        }
        catch (IOException e) {
            System.out.println("IOException " + e.getMessage());
        }
        catch (FormatException e) {
            System.out.println("FormatException " + e.getMessage());
        }
        return results;
    }

    //TODO doesn't work; needed to interoperate with TRI2
    private boolean saveICSExcitationFile(String fileName, double[] values) {
        boolean success = false;
        ICSWriter icsWriter = new ICSWriter();
        MetadataRetrieve meta = null;
//        icsWriter.setMetadataRetrieve(meta);
        try {
            for (int bin = 0; bin < values.length; ++bin) {
                icsWriter.saveBytes(bin, convertDoubleToBytes(values[bin]));
            }
            success = true;
        }
        catch (IOException e) {
            System.out.println("IOException " + e.getMessage());
        }
        catch (FormatException e) {
            System.out.println("FormatException " + e.getMessage());
        }        
        return success;
    }

    private double[] loadIRFExcitationFile(String fileName) {
        double[] values = null;
        try {
            ArrayList<Float> valuesArrayList = new ArrayList<Float>();
            Scanner scanner = new Scanner(new FileReader(fileName));
            String line = null;
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                valuesArrayList.add(Float.parseFloat(line));
            }
            values = new double[valuesArrayList.size()];
            for (int i = 0; i < valuesArrayList.size(); ++i) {
                values[i] = valuesArrayList.get(i);
            }
        }
        catch (Exception e) {
            System.out.println("Exception " + e.getMessage());
        }
        return values;
    }

    private boolean saveIRFExcitationFile(String fileName, double[] values) {
        boolean success = false;
        try {
            FileWriter writer = new FileWriter(fileName);
            for (int i = 0; i < values.length; ++i) {
                if (i > 0) {
                    writer.append('\n');
                }
                writer.append(Double.toString(values[i]));
            }
            writer.flush();
            writer.close();
            success = true;
        }
        catch (IOException e) {
            System.out.println("IOException " + e.getMessage());
        }
        return success;
    }

    private byte[] convertDoubleToBytes(double d) {
        float f = (float) d;
        int rawIntBits = Float.floatToRawIntBits(f);
        byte[] result = new byte[4];
        for (int i = 0; i < 4; ++i) {
            int offset = 8 * i;
            result[i] = (byte) ((rawIntBits >>> offset) & 0xff);
        }
        return result;
    }

    /**
     * Converts a little-endian four byte array to a double.
     *
     * @param littleEndian byte order
     * @param bitsPerPixel
     * @param bytes
     * @param index
     * @return
     */
    private double convertBytesToDouble(boolean littleEndian, int bitsPerPixel, byte[] bytes, int index) {
        double returnValue = 0.0f;
        if (32 == bitsPerPixel) {
            int i = 0;
            if (littleEndian) {
                i |= bytes[index + 3] & 0xff;
                i <<= 8;
                i |= bytes[index + 2] & 0xff;
                i <<= 8;
                i |= bytes[index + 1] & 0xff;
                i <<= 8;
                i |= bytes[index + 0] & 0xff;
            }
            else {
                i |= bytes[index + 0] & 0xff;
                i <<= 8;
                i |= bytes[index + 1] & 0xff;
                i <<= 8;
                i |= bytes[index + 2] & 0xff;
                i <<= 8;
                i |= bytes[index + 3] & 0xff;
            }
            returnValue = Float.intBitsToFloat(i);
        }
        else if (64 == bitsPerPixel) {
            long l = 0;
            if (littleEndian) {
                l |= bytes[index + 7] & 0xff;
                l <<= 8;
                l |= bytes[index + 6] & 0xff;
                l <<= 8;
                l |= bytes[index + 5] & 0xff;
                l <<= 8;
                l |= bytes[index + 4] & 0xff;
                l <<= 8;
                l |= bytes[index + 3] & 0xff;
                l <<= 8;
                l |= bytes[index + 2] & 0xff;
                l <<= 8;
                l |= bytes[index + 1] & 0xff;
                l <<= 8;
                l |= bytes[index + 0] & 0xff;
            }
            else {
                l |= bytes[index + 0] & 0xff;
                l <<= 8;
                l |= bytes[index + 1] & 0xff;
                l <<= 8;
                l |= bytes[index + 2] & 0xff;
                l <<= 8;
                l |= bytes[index + 3] & 0xff;
                l <<= 8;
                l |= bytes[index + 4] & 0xff;
                l <<= 8;
                l |= bytes[index + 5] & 0xff;
                l <<= 8;
                l |= bytes[index + 6] & 0xff;
                l <<= 8;
                l |= bytes[index + 7] & 0xff;
            }
            returnValue = (double) Double.longBitsToDouble(l);
        }
        return returnValue;
    }
}
