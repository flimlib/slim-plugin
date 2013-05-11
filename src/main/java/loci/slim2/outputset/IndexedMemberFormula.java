/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim2.outputset;

import java.util.List;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/**
 * This is the simplest {@link MemberFormula} that just takes the input value at
 * a given index.
 * 
 * @author Aivar Grislis
 */
public class IndexedMemberFormula <T extends RealType<T> & NativeType<T>> /*extends AbstractMemberFormula<T>*/ implements MemberFormula<T> {
	private long index;
	
	public IndexedMemberFormula(int index) {
		this.index = index;
	}
	@Override
	public T compute(List<T> values) {
		return values.get((int) index);
	}
}
