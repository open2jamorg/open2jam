package org.open2jam.gui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.open2jam.parsers.Chart;
import org.open2jam.parsers.ChartList;

/**
 *
 * @author fox
 */
public class ChartTableModel implements TableModel
{
    private final List<Chart> items;
    private final String[] col_names = new String[] { "Level", "Notes", "Keys" };
    private ChartList chartlist;
    
    private final List<TableModelListener> listeners;

    public ChartTableModel()
    {
        listeners = new ArrayList<TableModelListener>();
        items = new ArrayList<Chart>();
    }
    
    public void clear()
    {
        items.clear();
    }

    public void addRow(Chart h)
    {
        items.add(h);
        fireListeners();
    }

    public void setChartList(ChartList c)
    {
        this.chartlist = c;
        for(Chart h : this.chartlist)addRow(h);
        fireListeners();
    }

    public ChartList getChartList(){
        return chartlist;
    }

    public Chart getRow(int row)
    {
        return items.get(row);
    }

    public int getRowCount() {
        return items.size();
    }

    public int getColumnCount() {
       return col_names.length;
    }

    public String getColumnName(int columnIndex) {
        return col_names[columnIndex];
    }

    public Class<?> getColumnClass(int columnIndex) {
       switch(columnIndex)
        {
            case 0:return Integer.class;
            case 1:return Integer.class;
            case 2:return Integer.class;
        }
       return Object.class;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Chart c = items.get(rowIndex);
        switch(columnIndex)
        {
            case 0:return c.getLevel();
            case 1:return c.getNoteCount();
            case 2:return c.getKeys();
        }
        return null;
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        throw new UnsupportedOperationException("Can't do that cowboy");
    }

    public void addTableModelListener(TableModelListener l) {
        listeners.add(l);
    }

    public void removeTableModelListener(TableModelListener l) {
       listeners.remove(l);
    }

    private void fireListeners() {
        TableModelEvent e = new TableModelEvent(this);
        fireListeners(e);
    }

    private void fireListeners(TableModelEvent e) {
        for(TableModelListener l : listeners)l.tableChanged(e);
    }
}
