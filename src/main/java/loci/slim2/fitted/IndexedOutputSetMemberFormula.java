/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim2.fitted;

import java.util.List;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/**
 *
 * @author Aivar Grislis
 */
public class IndexedOutputSetMemberFormula <T extends RealType<T> & NativeType<T>> extends AbstractOutputSetMemberFormula<T> implements OutputSetMemberFormula<T> {
	private long index;
	
	public IndexedOutputSetMemberFormula(int index) {
		this.index = index;
	}
	@Override
	public T compute(List<T> values) {
		return values.get((int) index);
	}
}
