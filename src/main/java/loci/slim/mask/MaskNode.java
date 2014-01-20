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
