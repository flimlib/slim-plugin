/*
 * #%L
 * SLIM plugin for combined spectral-lifetime image analysis.
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

package loci.slim;

/**
 * Interface for a table of chunky pixels.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://dev.loci.wisc.edu/trac/software/browser/trunk/projects/slim-plugin/src/main/java/loci/slim/IChunkyPixelTable.java">Trac</a>,
 * <a href="http://dev.loci.wisc.edu/svn/software/trunk/projects/slim-plugin/src/main/java/loci/slim/IChunkyPixelTable.java">SVN</a></dd></dl>
 *
 * @author Aivar Grislis
 */
public interface IChunkyPixelTable {

	/**
	 * Get table size.
	 *
	 * @return size
	 */
	public int size();

	/**
	 * Get pixel width.
	 * 
	 * @return width
	 */
	public int getWidth();

	/**
	 * Get pixel height.
	 * 
	 * @return height;
	 */
	public int getHeight();

	/**
	 * Get table entry at index.
	 * 
	 * @param index
	 * @return ChunkyPixel
	 */
	public ChunkyPixel getChunkyPixel(int index);
}
