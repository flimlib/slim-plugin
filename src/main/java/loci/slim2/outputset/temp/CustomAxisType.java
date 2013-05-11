/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim2.outputset.temp;

import net.imglib2.meta.AxisType;

//TODO ARG copied from Axes.java in ImgLib2 Core Library, here for now only

/**
 * A custom dimensional axis type, for describing the dimensional axes of a
 * {@link CalibratedSpace} object (such as an {@link ImgPlus}).
 */
public class CustomAxisType implements AxisType {
	private final String label;

	public CustomAxisType(final String label) {
		this.label = label;
	}

	// -- Axis methods --
	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public boolean isXY() {
		return false;
	}

	@Override
	public boolean isSpatial() {
		return false;
	}

	// -- Object methods --
	@Override
	public String toString() {
		return label;
	}
	
}
