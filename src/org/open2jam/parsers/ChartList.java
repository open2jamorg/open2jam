package org.open2jam.parsers;

import java.io.File;
import java.util.ArrayList;

/**
 * the chart list contains all charts
 * produced by a source, ordered by level
 * 
 * @author fox
 */
public class ChartList extends ArrayList<Chart>
{
    File source_file;

    public File getSource()
    {
        return source_file;
    }
}
