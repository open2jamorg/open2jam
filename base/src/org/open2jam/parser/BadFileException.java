/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.open2jam.parser;

/**
 *
 * @author fox
 */
public class BadFileException extends RuntimeException {

    /**
     * Creates a new instance of <code>BadFileException</code> without detail message.
     */
    public BadFileException() {
    }


    /**
     * Constructs an instance of <code>BadFileException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public BadFileException(String msg) {
        super(msg);
    }
}
