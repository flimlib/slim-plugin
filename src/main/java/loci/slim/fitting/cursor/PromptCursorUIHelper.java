/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.fitting.cursor;

/**
 *
 * @author aivar
 */
public class PromptCursorUIHelper implements IPromptCursorUI {
    FittingCursor _fittingCursor;
    IFittingCursorListener _fittingCursorListener;
    IPromptCursorUI _promptCursorUI;
    
    public PromptCursorUIHelper() {
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
    
    public void setPromptCursorUI(IPromptCursorUI promptCursorUI) {
        _promptCursorUI = promptCursorUI;
        showPromptCursor();
    }
    
    /**
     * Gets the prompt start cursor.
     * 
     * @return 
     */
    @Override
    public String getPromptStart() {
        return _promptCursorUI.getPromptStart();
    }
  
    /**
     * Sets the prompt start cursor.
     * 
     * @param promptStart 
     */
    @Override
    public void setPromptStart(String promptStart) {
        _promptCursorUI.setPromptStart(promptStart);
        _fittingCursor.setPromptStart(promptStart);
    }

    /**
     * Gets the prompt end cursor.
     * 
     * @return 
     */
    @Override
    public String getPromptStop() {
        return _promptCursorUI.getPromptStop();
    }

    /**
     * Sets the prompt end cursor.
     * 
     * @param promptStop 
     */
    @Override
    public void setPromptStop(String promptStop) {
        _promptCursorUI.setPromptStop(promptStop);
        _fittingCursor.setPromptStart(promptStop); 
    }
    
    /**
     * Gets the prompt baseline cursor.
     * @return 
     */
    public String getPromptBaseline() {
        return _promptCursorUI.getPromptBaseline();
    }

    /**
     * Sets the prompt baseline cursor.
     * 
     * @param promptBaseline 
     */
    public void setPromptBaseline(String promptBaseline) {
        _promptCursorUI.setPromptBaseline(promptBaseline);
        _fittingCursor.setPromptStart(promptBaseline);
    }

    private void showPromptCursor() {
        if (_fittingCursor.getShowBins())  {
            int promptStart     = _fittingCursor.getPromptStartBin();
            int promptStop      = _fittingCursor.getPromptStopBin();
            int promptBaseline  = _fittingCursor.getPromptBaselineBin(); //TODO ARG no such thing!?
            _promptCursorUI.setPromptStart   ("" + promptStart);
            _promptCursorUI.setPromptStop    ("" + promptStop);
            _promptCursorUI.setPromptBaseline("" + promptBaseline);
        }
        else {
            double promptStart    = _fittingCursor.getPromptStartValue();
            double promptStop     = _fittingCursor.getPromptStopValue();
            double promptBaseline = _fittingCursor.getPromptBaselineValue();
            _promptCursorUI.setPromptStart   ("" + promptStart);
            _promptCursorUI.setPromptStop    ("" + promptStop);
            _promptCursorUI.setPromptBaseline("" + promptBaseline);
        }
    }
    
    private class FittingCursorListener implements IFittingCursorListener {
        public void cursorChanged(FittingCursor cursor) {
            showPromptCursor();
        }
    }  
}
