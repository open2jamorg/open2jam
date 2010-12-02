package org.open2jam.gui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.open2jam.parser.Chart;

/**
 *
 * @author fox
 */
public class ChartTableModel implements TableModel
{
    private List<Chart> items;
    private String[] col_names = new String[] { "Name", "Level", "Genre" };
    private int rank;
    
    private List<TableModelListener> listeners;

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

    public void setRank(int rank)
    {
        this.rank = rank;
        fireListeners();
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
            case 0:return String.class;
            case 1:return Integer.class;
            case 2:return String.class;
        }
       return Object.class;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        switch(columnIndex)
        {
            case 0:return items.get(rowIndex).getTitle();
            case 1:return items.get(rowIndex).getLevel(rank);
            case 2:return items.get(rowIndex).getGenre();
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
