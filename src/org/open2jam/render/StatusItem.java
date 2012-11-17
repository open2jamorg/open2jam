package org.open2jam.render;

/**
 * Represents a single line of a status item to display at the right hand side
 * of the gameplay window.
 * 
 * You'd create a StatusItem object and add it to a StatusList to make some
 * text display on the gameplay window.
 * 
 * @author Thai Pangsakulyanont
 */
public interface StatusItem {
    
    /**
     * Returns the text to display on that status line.
     * 
     * @return the text to display
     */
    String getText();
    
    /**
     * Returns true if this status item should keep displaying. If you return
     * false, then this status line will be removed from the StatusList.
     * 
     * @return true if this status should keep being visible, false otherwise.
     */
    boolean isVisible();
    
}
