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
    Mask _selfMask;
    Mask _otherMask;
    
    public MaskNode(IMaskGroup maskGroup) {
        _maskGroup = maskGroup;
    }

    /**
     * This method notifies other nodes that this node has changed the mask.
     * 
     * @param mask 
     */
    public void updateSelfMask(Mask mask) {
        _maskGroup.updateMask(this, mask);
        //TODO ARG promulgate changes
        //  this should change the image and not the histogram
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
     * This method notifies a node that other nodes have changed the mask.
     * 
     * @param mask 
     */
    public void updateOtherMask(Mask mask) {
        _otherMask = mask;
        //TODO ARG promulgate changes; need a listener
        //  this should change the image and the histogram
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
