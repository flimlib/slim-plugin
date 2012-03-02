/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loci.slim.fitting.cursor;

/**
 * This is a helper class for the user interface for setting and displaying
 * the fitting cursor start and stop values.  This listens for changes and
 * keeps the user interface display up to date.
 * 
 * @author Aivar Grislis
 */
public class FittingCursorHelper implements IFittingCursorUI {
    private FittingCursor _fittingCursor;
    private IFittingCursorListener _fittingCursorListener;
    private IFittingCursorUI _fittingCursorUI;
    
    /**
     * Sets the UI source and destination of fitting cursor strings, e.g. a
     * wrapper around some JTextField.
     * 
     * @param promptCursorUI 
     */
    public void setFittingCursorUI(IFittingCursorUI fittingCursorUI) {
        _fittingCursorUI = fittingCursorUI;
        showFittingCursor();
    }

    /**
     * Sets the fitting cursor that is keeping track of cursor settings.
     * 
     * @param fittingCursor 
     */
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

    /**
     * Gets whether to show bins or time values for cursors.
     * 
     * @return 
     */
    public boolean getShowBins() {
        return _fittingCursor.getShowBins();
    }
    
    /**
     * Gets the transient start cursor.
     * 
     * @return 
     */
    @Override
    public String getTransientStart() {
        return _fittingCursorUI.getTransientStart();
    }
  
    /**
     * Sets the transient start cursor.
     * 
     * @param transientStart 
     */
    @Override
    public void setTransientStart(String transientStart) {
        _fittingCursor.setTransientStart(transientStart);
    }
    
    /**
     * Gets the data start cursor.
     * 
     * @return 
     */
    @Override
    public String getDataStart() {
        return _fittingCursorUI.getDataStart();
    }

    /**
     * Sets the data start cursor.
     * 
     * @param transientBaseline 
     */
    @Override
    public void setDataStart(String dataStart) {
        _fittingCursor.setDataStart(dataStart);
    }
    
    /**
     * Gets the transient end cursor.
     * 
     * @return 
     */
    @Override
    public String getTransientStop() {
        return _fittingCursorUI.getTransientStop();
    }

    /**
     * Sets the transient end cursor.
     * 
     * @param transientStop 
     */
    @Override
    public void setTransientStop(String transientStop) {
        _fittingCursor.setTransientStop(transientStop); 
    }
    
    /**
     * Gets the prompt start cursor.
     * 
     * @return 
     */
    @Override
    public String getPromptStart() {
        return _fittingCursorUI.getPromptStart();
    }
  
    /**
     * Sets the prompt start cursor.
     * 
     * @param promptStart 
     */
    @Override
    public void setPromptStart(String promptStart) {
        _fittingCursor.setPromptStart(promptStart);
    }

    /**
     * Gets the prompt end cursor.
     * 
     * @return 
     */
    @Override
    public String getPromptStop() {
        return _fittingCursorUI.getPromptStop();
    }

    /**
     * Sets the prompt end cursor.
     * 
     * @param promptStop 
     */
    @Override
    public void setPromptStop(String promptStop) {
        _fittingCursor.setPromptStart(promptStop); 
    }
    
    /**
     * Gets the prompt baseline cursor as a string.
     * @return 
     */
    @Override
    public String getPromptBaseline() {
        return _fittingCursorUI.getPromptBaseline();
    }

    /**
     * Sets the prompt baseline cursor as a string.
     * 
     * @param promptBaseline 
     */
    @Override
    public void setPromptBaseline(String promptBaseline) {
        _fittingCursor.setPromptStart(promptBaseline);
    }

    /**
     * Shows current fitting cursor settings in UI.
     */
    private void showFittingCursor() {
        if (null != _fittingCursorUI) {
            _fittingCursorUI.setTransientStart(_fittingCursor.getTransientStart());
            _fittingCursorUI.setDataStart(_fittingCursor.getDataStart());
            _fittingCursorUI.setTransientStop(_fittingCursor.getTransientStop());
            _fittingCursorUI.setPromptStart(_fittingCursor.getPromptStart());
            _fittingCursorUI.setPromptStop(_fittingCursor.getPromptStop());
            _fittingCursorUI.setPromptBaseline(_fittingCursor.getPromptBaseline()); 
        }
    }
    
    /**
     * Inner listener for cursor changes.
     */   
    private class FittingCursorListener implements IFittingCursorListener {
        @Override
        public void cursorChanged(FittingCursor cursor) {
            showFittingCursor();
        }
    }  
}
