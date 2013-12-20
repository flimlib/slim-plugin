/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim2.experimental;

/**
 *
 * @author aivar
 */
public class EnderleinErdmann {
	public static final int SINGLE = 0;
	public static final int DOUBLE = 1;
	public static final int TRIPLE = 2;
	
	public static void fit(int type, double[] decay, double[] irf) {
		int upperM = 0;
		double[] lowerP;
		double[][] upperVHat;
		double[] lowerY = new double[] { 0,3,1,2,2,0,0,0,1,1,0,0,1,1,0,1,0,2,2,1,1,1,1,1,0,1,2,1,0,0,1,0,2,3,2,0,0,0,0,1,0,2,0,2,0,0,0,2,0,0,2,3,2,3,4,3,7,12,3,5,3,7,10,1,7,3,6,8,1,10,6,4,6,7,6,4,7,4,3,4,3,0,2,3,1,4,2,3,5,1,3,6,5,7,4,3,4,6,6,5,3,3,5,5,4,1,3,4,4,5,5,3,4,3,2,2,4,3,3,2,4,1,3,3,4,4,2,6,0,1,0,6,3,3,4,3,3,4,1,3,0,2,1,1,2,1,1,0,3,2,1,1,2,1,3,1,1,3,1,1,0,2,1,1,3,0,3,1,1,2,0,1,3,0,2,0,1,4,1,1,3,0,3,1,3,2,4,1,1,0,3,1,2,0,1,1,0,2,1,4,2,0,2,0,2,0,2,0,2,0,0,1,2,0,0,1,0,1,1,2,2,1,3,3,1,1,1,2,0,0,0,4,2,1,1,2,3,4,1,4,1,2,1,2,2,0,2,0,1,0,1,1,0,1,1,1 };
		
		System.out.println("fit " + type);
		
		// approximate vector p, equations 28a-c
		switch (type) {
			case SINGLE:
				upperM = 1;
				break;
			case DOUBLE:
				upperM = 2;
				break;
			case TRIPLE:
				upperM = 3;
				break;
		}
		
		lowerP = new double[2 * upperM];
		for (int i = 1; i <= 2 * upperM; ++i) {
			if (1 == i) {
				lowerP[i - 1] = upperM;
			}
			else {
				lowerP[i - 1] = -((upperM - i + 1) * lowerP[i - 2]) / i;
			}
		}
		
		dump("lowerP " + type, lowerP);
		upperVHat = calculateUpperVHat(upperM, lowerY, lowerP);

	}
	
	private static double[][] calculateUpperVHat(int upperM, double[] lowerY, double[] lowerP) {
		double[][] upperVHat = new double[upperM][upperM];
		double sum;
		
		// initialize to zero
		for (int j = 0; j < upperM; ++j) {
			for (int i = 0; i < upperM; ++i) {
				upperVHat[i][j] = 0.0;
			}
		}

		// equation 25b
		for (int i = 0; i < upperM; ++i) {
			upperVHat[i][i] = lowerY[i + upperM];
			for (int j = 0; j < upperM; ++j) {
				upperVHat[i][i] += lowerP[j] * lowerP[j] * lowerY[i - j + upperM];
			}
		}

		// equations 25a & 25c
		for (int j = 0; j < upperM; ++j) {
			for (int i = 0; i < upperM; ++i) {
				if (i != j && 0 <= j - i && j - i < upperM) {
					sum = -lowerP[j - i] * lowerY[i + upperM];
					for (int k = 0; k < upperM - j + 1; ++k) {
						sum += lowerP[k] * lowerP[j - i + k] * lowerY[i - k + upperM];
					}
					upperVHat[i][j] = upperVHat[j][i] = sum;
				}
			}
		}
		dump("upperVHat", upperVHat);
		return upperVHat;
	}
	
	private static void dump(String label, double[] x) {
		System.out.print(label);
		for (double d : x) {
			System.out.print(" " + d);
		}
		System.out.println();
	}
	
	private static void dump(String label, double[][] x) {
		System.out.println(label);
		for (int j = 0; j < x.length; ++j) {
			for (int i = 0; i < x[0].length; ++i) {
				System.out.print(" " + x[i][j]);
			}
			System.out.println();
		}
	}
	
}
