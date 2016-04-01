
// SMGRegister 

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.util.*;

public class SMGRegister extends JFrame {

	public static final String computer1 = "Comp1";
	public static final String computer2 = "Comp2";
	
	public SMGServer smg;
	public SMGClient client;
	public SMGClientImpl clientImpl;
	
	JTextField tf = new JTextField(13);
	JButton b = new JButton("      Register      ");
	
	public SMGRegister(SMGServer smg, SMGClient client, SMGClientImpl clientImpl) {
		super("Register");
		this.smg = smg;
		this.client = client;
		this.clientImpl = clientImpl;
				
		ActionHandler ah = new ActionHandler();
		
		b.addActionListener(ah);
		tf.addActionListener(ah);
		
		JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER,20,20));
		
		p.add(new JLabel (" Welcome to the Stock Market Game"));
		p.add(new JLabel ("Name: "));
		p.add(tf);
		p.add(b);
		p.setBackground(new Color(230,230,255));
		getContentPane().add(p);
		setResizable(false);
		pack();
	}
	public void hideFrame(){
		this.hide();	
	}
/*
	public static void main(String[] args) {
		JFrame frame = new SMGRegister();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(270,180);
		frame.setVisible(true);
		
	}
	
*/	
	class ActionHandler implements ActionListener {

  		public void actionPerformed(ActionEvent e){
			try{
				String playerName = tf.getText();
				
				if(playerName.trim().length() != 0){
				
					if(!(   playerName.equalsIgnoreCase(computer1) 
					     || playerName.equalsIgnoreCase(computer2))){
					
						int flag = smg.register(client, playerName);
						if(flag == 0){
							//////////////////
							JFrame frame = new SMGame(smg, playerName, clientImpl);
							frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
							frame.setSize(590,630);
							frame.setVisible(true);
							///////////////////
							hideFrame();
							
						}
						else if(flag == -1)
							JOptionPane.showMessageDialog(SMGRegister.this, 
				  				"Sorry! There is enough players in the game\n please try later...", "Error",JOptionPane.ERROR_MESSAGE);
				  		
						else if(flag == -2)
							JOptionPane.showMessageDialog(SMGRegister.this, 
				  				"Sorry! This name is used...", "Error",JOptionPane.ERROR_MESSAGE);
				  	}
				  	else
				  		JOptionPane.showMessageDialog(SMGRegister.this, 
				  				"Sorry! This name is reserved...", "Error",JOptionPane.ERROR_MESSAGE);
				 } 		
			}catch(java.rmi.RemoteException ex){
				System.out.println(ex.getMessage());	
			}
  		}
 	}
}
