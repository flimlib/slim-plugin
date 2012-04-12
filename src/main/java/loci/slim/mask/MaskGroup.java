/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.slim.mask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
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
    public void addNode(MaskNode node) {
        _nodeList.add(node);
    }

    @Override
    public void removeNode(MaskNode node) {
        _nodeList.remove(node);
        _maskMap.put(node, null);
    }

    @Override
    public void updateMask(IMaskNode node, Mask mask) {
        // update map with given mask
        _maskMap.put(node, mask);

        // combine maska and notify other nodes
        for (IMaskNode otherNode : _nodeList) {
            // don't notify the caller
            if (otherNode != node) {
                // don't combine the recipients mask
                Mask combinedMask = Mask.addOtherMasks(mask, _maskMap.values());
                otherNode.updateOtherMask(combinedMask);
            }
        }


    }
}
