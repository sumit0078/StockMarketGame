
// StartServer

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.util.*;

public class StartServer extends JFrame{
	
	
	String companies[] 		= SMG.companies; 	
	java.awt.List list 		= new java.awt.List(10,true);
	JTextField name 		= new JTextField(12);
	JTextField comp1TF 		= new JTextField(SMG.computer1);
	JTextField comp2TF 		= new JTextField(SMG.computer2);
	
	JComboBox comp1Level;
	JComboBox comp2Level;
	JComboBox gameTimeCB;
	
	JButton add  			= new JButton("           Add              ");
	JButton remove 			= new JButton("         Remove         ");
	JButton start 			= new JButton("  Start  ");
	
	JLabel comp1L			= new JLabel (" Computer Player 1");
	JLabel comp2L			= new JLabel (" Computer Player 2");
	JLabel companiesList	= new JLabel ("         Companies List");
	JLabel gameTimeL		= new JLabel (" Game Time ");
	
	JPanel motherPanel 		= new JPanel (new BorderLayout(10,10));
	JPanel northPanel 		= new JPanel (new GridLayout(1,2));
	JPanel northRightPanel 	= new JPanel (new FlowLayout(FlowLayout.CENTER,10,10));
	JPanel northLeftPanel 	= new JPanel (new BorderLayout(10,10));
	JPanel centerPanel 		= new JPanel (new GridLayout(3,3,5,10));
	JPanel southPanel		= new JPanel (new FlowLayout(FlowLayout.CENTER));
	
 	
	public StartServer(){
		
		super("Stock Market Game Server");
		
		Vector levels = new Vector(4);
		levels.add("Novice");
		levels.add("Basic");
		levels.add("Standard");
		levels.add("Expert");
		levels.add("Random");

		Vector times = new Vector(6);
		times.add("5");
		times.add("10");
		times.add("15");
		times.add("20");
		times.add("30");
		times.add("60");
		
		comp1Level	= new JComboBox(levels);
		comp2Level	= new JComboBox(levels);
		
		gameTimeCB	= new JComboBox(times);
		
		comp1TF.setForeground(Color.BLUE);
		comp1TF.setBackground(Color.LIGHT_GRAY);
		comp1TF.setEditable(false);
		comp1TF.setFocusable(false);
		
		comp2TF.setForeground(Color.BLUE);
		comp2TF.setBackground(Color.LIGHT_GRAY);
		comp2TF.setEditable(false);
		comp2TF.setFocusable(false);		
		
		northLeftPanel.add(companiesList,BorderLayout.NORTH);
		northLeftPanel.add(list,BorderLayout.CENTER);
		northRightPanel.add(new JLabel("                                         "));
		northRightPanel.add(name);
		northRightPanel.add(add);
		northRightPanel.add(remove);
		northPanel.add(northLeftPanel);
		northPanel.add(northRightPanel);
		centerPanel.add(comp1L);
		centerPanel.add(comp1TF);
		centerPanel.add(comp1Level);
		centerPanel.add(comp2L);
		centerPanel.add(comp2TF);
		centerPanel.add(comp2Level);
		centerPanel.add(gameTimeL);
		centerPanel.add(gameTimeCB);
		centerPanel.add(new JLabel("minutes"));
		southPanel.add(start);
		motherPanel.add(northPanel,BorderLayout.NORTH);
		motherPanel.add(centerPanel,BorderLayout.CENTER);
		motherPanel.add(southPanel,BorderLayout.SOUTH);
		
		for (int i = 0 ; i<companies.length ; i++)
			list.add(companies[i]);
		ButtonHandler bh = new ButtonHandler();
		
		add.addActionListener(bh);
		remove.addActionListener(bh);
		start.addActionListener(bh);
		
		getContentPane().add(motherPanel);	
		pack();
	}
	
	public static void main(String[] args){
			JFrame frame = new StartServer();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(350,350);
			frame.setVisible(true);
			///////////////////////////////////////////////////////
	}
	
	public class ButtonHandler implements ActionListener {
		
		public void actionPerformed(ActionEvent e){
			String ac = e.getActionCommand();
			
		  		if (ac.indexOf("Add")>=0){	
		  			
		  			String txt = name.getText();
		  			txt = txt.trim();
		  			if (txt.length()>0){
		  				
		  				if (list.countItems()<10){
		  					if (!listContains(list,txt))
	  							list.add(txt);
	  					}
		  				else 
			  				JOptionPane.showMessageDialog(StartServer.this,
			  						"Sorry! You cant add more than ten Companies",
			  						"Attention",JOptionPane.WARNING_MESSAGE);
			  						
		  			}
		  			name.setText("");
		  		}
		  		
		  		else if (ac.indexOf("Remove")>=0){	
		  			String sellected[] = list.getSelectedItems();		  					
		  			int i;
		  			
		  			for (i=0; i<sellected.length&&list.countItems()>3; i++)
		  				list.remove(sellected[i]); 
		  			
		  			if (i<sellected.length)
		  				JOptionPane.showMessageDialog(StartServer.this,
		  						"Sorry! Minimum No. of companies is tree",
		  						"Attention",JOptionPane.WARNING_MESSAGE);
		  			for (i=0;i<list.countItems();i++)
		  				if (list.isSelected(i))
		  					list.deselect(i);
		  			
		  		}
		  		
		  		/*******************************\
		  		*********start server************
		  		\*******************************/
		  		else if (ac.indexOf("Start")>=0){	
					try{
						
						new SMGServerImpl(list.getItems(), 
										  comp1Level.getSelectedIndex()+1,
										  comp2Level.getSelectedIndex()+1,
										  new Integer((String)gameTimeCB.getSelectedItem()).intValue());
						
						StartServer.this.hide();
						
					}catch (java.rmi.RemoteException ex){
						System.err.println("Failure with RMI server: " + ex.getMessage());
					}
				}        	
		}
	
		public boolean listContains(java.awt.List list, String item){
			int i,count;
			count=list.getItemCount();
			for(i=0;i<count;i++)
				if (list.getItem(i).equals(item))
					return true;
			
			return false;		
		}
	
	}
}