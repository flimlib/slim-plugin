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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mask group class associates a set of mask nodes.
 * <p>
 * Note that mask changes are a results of user interaction using the single
 * histogram tool, so threading issues are unlikely.
 *
 * @author Aivar Grislis
 */
public class MaskGroup implements IMaskGroup {

	List<IMaskNode> _nodeList;
	Map<IMaskNode, Mask> _maskMap;
	IMaskNode[] _nodes;

	public MaskGroup() {
		_nodeList = new ArrayList<IMaskNode>();
		_maskMap = new HashMap<IMaskNode, Mask>();
	}

	@Override
	public void addNode(final IMaskNode node) {
		// avoid duplicate entries
		if (!_nodeList.contains(node)) {
			_nodeList.add(node);
		}
	}

	@Override
	public void removeNode(final IMaskNode node) {
		_nodeList.remove(node);
		_maskMap.put(node, null);
	}

	@Override
	public void updateMask(final IMaskNode node) {
		// update map with node's new self mask
		final Mask selfMask = node.getSelfMask();
		_maskMap.put(node, selfMask);

		// combine masks and notify other nodes
		for (final IMaskNode peerNode : _nodeList) {
			// skip notifying the caller
			if (peerNode != node) {
				// combine all masks but the recipient's own
				final Mask peerSelfMask = _maskMap.get(peerNode);
				final Collection<Mask> masks = new ArrayList<Mask>(_maskMap.values());
				if (null != peerSelfMask) {
					masks.remove(peerSelfMask);
				}

				// notify other node
				final Mask peerOtherMask = Mask.addMasks(masks);
				peerNode.updateOtherMask(peerOtherMask);
			}
		}
	}

	@Override
	public Mask getMask() {
		// combine all masks
		return Mask.addMasks(_maskMap.values());
	}
}
