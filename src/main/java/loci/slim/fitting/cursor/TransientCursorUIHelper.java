/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.fitting.cursor;

/**
 *
 * @author aivar
 */
public class TransientCursorUIHelper implements ITransientCursorUI {
    FittingCursor _fittingCursor;
    IFittingCursorListener _fittingCursorListener;
    ITransientCursorUI _transientCursorUI;
    
    public TransientCursorUIHelper() {
    }
    
    public void setFittingCursor(FittingCursor fittingCursor) {
        if (null == _fittingCursor) {
            _fittingCursorListener = new FittingCursorListener();
        }
        else {
            _fittingCursor.removeListener(_fittingCursorListener);
        }
        _fittingCursor = fittingCursor;
        _fittingCursor.addListener(_fittingCursorListener);
    }
    
    public void setTransientCursorUI(ITransientCursorUI transientCursorUI) {
        _transientCursorUI = transientCursorUI;
        showTransientCursor();
    }
    
    /**
     * Gets the transient start cursor.
     * 
     * @return 
     */
    @Override
    public String getTransientStart() {
        return _transientCursorUI.getTransientStart();
    }
  
    /**
     * Sets the transient start cursor.
     * 
     * @param transientStart 
     */
    @Override
    public void setTransientStart(String transientStart) {
        _transientCursorUI.setTransientStart(transientStart);
        _fittingCursor.setTransientStart(transientStart);
    }
    
    /**
     * Gets the data start cursor.
     * @return 
     */
    @Override
    public String getDataStart() {
        return _transientCursorUI.getDataStart();
    }

    /**
     * Sets the data start cursor.
     * 
     * @param transientBaseline 
     */
    public void setDataStart(String dataStart) {
        _transientCursorUI.setDataStart(dataStart);
        _fittingCursor.setDataStart(dataStart);
    }
    
    /**
     * Gets the transient end cursor.
     * 
     * @return 
     */
    @Override
    public String getTransientStop() {
        return _transientCursorUI.getTransientStop();
    }

    /**
     * Sets the transient end cursor.
     * 
     * @param transientStop 
     */
    @Override
    public void setTransientStop(String transientStop) {
        _transientCursorUI.setTransientStop(transientStop);
        _fittingCursor.setTransientStop(transientStop); 
    }

    private void showTransientCursor() {
        if (_fittingCursor.getShowBins())  {
            int transientStart     = _fittingCursor.getTransientStartBin();
            int dataStart          = _fittingCursor.getDataStartBin();
            int transientStop      = _fittingCursor.getTransientStopBin();
            _transientCursorUI.setTransientStart   ("" + transientStart);
            _transientCursorUI.setDataStart        ("" + dataStart);
            _transientCursorUI.setTransientStop    ("" + transientStop);
        }
        else {
            double transientStart    = _fittingCursor.getTransientStartValue();
            double dataStart         = _fittingCursor.getDataStartBin();
            double transientStop     = _fittingCursor.getTransientStopValue();
            _transientCursorUI.setTransientStart   ("" + transientStart);
            _transientCursorUI.setDataStart        ("" + dataStart);
            _transientCursorUI.setTransientStop    ("" + transientStop);
        }
    }
    
    private class FittingCursorListener implements IFittingCursorListener {
        public void cursorChanged(FittingCursor cursor) {
            showTransientCursor();
        }
    }  
}
