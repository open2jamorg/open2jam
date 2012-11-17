package org.open2jam.render;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * The StatusList class represents a list of text to display at the right
 * hand side of the game window.
 * 
 * @author Thai Pangsakulyanont
 */
public class StatusList implements Iterable<String> {
    
    private LinkedList<StatusItem> items = new LinkedList<StatusItem>();

    /**
     * Adds a StatusItem to this StatusList
     * @param item a status item.
     */
    public void add(StatusItem item) {
        items.add(item);
    }
    
    @Override
    public Iterator<String> iterator() {
        final Iterator<StatusItem> it = items.iterator();
        return new Iterator<String>() {

            private String next = null;
            
            @Override
            public boolean hasNext() {
                if (next != null) return true;
                while (it.hasNext()) {
                    StatusItem nextItem = it.next();
                    if (nextItem.isVisible()) {
                        next = nextItem.getText();
                        return true;
                    } else {
                        it.remove();
                    }
                }
                return false;
            }

            @Override
            public String next() {
                String ret = next;
                next = null;
                return ret;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            
        };
    }
    
}
