package org.open2jam.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import javax.swing.SwingWorker;
import org.open2jam.Config;
import org.open2jam.parsers.ChartList;
import org.open2jam.parsers.ChartParser;
import org.open2jam.util.Logger;

/**
 *
 * @author fox
 */
public class ChartModelLoader extends SwingWorker<ChartListTableModel,ChartList>
{

    private final ChartListTableModel table_model;
    private final File dir;

    public ChartModelLoader(ChartListTableModel table_model, File dir){
        this.table_model = table_model;
        this.dir = dir;
    }

    @Override
    protected ChartListTableModel doInBackground() {
        try{
        table_model.clear();
        ArrayList<File> files = new ArrayList<File>(Arrays.asList(dir.listFiles()));
        double perc = files.size() / 100d;
        for(int i=0;i<files.size();i++)
        {
            ChartList cl = ChartParser.parseFile(files.get(i));
            if(cl != null)publish(cl);
            else if(files.get(i).isDirectory()){
                List<File> nl = Arrays.asList(files.get(i).listFiles());
                files.addAll(nl);
                perc = files.size() / 100d;
            }
            setProgress((int)(i/perc));
        }
        setProgress(100);
        return table_model;
        }catch(Exception e){
            Logger.global.log(Level.SEVERE, "Exception in chart loader ! {0} {1}", new Object[]{e.toString(), e.getMessage()});
            System.exit(1);
            return null;
        }
    }

    @Override
    protected void done() {
        Config.setCache(dir, table_model.getRawList());
    }

    @Override
     protected void process(List<ChartList> chunks) {
        table_model.addRows(chunks);
     }
}
