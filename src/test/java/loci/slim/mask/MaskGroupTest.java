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

import ij.IJ;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Aivar Grislis
 */
public class MaskGroupTest {

	public MaskGroupTest() {}

	@BeforeClass
	public static void setUpClass() {}

	@AfterClass
	public static void tearDownClass() {}

	@Before
	public void setUp() {}

	@After
	public void tearDown() {}

	@Test
	public void updateTest() {
		final Mask emptyMask = new Mask(3, 3);

		// X X X
		// O O O
		// O O O
		final Mask mask1 = new Mask(3, 3);
		mask1.set(0, 0);
		mask1.set(1, 0);
		mask1.set(2, 0);

		// X O O
		// X O O
		// X O O
		final Mask mask2 = new Mask(3, 3);
		mask2.set(0, 0);
		mask2.set(0, 1);
		mask2.set(0, 2);

		// X O O
		// O X O
		// O O X
		final Mask mask3 = new Mask(3, 3);
		mask3.set(0, 0);
		mask3.set(1, 1);
		mask3.set(2, 2);

		// X O X
		// O X O
		// X O X
		final Mask mask4 = new Mask(3, 3);
		mask4.set(0, 0);
		mask4.set(2, 0);
		mask4.set(1, 1);
		mask4.set(0, 2);
		mask4.set(2, 2);

		// X O O
		// O O O
		// O O O
		final Mask mask5 = new Mask(3, 3);
		mask5.set(0, 0);

		// X O X
		// O O O
		// O O O
		final Mask mask6 = new Mask(3, 3);
		mask6.set(0, 0);
		mask6.set(2, 0);

		// X O O
		// O O O
		// X O O
		final Mask mask7 = new Mask(3, 3);
		mask7.set(0, 0);
		mask7.set(0, 2);

		// hook up three nodes
		final MaskGroup maskGroup = new MaskGroup();
		final MyNode node1 = new MyNode("node1", maskGroup);
		final MyNode node2 = new MyNode("node2", maskGroup);
		final MyNode node3 = new MyNode("node3", maskGroup);

		// set mask in one node and observe propagation
		node1.updateSelfMask(mask1);

		checkNode(node1, mask1, null, mask1); // 1; self just set; other is null;
																					// total is self also
		checkNode(node2, null, mask1, mask1); // 2; self is null; other is 1's;
																					// total is 1's
		checkNode(node3, null, mask1, mask1); // 3; self is null; other is 1's;
																					// total is 1's

		// set another mask in another node and observe propagation
		node2.updateSelfMask(mask2);

		checkNode(node1, mask1, mask2, mask5); // 1; self from before; other from 2;
																						// total is 1 && 2
		checkNode(node2, mask2, mask1, mask5); // 2; self just set; other from 1;
																						// total is 1 && 2
		checkNode(node3, null, mask5, mask5); // 3; self is null; other is from 1 &&
																					// 2; total is 1 && 2

		// set third mask in third node
		node3.updateSelfMask(mask3);

		checkNode(node1, mask1, mask5, mask5); // 1; self from before; other is from
																						// 2 && 3; total is 1 && 2 && 3
		checkNode(node2, mask2, mask5, mask5); // 2; self from before; other is from
																						// 1 && 3; total is 1 && 2 && 3
		checkNode(node3, mask3, mask5, mask5); // 3; self just set; other is from 1
																						// && 2; total is 1 && 2 && 3

		// now change a node
		node2.updateSelfMask(mask4);

		checkNode(node1, mask1, mask3, mask5); // 1; self from before; other is from
																						// 3 && 4; total is 1 && 3 && 4
		checkNode(node2, mask4, mask5, mask5); // 2; self just set; other is from 1
																						// && 3; total is 1 && 3 && 4
		checkNode(node3, mask3, mask6, mask5); // 3; self from before; other is from
																						// 1 && 4; total is 1 && 3 && 4

		// delete a mask
		node1.updateSelfMask(null);

		checkNode(node1, null, mask3, mask3); // 1; self just set; other is from 3
																					// && 4; total is 3 && 4
		checkNode(node2, mask4, mask3, mask3); // 2; self from before; other is from
																						// 3; total is 3 && 4
		checkNode(node3, mask3, mask4, mask3); // 3; self from before; other is from
																						// 4; total is 3 && 4

		// delete another mask
		node3.updateSelfMask(null);

		checkNode(node1, null, mask4, mask4); // 1; self from before; other is from
																					// 4; total is 4
		checkNode(node2, mask4, null, mask4); // 2; self from before; no others;
																					// total is 4
		checkNode(node3, null, mask4, mask4); // 3; self from before; other is from
																					// 4; total is 4

		// delete last mask
		node2.updateSelfMask(null);

		checkNode(node1, null, null, null); // 1; self from before; no others; no
																				// total
		checkNode(node2, null, null, null); // 2; self from before; no others; no
																				// total
		checkNode(node3, null, null, null); // 3; self from before; no others; no
																				// total
	}

	private void checkNode(final IMaskNode node, final Mask self,
		final Mask other, final Mask total)
	{
		Mask mask = node.getSelfMask();
		checkMask(self, mask);
		mask = node.getOtherMask();
		checkMask(other, mask);
		mask = node.getTotalMask();
		checkMask(total, mask);
	}

	private void checkMask(final Mask want, final Mask got) {
		if (null == want) {
			if (null == got) {
				assert (true);
			}
			else {
				assert (!got.hasExcludedPixels());
			}
		}
		else {
			if (null == got) {
				assert (!want.hasExcludedPixels());
			}
			else {
				assert (want.equals(got));
			}
		}
	}

	private void dumpMask(final Mask mask) {
		if (null == mask) {
			IJ.log("NULL");
		}
		else for (int y = 0; y < 3; ++y) {
			final StringBuilder sb = new StringBuilder();
			for (int x = 0; x < 3; ++x) {
				sb.append("" + mask.getBits()[x][y] + " ");
			}
			IJ.log(sb.toString());
		}
	}

	private class MyNode implements IMaskNode {

		public String name;
		public MaskGroup maskGroup;
		public Mask selfMask;
		public Mask otherMask;

		public MyNode(final String name, final MaskGroup maskGroup) {
			this.name = name;
			this.maskGroup = maskGroup;
			maskGroup.addNode(this);
		}

		/**
		 * This method notifies other nodes that this node has changed the mask.
		 *
		 * @param mask or null
		 */
		@Override
		public void updateSelfMask(final Mask mask) {
			selfMask = mask;
			maskGroup.updateMask(this);
		}

		/**
		 * Gets the current mask created by this node.
		 *
		 * @return mask or null
		 */
		@Override
		public Mask getSelfMask() {
			return selfMask;
		}

		/**
		 * This method notifies a node that other nodes have changed the mask.
		 *
		 * @param mask or null
		 */
		@Override
		public void updateOtherMask(final Mask mask) {
			IJ.log("Node " + name + " updateOtherMask");
			dumpMask(mask);
			otherMask = mask;
		}

		/**
		 * Gets the current mask created by all other nodes.
		 *
		 * @return mask or null
		 */
		@Override
		public Mask getOtherMask() {
			return otherMask;
		}

		/**
		 * Gets the current mask.
		 *
		 * @return mask or null
		 */
		@Override
		public Mask getTotalMask() {
			if (null == selfMask) {
				if (null == otherMask) {
					return null;
				}
				return otherMask.clone();
			}
			return selfMask.add(otherMask);
		}
	}
}
