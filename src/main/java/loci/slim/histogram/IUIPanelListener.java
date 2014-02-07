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

package loci.slim.histogram;

/**
 *
 * @author Aivar Grislis
 */
public interface IUIPanelListener {

	/**
	 * User has clicked the auto ranging checkbox.
	 * 
	 * @param autoRange
	 */
	public void setAutoRange(boolean autoRange);

	/**
	 * User has clicked the exclude pixels checkbox.
	 * 
	 * @param excludePixels
	 */
	public void setExcludePixels(boolean excludePixels);

	/**
	 * User has clicked the combine channels checkbox.
	 */
	public void setCombineChannels(boolean combineChannels);

	/**
	 * User has clicked the display channels checkbox.
	 * 
	 * @param displayChannels 
	 */
	public void setDisplayChannels(boolean displayChannels);

	/**
	 * User has clicked the logarithmic display checkbox.
	 * 
	 * @param log
	 */
	public void setLogarithmicDisplay(boolean log);

	/**
	 * User has clicked the smoothing checkbox.
	 * 
	 * @param smooth 
	 */
	public void setSmoothing(boolean smooth);

	/**
	 * User has entered new bandwidth (used for smoothing).
	 * 
	 * @param bandwidth 
	 */
	public void setBandwidth(double bandwidth);

	/**
	 * User has clicked the family style 1 checkbox.
	 * 
	 * @param on
	 */
	public void setFamilyStyle1(boolean on);

	/**
	 * User has clicked the family style 1 checkbox.
	 * 
	 * @param on 
	 */
	public void setFamilyStyle2(boolean on);

	/**
	 * User has entered new min/max LUT value.
	 *
	 * @param min
	 * @param max
	 */
	public void setMinMaxLUT(double min, double max);
}
