package org.open2jam;

/**
 * Stores the version of the game
 * 
 * @author Thai Pangsakulyanont
 */
public class Open2jam {
    
    public static final String PRODUCT_NAME = "open2jam";
    public static final String OPEN2JAM_VERSION = "Alpha 7.1pre";

    public static String getProductTitle() {
        return PRODUCT_NAME + " [" + OPEN2JAM_VERSION + "]";
    }
    
}
