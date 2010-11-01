/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.open2jam.gui;

import java.io.File;
import java.util.List;
import javax.swing.SwingWorker;
import org.open2jam.parser.ChartHeader;
import org.open2jam.parser.ChartParser;

/**
 *
 * @author fox
 */
public class ChartModelLoader extends SwingWorker<ChartTableModel,ChartHeader> {

    private ChartTableModel table_model;
    private File[] files;

    public ChartModelLoader(ChartTableModel table_model, File[] files){
        this.table_model = table_model;
        this.files = files;
    }

    protected ChartTableModel doInBackground() throws Exception {
        table_model.clear();
        int perc = files.length / 100;
        for(int i=0;i<files.length;i++)
        {
            publish(ChartParser.parseFileHeader(files[i].getAbsolutePath()));
            if(i%perc == 0)setProgress(i/perc);
        }
        return table_model;
    }


     protected void process(List<ChartHeader> chunks) {
         for (ChartHeader row : chunks) {
             table_model.addRow(row);
         }
     }

}
