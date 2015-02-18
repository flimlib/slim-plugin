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

package loci.slim2.fitting;

/**
 * Container for local fitted results for current pixel.
 * 
 * @author Aivar Grislis
 */
public class DefaultFitResults implements FitResults {
	int errorCode;
	double chiSquare;
	double[] params;
	double[] yFitted;
	double[] trans;
	int photonCount;
	int transStart;
	int dataStart;
	int transStop;

	@Override
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	@Override
	public int getErrorCode() {
		return errorCode;
	}

	@Override
	public void setChiSquare(double chiSquare) {
		this.chiSquare = chiSquare;
	}

	@Override
	public double getChiSquare() {
		return chiSquare;
	}

	@Override
	public void setParams(double[] params) {
		this.params = params;
	}

	@Override
	public double[] getParams() {
		return params;
	}

	@Override
	public void setYFitted(double[] yFitted) {
		this.yFitted = yFitted;
	}

	@Override
	public double[] getYFitted() {
		return yFitted;
	}

	@Override
	public void setTransient(double[] trans) {
		this.trans = trans;
	}

	@Override
	public double[] getTransient() {
		return trans;
	}

	@Override
	public void setPhotonCount(int photonCount) {
		this.photonCount = photonCount;
	}

	@Override
	public int getPhotonCount() {
		return photonCount;
	}

	@Override
	public void setTransStart(int transStart) {
		this.transStart = transStart;
	}

	@Override
	public int getTransStart() {
		return transStart;
	}

	@Override
	public void setDataStart(int dataStart) {
		this.dataStart = dataStart;
	}

	@Override
	public int getDataStart() {
		return dataStart;
	}

	@Override
	public void setTransStop(int transStop) {
		this.transStop = transStop;
	}

	@Override
	public int getTransStop() {
		return transStop;
	}
}
