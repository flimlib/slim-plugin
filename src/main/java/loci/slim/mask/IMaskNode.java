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

package loci.slim.mask;

/**
 * Interface for a mask node.
 *
 * @author Aivar Grislis
 */
public interface IMaskNode {

	/**
	 * This method notifies other nodes that this node has changed the mask.
	 *
	 * @param mask or null
	 */
	public void updateSelfMask(Mask mask);

	/**
	 * Gets the current mask created by this node.
	 *
	 * @return mask or null
	 */
	public Mask getSelfMask();

	/**
	 * This method notifies a node that other nodes have changed the mask.
	 *
	 * @param mask or null
	 */
	public void updateOtherMask(Mask mask);

	/**
	 * Gets the current mask created by all other nodes.
	 *
	 * @return mask or null
	 */
	public Mask getOtherMask();

	/**
	 * Gets the current mask.
	 *
	 * @return mask or null
	 */
	public Mask getTotalMask();
}
