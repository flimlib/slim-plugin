/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.slim.mask;

/**
 *
 * @author aivar
 */
public interface IMaskGroup {
    
    public void addNode(MaskNode node);

    public void removeNode(MaskNode node);

    public void updateMask(IMaskNode node, Mask mask);
}
