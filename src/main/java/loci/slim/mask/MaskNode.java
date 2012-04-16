/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.slim.mask;

/**
 *
 * @author Aivar Grislis
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
