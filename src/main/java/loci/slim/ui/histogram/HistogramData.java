/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.ui.histogram;

/**
 * Keeps track of data needed to draw a Histogram.
 * 
 * @author Aivar Grislis
 */
public class HistogramData {
    private float[] _values;
    int _valuesIndex;
    
    public HistogramData(int channels, int width, int height) {
        _values = new float[channels * width * height];
        _valuesIndex = 0;
    }
    
    public void accountForValue(float value) {
        _values[_valuesIndex++] = value;
    }
    
    public float[] getValues() {
        float[] values = new float[_valuesIndex];
        for (int i = 0; i < _valuesIndex; ++i) {
            values[i] = _values[i];
        }
        return values;
    }
    
}
