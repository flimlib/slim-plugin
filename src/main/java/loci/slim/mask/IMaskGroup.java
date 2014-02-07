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

package loci.slim.mask;

/**
 * This is an interface for a group of associated mask nodes.
 * 
 * @author Aivar Grislis
 */
public interface IMaskGroup {

    /**
     * Adds a mask node to group.
     * 
     * @param node 
     */
    public void addNode(IMaskNode node);

    /**
     * Removes a mask node from group.
     * 
     * @param node 
     */
    public void removeNode(IMaskNode node);

    /**
     * Updates the mask, notifies group.
     * 
     * @param node originating node
     */
    public void updateMask(IMaskNode node);

	/**
	 * Gets the current total mask for this group.
	 * 
	 * @return 
	 */
	public Mask getMask();
}
