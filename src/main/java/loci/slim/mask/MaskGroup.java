/*
 * #%L
 * SLIM Plugin for combined spectral-lifetime image analysis.
 * %%
 * Copyright (C) 2010 - 2014 Board of Regents of the University of
 * Wisconsin-Madison.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
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
    public void addNode(IMaskNode node) {
		// avoid duplicate entries
		if (!_nodeList.contains(node)) {
            _nodeList.add(node);
		}
    }

    @Override
    public void removeNode(IMaskNode node) {
        _nodeList.remove(node);
        _maskMap.put(node, null);
    }

    @Override
    public void updateMask(IMaskNode node) {
        // update map with node's new self mask
		Mask selfMask = node.getSelfMask();
		_maskMap.put(node, selfMask);

        // combine masks and notify other nodes
        for (IMaskNode peerNode : _nodeList) {
            // skip notifying the caller
            if (peerNode != node) {
                // combine all masks but the recipient's own
                Mask peerSelfMask = _maskMap.get(peerNode);
				Collection<Mask> masks = new ArrayList<Mask>(_maskMap.values());
				if (null != peerSelfMask) {
                    masks.remove(peerSelfMask);
				}

				// notify other node
                Mask peerOtherMask = Mask.addMasks(masks);
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
