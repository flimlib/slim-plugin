//
// MaskNode.java
//

/*
SLIMPlugin for combined spectral-lifetime image analysis.

Copyright (c) 2010, UW-Madison LOCI
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package loci.slim.mask;

/**
 * Mask node class.
 * 
 * @author Aivar Grislis grislis at wisc dot edu
 */
public class MaskNode implements IMaskNode {
    IMaskGroup _maskGroup;
    IMaskNodeListener _listener;
    Mask _selfMask;
    volatile Mask _otherMask;
    
    public MaskNode(IMaskGroup maskGroup, IMaskNodeListener listener) {
        _maskGroup = maskGroup;
        _listener = listener;
    }

    /**
     * This method notifies other nodes that this node has changed its mask.
     * 
     * @param mask 
     */
    public void updateSelfMask(Mask mask) {
        _maskGroup.updateMask(this, mask);
    }

    /**
     * Gets the current mask created by this node.
     * 
     * @return 
     */
    public Mask getSelfMask() {
        return _selfMask;
    }

    /**
     * This method notifies this node that other nodes have changed the mask.
     * 
     * @param mask 
     */
    public void updateOtherMask(Mask mask) {
        _otherMask = mask;
        _listener.updateMask(mask);
    }

    /**
     * Gets the current mask created by all other nodes.
     * 
     * @return 
     */
    public Mask getOtherMask() {
        return _otherMask;
    }

    /**
     * Gets the current mask.
     * 
     * @return 
     */
    public Mask getTotalMask() {
        return _selfMask.add(_otherMask);
    }
}
