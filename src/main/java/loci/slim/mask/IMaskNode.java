/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.slim.mask;

/**
 *
 * @author aivar
 */
public interface IMaskNode {
    
    /**
     * This method notifies other nodes that this node has changed the mask.
     * 
     * @param mask 
     */
    public void updateSelfMask(Mask mask);

    /**
     * Gets the current mask created by this node.
     * 
     * @return 
     */
    public Mask getSelfMask();

    /**
     * This method notifies a node that other nodes have changed the mask.
     * 
     * @param mask 
     */
    public void updateOtherMask(Mask mask);

    /**
     * Gets the current mask created by all other nodes.
     * 
     * @return 
     */
    public Mask getOtherMask();

    /**
     * Gets the current mask.
     * 
     * @return 
     */
    public Mask getTotalMask();
}
