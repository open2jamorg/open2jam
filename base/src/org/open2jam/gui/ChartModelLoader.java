/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.open2jam.gui;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import javax.swing.SwingWorker;
import org.open2jam.parser.Chart;
import org.open2jam.parser.ChartParser;

/**
 *
 * @author fox
 */
public class ChartModelLoader extends SwingWorker<ChartTableModel,Chart> {

    private ChartTableModel table_model;
    private File dir;

    public ChartModelLoader(ChartTableModel table_model, File dir){
        this.table_model = table_model;
        this.dir = dir;
    }

    protected ChartTableModel doInBackground() {
        List<File> files = Arrays.asList(dir.listFiles());
        table_model.clear();
        double perc = files.size() / 100.0d;
        for(int i=0;i<files.size();i++)
        {
            try{
                publish(ChartParser.parseFile(files.get(i)));
            }catch(UnsupportedOperationException e){}
            setProgress((int)(i/perc));
        }
        setProgress(100);
        return table_model;
    }


    @Override
     protected void process(List<Chart> chunks) {
         for (Chart row : chunks) {
             table_model.addRow(row);
         }
     }
}
