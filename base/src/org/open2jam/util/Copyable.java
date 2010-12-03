
package org.open2jam.util;

/**
 *
 * @author fox
 */
public interface Copyable {

    /** 
     *
     * @return returns a copy of the object.
     */
    public <T extends Copyable> T copy();

}
