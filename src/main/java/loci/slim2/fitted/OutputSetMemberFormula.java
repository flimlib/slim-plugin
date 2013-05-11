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
public interface OutputSetMemberFormula <T extends RealType<T> & NativeType<T>> {
	
	/**
	 * Formula for calculating a value for this tuple dimension.
	 * 
	 * @param <T> type of the values
	 */
	public T compute(List<T> values);
}
