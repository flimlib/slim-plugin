/*
 * #%L
 * SLIM Curve plugin for combined spectral-lifetime image analysis.
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
 * Mask node class.
 * 
 * @author Aivar Grislis
 */
public class MaskNode implements IMaskNode {
	IMaskGroup _maskGroup;
	IMaskNodeListener _listener;
	Mask _selfMask;
	Mask _otherMask;

	public MaskNode(IMaskGroup maskGroup, IMaskNodeListener listener) {
		_maskGroup = maskGroup;
		_listener = listener;
		_selfMask = _otherMask = null;
		maskGroup.addNode(this);
	}

	/**
	 * This method should be called when node has changed its mask.
	 * 
	 * @param mask may be null
	 */
	@Override
	public void updateSelfMask(Mask selfMask) {
		_selfMask = selfMask;

		// show changes locally
		_listener.updateMasks(_otherMask, getTotalMask());

		// propagate changes through the mask group
		_maskGroup.updateMask(this);
	}

	/**
	 * Gets the current mask created by this node.
	 * 
	 * @return mask may be null
	 */
	@Override
	public Mask getSelfMask() {
		return _selfMask;
	}

	/**
	 * This method notifies this node that other nodes have changed masks.
	 * 
	 * @param otherMask may be null
	 */
	@Override
	public void updateOtherMask(Mask otherMask) {
		_otherMask = otherMask;

		_listener.updateMasks(_otherMask, getTotalMask());
	}

	String debugMask(Mask mask) {
		if (null == mask) {
			return "NULL";
		}
		return "" + mask.getCount();
	}

	/**
	 * Gets the current mask created by all other nodes.
	 * 
	 * @return mask may be null
	 */
	@Override
	public Mask getOtherMask() {
		return _otherMask;
	}

	/**
	 * Gets the current total mask.
	 * 
	 * @return mask may be null
	 */
	@Override
	public Mask getTotalMask() {
		Mask mask = null;
		if (null == _otherMask) {
			if (null != _selfMask) {
				mask = _selfMask.clone();
			}
		}
		else {
			mask = _otherMask.add(_selfMask);
		}
		return mask;
	}
}
