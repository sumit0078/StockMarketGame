import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.*;

public class Table{

	private DefaultTableModel tm;
	private JTable tbl;

	public Table(String[] arr){  
    	tm = new DefaultTableModel();      
      
      	for (int i=0;i<arr.length;i++)
      		tm.addColumn(arr[i]);
	}
    
    public void addRow(String[] row){
    	
    	tm.addRow(row);
    }
	public void addRow(int i, String[] row){
    	
    	tm.insertRow(i,row);
    }
	public void clear(){
    	int count = tm.getRowCount();
    	for (int i= 0; i <count ; i++)
    		tm.removeRow(0);
    }
    
    public JTable getTable(){
 
    	tbl = new JTable(tm); 
    	tbl.disable();
    	tbl.setFocusable(false);
    	return tbl; 
    }
    
    public void setRowCount(int rc){
    	tm.setRowCount(rc);	
    }
}
