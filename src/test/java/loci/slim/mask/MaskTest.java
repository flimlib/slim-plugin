/*
 * #%L
 * SLIM Plugin for combined spectral-lifetime image analysis.
 * %%
 * Copyright (C) 2010 - 2014 Board of Regents of the University of
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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Aivar Grislis
 */
public class MaskTest {
	
	public MaskTest() {
	}
	
	@BeforeClass
	public static void setUpClass() {
	}
	
	@AfterClass
	public static void tearDownClass() {
	}
	
	@Before
	public void setUp() {
	}
	
	@After
	public void tearDown() {
	}
	
	@Test
	public void testConstructors() {
		// construct at given size
		Mask mask1 = new Mask(400, 300);
		for (int y = 0; y < 300; ++y) {
			for (int x = 0; x < 400; ++x) {
				assert(!mask1.test(x, y));
			}
		}

		// construct with given bits
		boolean[][] bits = new boolean[500][400];
		Mask mask2 = new Mask(bits);
		for (int y = 0; y < 400; ++y) {
			for (int x = 0; x < 500; ++x) {
				assert(!mask2.test(x, y));
			}
		}
	}

	@Test
	public void testClone() {
		Mask mask1 = new Mask(400, 300);
		Mask mask2 = mask1.clone();
		
		// setting flag in original should not affect clone
		mask1.set(0,0);
		assert(!mask2.test(0, 0));
	}
	
	@Test
	public void testAdd1() {
		Mask mask1 = new Mask(10, 10);
		Mask mask2 = new Mask(10, 10);
		for (int y = 0; y < 10; ++y) {
			for (int x = 0; x < 10; ++x) {
				boolean yEven = y % 2 == 0;
				boolean xEven = x % 2 == 0;
				if (yEven == xEven) {
					mask1.set(x, y);
				}
				else {
					mask2.set(x, y);
				}
			}
		}
		Mask mask3 = mask1.add(mask2);
		for (int y = 0; y < 10; ++y) {
			for (int x = 0; x < 10; ++x) {
				assert(!mask3.test(x, y));
			}
		}
		mask3 = mask2.add(mask1);
		for (int y = 0; y < 10; ++y) {
			for (int x = 0; x < 10; ++x) {
				assert(!mask3.test(x, y));
			}
		}
	}
	
	@Test
	public void testAdd2() {
		Mask mask1 = new Mask(10, 10);
		Mask mask2 = new Mask(10, 10);
		for (int y = 0; y < 10; ++y) {
			for (int x = 0; x < 10; ++x) {
				mask1.set(x, y);
				mask2.set(x, y);
			}
		}
		Mask mask3 = mask1.add(mask2);
		for (int y = 0; y < 10; ++y) {
			for (int x = 0; x < 10; ++x) {
				assert(mask3.test(x, y));
			}
		}
		mask3 = mask2.add(mask1);
		for (int y = 0; y < 10; ++y) {
			for (int x = 0; x < 10; ++x) {
				assert(mask3.test(x, y));
			}
		}
	}
	
	@Test
	public void testAddMasks() {
		Mask mask1 = new Mask(3, 3);
		mask1.set(0, 0);
		mask1.set(1, 0);
		mask1.set(2, 0);
		Mask mask2 = new Mask(3, 3);
		mask2.set(0, 0);
		mask2.set(0, 1);
		mask2.set(0, 2);
		Mask mask3 = new Mask(3, 3);
		mask3.set(0, 0);
		mask3.set(1, 1);
		mask3.set(2, 2);
		Mask mask4 = new Mask(3, 3);
		mask4.set(0, 0);
		mask4.set(2, 0);
		mask4.set(1, 1);
		mask4.set(0, 2);
		mask4.set(2, 2);
		
		Collection<Mask> masks = new ArrayList<Mask>();
		Mask mask = Mask.addMasks(masks);
		assertEquals(mask, null);

		masks = new ArrayList<Mask>();
		masks.add(mask1);
		masks.add(mask2);
		mask = Mask.addMasks(masks);
		assert(mask.test(0, 0));
		assert(!mask.test(1, 0));
		assert(!mask.test(2, 0));
		assert(!mask.test(0, 1));
		assert(!mask.test(1, 1));
		assert(!mask.test(2, 1));
		assert(!mask.test(0, 2));
		assert(!mask.test(1, 2));
		assert(!mask.test(2, 2));
		
		masks = new ArrayList<Mask>();
		masks.add(mask1);
		masks.add(mask2);
		masks.add(mask3);
		mask = Mask.addMasks(masks);
		assert(mask.test(0, 0));
		assert(!mask.test(1, 0));
		assert(!mask.test(2, 0));
		assert(!mask.test(0, 1));
		assert(!mask.test(1, 1));
		assert(!mask.test(2, 1));
		assert(!mask.test(0, 2));
		assert(!mask.test(1, 2));
		assert(!mask.test(2, 2));
		
		masks = new ArrayList<Mask>();
		masks.add(mask3);
		masks.add(mask4);
		mask = Mask.addMasks(masks);
		assert(mask.test(0, 0));
		assert(!mask.test(1, 0));
		assert(!mask.test(2, 0));
		assert(!mask.test(0, 1));
		assert(mask.test(1, 1));
		assert(!mask.test(2, 1));
		assert(!mask.test(0, 2));
		assert(!mask.test(1, 2));
		assert(mask.test(2, 2));
	}
}
