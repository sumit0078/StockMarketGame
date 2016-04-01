
// SMGame

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.util.*;

public class SMGame extends JFrame{
	
	private final String selling = "sell";
	private final String buying = "buy";											
	private final float maxDebit = 1000;
	public static final int timeScale = 15;
	private final int scale = 3;
										
	private SMGServer smg;
	private String player;
	private int i;
	private int gameTime;
	private int currentTime;
	private String [] symbols;
	private JTabbedPane tab;
	private String marquee = "";
	
	private Hashtable stocksPrices;
	private Hashtable winners;
	
	JTextField marqueeTF = new JTextField(40);	
	PaintCanvas pc = new PaintCanvas();	
	Thread marqueeMotion;
	/****  Trade panel Variables ***************************/

	JTextArea winnersList		= new JTextArea(11,12);
	
	JComboBox  symbolCBTrade 	= new JComboBox();
	JComboBox  bidsCB			= new JComboBox();
	JTextField playerTime 		= new JTextField(8);
	JTextField sharePrice 		= new JTextField(10);
	JTextField availableShares 	= new JTextField(10);
	JTextField borrowRepay 		= new JTextField(10);	
	JTextField lowerPrice 		= new JTextField(10);	
	JTextField upperPrice 		= new JTextField(10);	
	JTextField cash				= new JTextField(10);	
	JTextField shares			= new JTextField(10);	
	JTextField debit			= new JTextField(10);	
	JTextField total			= new JTextField(10);	
	JTextField tradeShares		= new JTextField(10);	
	JTextField tradePrice		= new JTextField(10);	
	JTextArea  actions			= new JTextArea("", 10, 34);
	
	JLabel playerName 	= new JLabel("");
	JLabel win 			= new JLabel("   Player   |   Score   ");
	
	JButton borrow 		= new JButton("Borrow");
	JButton repay 		= new JButton("Repay");
	JButton setAlert	= new JButton("    Set Alert     ");
	JButton removeAlert	= new JButton("Remove Alert");
	JButton sell		= new JButton("     Sell    ");
	JButton buy 		= new JButton("     Buy      ");
	JButton undoSellBuy = new JButton("Undo");
	 
	/****  Chart panel Variables ***************************/

	JComboBox symbolCBChart = new JComboBox();
	JButton drawChart 		= new JButton("Draw Chart");

	/****  Position panel Variables ************************/	

	String[] positionTableHeaders = {"Stock Symbol","Shares","Current Price"};
	Table positionTable = new Table(positionTableHeaders);
	
	JTextField pAvailableCash  	= new JTextField(10);
	JTextField pTotalShares    	= new JTextField(10);
	JTextField pCurValOfShares 	= new JTextField(10);
	JTextField pDebit		  	= new JTextField(10);
	JTextField pTotalValue	  	= new JTextField(10);

	/****  History panel Variables *************************/

	String[] historyTableHeaders = {"Time","Trading Type","Symbol","# of Shares","Trading Price"};
	Table historyTable = new Table(historyTableHeaders);
	
	/*******************************************************/
	
	// Constructor
	public SMGame(SMGServer smg, String player, SMGClientImpl client) throws RemoteException{
		
		super("Stock Market Game");
		winners = new Hashtable();
		
		this.smg = smg;
		this.player = player;
//		this.client = client;
		client.setSMGame(this);

		final String thisPlayer = player;
		final SMGServer thisSMG = smg;
	
		playerName.setText(player);	
		
		stocksPrices = smg.getAllStockQuotes();	
		symbols = smg.getCompaniesNames();
		refreshMarquee(stocksPrices);
		smg.refreshWinnersList();
		//////////////////////////////////
		refreshBalance(smg.getAccount(player), smg.getPlayerSharesPrice(player)); 
				
		gameTime = smg.getGameTime();
		currentTime = gameTime;
		playerTime.setText((new Integer(currentTime/60)).toString()+":00");
		
		//////******* Timer ********////////
		Thread time = new Thread() {

			  String sec;
			  String min;
			  		  
			  public void run() {
			  	try{
			  		while(currentTime-- != 0){
			  			
			  			if (currentTime ==  gameTime*2/3)
			  				playerTime.setForeground(Color.yellow);
					  	else if (currentTime ==  gameTime/3)
			  				playerTime.setForeground(Color.red);
					  	
					  	// sleep a second
					  	sleep(1000);
						
						min = new Integer(currentTime/60).toString();
						min = min.length() ==2? min : "0"+min;
						
						sec = new Integer(currentTime%60).toString();
						sec = sec.length() ==2? sec : "0"+sec;
							
					  	playerTime.setText(min + ":" + sec);
					}
					
					marqueeMotion.stop();
					refreshPosition();
					thisSMG.exit(thisPlayer);
					goodbye();
					
				}
			  	catch(InterruptedException e){	
			  		System.out.println(e.getMessage());
			  	}
			  	catch(RemoteException e){	
			  		System.out.println(e.getMessage());
			  	}
			  }
		};
		time.start();
		
		marqueeTF.setFont(new Font("Courier New",Font.BOLD,16));
		marqueeTF.setBackground(new Color(0,0,0));
		marqueeTF.setForeground(new Color(0,255,0));
		marqueeTF.setEditable(false);
		marqueeTF.setFocusable(false);
		marqueeTF.setText(marquee);
		
		playerName.setFont(new Font("Tahoma",Font.BOLD,16));
		playerName.setForeground(new Color(200,0,0));

		/****  Trade panel ***************************************/
		
		//***  Action Listener ******************************************/
		ButtonEventHandler beh = new ButtonEventHandler();
		
		borrow.addActionListener(beh);
		repay.addActionListener(beh);
		setAlert.addActionListener(beh);
		removeAlert.addActionListener(beh);
		sell.addActionListener(beh);
		buy.addActionListener(beh);
		undoSellBuy.addActionListener(beh);
	
		symbolCBTrade.addPopupMenuListener(new TradeSymbolPopupMenuListener());
		bidsCB.addPopupMenuListener(new BidPopupMenuListener());
		
		tradeShares.addKeyListener(new TradeSharesKeyListener());
		tradePrice.addKeyListener(new TradePriceKeyListener());
			
		//********* Shapes  *************************************/
		
		symbolCBTrade.setBackground(Color.white);
		symbolCBTrade.setForeground(new Color(0,0,0));
		
		for (i=0;i<symbols.length;i++)
			symbolCBTrade.addItem(symbols[i]);
		
		cash.setEditable(false);
		debit.setEditable(false);
		shares.setEditable(false);
		total.setEditable(false);
		sharePrice.setEditable(false);
		availableShares.setEditable(false);
		
		cash.setBackground(Color.LIGHT_GRAY);
		shares.setBackground(Color.LIGHT_GRAY);
		debit.setBackground(Color.LIGHT_GRAY);
		total.setBackground(Color.LIGHT_GRAY);
		sharePrice.setBackground(Color.LIGHT_GRAY);
		availableShares.setBackground(Color.LIGHT_GRAY);
					
		cash.setFocusable(false);
		shares.setFocusable(false);
		debit.setFocusable(false);
		total.setFocusable(false);
		sharePrice.setFocusable(false);
		availableShares.setFocusable(false);
		
		bidsCB.setBackground(Color.white);
		bidsCB.setForeground(new Color(0,0,0));
		bidsCB.setMaximumRowCount(3);
		
		win.setFont(new Font("Tahoma",Font.BOLD,12));
		win.setForeground(Color.yellow);
		win.setBackground(Color.black);
		win.setBorder(new BevelBorder (BevelBorder.RAISED));
		
		winnersList.setFont(new Font("Courier New",Font.TRUETYPE_FONT,13));
		winnersList.setForeground(Color.black);
		winnersList.setBackground(new Color(255,255,245));
		winnersList.setBorder(new BevelBorder (BevelBorder.RAISED));
		winnersList.setEditable(false);
		winnersList.setFocusable(false);
		
		playerTime.setFont(new Font("Display 1",Font.BOLD,18));
		playerTime.setForeground(new Color(0,255,0));
		playerTime.setBackground(new Color(0,0,0));
		playerTime.setHorizontalAlignment(playerTime.CENTER);
		playerTime.setEditable(false);
		playerTime.setFocusable(false);
		
		actions.setFont(new Font("Tahoma",Font.TRUETYPE_FONT,14));
		actions.setEditable(false);
		actions.setFocusable(false);
		actions.setBackground(new Color(255,255,255));
		actions.setForeground(new Color(150,0,0));
		undoSellBuy.setMargin(new Insets(2,4,2,4));
		
		///////////////////////
		sharePrice.setText(new Float(smg.getStockQuote((String)symbolCBTrade.getSelectedItem())).toString());
		availableShares.setText(new Float(smg.getAvailableSharesForTrade((String)symbolCBTrade.getSelectedItem())).toString());
		//////////////////////
		
		//**************************************************************/		
		JPanel tradePanel 				= new JPanel(new BorderLayout());
		JPanel tradeNorthPanel 			= new JPanel(new BorderLayout());
		JPanel tradeNorthRightPanel 	= new JPanel(new FlowLayout());
		JPanel tradeNorthLeftPanel 		= new JPanel(new FlowLayout());
		JPanel tradeSouthPanel 			= new JPanel(new BorderLayout(5,5));
		JPanel tradeCenterPanel 		= new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel tradeCenterLeftPanel 	= new JPanel(new GridLayout(3,1));
		JPanel tradeCenterLeftPanel1 	= new JPanel(new GridLayout(2,1));
		JPanel tradeCenterLeftPanel1up 	= new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
		JPanel tradeCenterLeftPanel1dn 	= new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
		JPanel tradeCenterLeftPanel2 	= new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
		JPanel tradeCenterLeftPanel2Left= new JPanel(new GridLayout(2,1,0,10));
		JPanel tradeCenterLeftPanel3 	= new JPanel(new GridLayout(2,1));
		JPanel tradeCenterLeftPanel3up 	= new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
		JPanel tradeCenterLeftPanel3dn 	= new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
		JPanel tradeCenterRightPanel 	= new JPanel(new GridLayout(2,1));
		JPanel tradeCenterRightPanel1 	= new JPanel(new GridLayout(4,1));
		JPanel tradeCenterRightPanel11 	= new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
		JPanel tradeCenterRightPanel12 	= new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
		JPanel tradeCenterRightPanel13 	= new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
		JPanel tradeCenterRightPanel14 	= new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));		
		JPanel tradeCenterRightPanel2 	= new JPanel(new GridLayout(4,1,0,2));
		JPanel tradeCenterRightPanel21 	= new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
		JPanel tradeCenterRightPanel22 	= new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
		JPanel tradeCenterRightPanel23 	= new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
		JPanel tradeCenterRightPanel24 	= new JPanel(new BorderLayout(5,0));
			
		tradeCenterLeftPanel1.setBorder (new CompoundBorder( new TitledBorder(
										 new EtchedBorder()," Quote ")
										,new EmptyBorder(1, 5, 5, 5)));
		tradeCenterLeftPanel2.setBorder (new CompoundBorder( new TitledBorder(
										 new EtchedBorder()," Bank ")
										,new EmptyBorder(1, 5, 5, 5)));
		tradeCenterLeftPanel3.setBorder (new CompoundBorder( new TitledBorder(
										 new EtchedBorder()," Alert ")
										,new EmptyBorder(1, 5, 5, 5)));
		tradeCenterRightPanel1.setBorder(new CompoundBorder( new TitledBorder(
										 new EtchedBorder()," Balance Sheet ")
										,new EmptyBorder(1, 5, 5, 5)));
		tradeCenterRightPanel2.setBorder(new CompoundBorder( new TitledBorder(
										 new EtchedBorder()," Trade ")
										,new EmptyBorder(1, 5, 5, 5)));
		
		tradeNorthLeftPanel.add(new JLabel("   Symbol"));
		tradeNorthLeftPanel.add(symbolCBTrade);
		
		tradeNorthRightPanel.add(playerName);
		tradeNorthRightPanel.add(new JLabel("    "));			
		tradeNorthRightPanel.add(playerTime);	
		tradeNorthRightPanel.add(new JLabel("Minutes    "));	
		
		tradeNorthPanel.add(tradeNorthLeftPanel,BorderLayout.WEST);	
		tradeNorthPanel.add(tradeNorthRightPanel,BorderLayout.EAST);	
				
		tradeCenterLeftPanel1up.add(new JLabel("Share Price"));
		tradeCenterLeftPanel1up.add(sharePrice);
		tradeCenterLeftPanel1up.add(new JLabel("$"));
		tradeCenterLeftPanel1dn.add(new JLabel("Available     "));
		tradeCenterLeftPanel1dn.add(availableShares);
		tradeCenterLeftPanel1dn.add(new JLabel("Shares"));
		tradeCenterLeftPanel1.add(tradeCenterLeftPanel1up);
		tradeCenterLeftPanel1.add(tradeCenterLeftPanel1dn);
		tradeCenterLeftPanel.add(tradeCenterLeftPanel1);
		
		tradeCenterLeftPanel2Left.add(borrow);
		tradeCenterLeftPanel2Left.add(repay);
		tradeCenterLeftPanel2.add(tradeCenterLeftPanel2Left);
		tradeCenterLeftPanel2.add(borrowRepay);
		tradeCenterLeftPanel2.add(new JLabel("$"));
		tradeCenterLeftPanel.add(tradeCenterLeftPanel2);
				
		tradeCenterLeftPanel3up.add(new JLabel("L. Price"));
		tradeCenterLeftPanel3up.add(lowerPrice);
		tradeCenterLeftPanel3up.add(new JLabel("$"));
		tradeCenterLeftPanel3up.add(setAlert);
		tradeCenterLeftPanel3dn.add(new JLabel("U. Price"));
		tradeCenterLeftPanel3dn.add(upperPrice);
		tradeCenterLeftPanel3dn.add(new JLabel("$"));
		tradeCenterLeftPanel3dn.add(removeAlert);
		tradeCenterLeftPanel3.add(tradeCenterLeftPanel3up);
		tradeCenterLeftPanel3.add(tradeCenterLeftPanel3dn);
		tradeCenterLeftPanel.add(tradeCenterLeftPanel3);
		

		tradeCenterRightPanel11.add(new JLabel("Cash    "));
		tradeCenterRightPanel11.add(cash);
		tradeCenterRightPanel11.add(new JLabel("$"));
		tradeCenterRightPanel12.add(new JLabel("Shares"));
		tradeCenterRightPanel12.add(shares);
		tradeCenterRightPanel12.add(new JLabel("$"));
		tradeCenterRightPanel13.add(new JLabel("Debits  "));
		tradeCenterRightPanel13.add(debit);
		tradeCenterRightPanel13.add(new JLabel("$"));
		tradeCenterRightPanel14.add(new JLabel("Total     "));
		tradeCenterRightPanel14.add(total);
		tradeCenterRightPanel14.add(new JLabel("$"));
		tradeCenterRightPanel1.add(tradeCenterRightPanel11);
		tradeCenterRightPanel1.add(tradeCenterRightPanel12);
		tradeCenterRightPanel1.add(tradeCenterRightPanel13);
		tradeCenterRightPanel1.add(tradeCenterRightPanel14);
		tradeCenterRightPanel.add(tradeCenterRightPanel1);
		tradeCenterRightPanel21.add(tradeShares);
		tradeCenterRightPanel21.add(new JLabel("Shares"));
		tradeCenterRightPanel22.add(tradePrice);
		tradeCenterRightPanel22.add(new JLabel("$"));
		tradeCenterRightPanel23.add(sell);
		tradeCenterRightPanel23.add(buy);
		tradeCenterRightPanel24.add(bidsCB,BorderLayout.CENTER);
		tradeCenterRightPanel24.add(undoSellBuy,BorderLayout.EAST);
		tradeCenterRightPanel2.add(tradeCenterRightPanel21);
		tradeCenterRightPanel2.add(tradeCenterRightPanel22);
		tradeCenterRightPanel2.add(tradeCenterRightPanel23);
		tradeCenterRightPanel2.add(tradeCenterRightPanel24);
		tradeCenterRightPanel.add(tradeCenterRightPanel2);
		
		actions.append("");
		tradeSouthPanel.add(new JScrollPane(actions),BorderLayout.CENTER);
		
		JPanel winnerPanel = new JPanel(new BorderLayout());
		

		winnerPanel.add(win,BorderLayout.NORTH);
		winnerPanel.add(winnersList,BorderLayout.CENTER);
		winnerPanel.setBorder(new BevelBorder (BevelBorder.LOWERED));
		winnerPanel.setBackground(Color.black);
		
		tradeSouthPanel.add(winnerPanel,BorderLayout.EAST);
		tradeCenterPanel.add(tradeCenterLeftPanel);
		tradeCenterPanel.add(tradeCenterRightPanel);		
		tradePanel.add(tradeNorthPanel ,BorderLayout.NORTH);
		tradePanel.add(tradeSouthPanel ,BorderLayout.SOUTH);
		tradePanel.add(tradeCenterPanel,BorderLayout.CENTER);	
		
		/*********************************************************/
		/****  chart panel ***************************************/
		
		drawChart.addActionListener(beh);
		symbolCBChart.addPopupMenuListener(new ChartSymbolPopupMenuListener());
		
		symbolCBChart.setBackground(Color.white);
		symbolCBChart.setForeground(new Color(0,0,0));
		for (i=0;i<symbols.length;i++)
			symbolCBChart.addItem(symbols[i]);

		JPanel chartPanel = new JPanel(new BorderLayout());
		JPanel chartNorthPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		chartNorthPanel.add(new JLabel("symbol"));
		chartNorthPanel.add(symbolCBChart);
		chartNorthPanel.add(drawChart);
		
		chartPanel.add(chartNorthPanel,BorderLayout.NORTH);
		chartPanel.add(pc,BorderLayout.CENTER);
		
		/*********************************************************/
		/****  Position panel ************************************/
		// shapes ///
		pAvailableCash.setEditable(false);
		pTotalShares.setEditable(false);
		pCurValOfShares.setEditable(false);
		pDebit.setEditable(false);
		pTotalValue.setEditable(false);
		
		pAvailableCash.setFocusable(false);
		pTotalShares.setFocusable(false);
		pCurValOfShares.setFocusable(false);
		pDebit.setFocusable(false);
		pTotalValue.setFocusable(false);
		
		pAvailableCash.setBackground(Color.LIGHT_GRAY);
		pTotalShares.setBackground(Color.LIGHT_GRAY);
		pCurValOfShares.setBackground(Color.LIGHT_GRAY);
		pDebit.setBackground(Color.LIGHT_GRAY);
		pTotalValue.setBackground(Color.LIGHT_GRAY);	
		////////////////////////////////////////////////////////
				
		JPanel positionPanel = new JPanel(new BorderLayout());
		JPanel positionNorthPanel = new JPanel(new BorderLayout());
		JPanel positionNorthNorthPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JPanel positionNorthCenterPanel = new JPanel(new GridLayout(6,4));
		
		positionNorthNorthPanel.add(new JLabel("   "));

		positionNorthCenterPanel.add(new JLabel("  Available Cash"));
		positionNorthCenterPanel.add(pAvailableCash);
		positionNorthCenterPanel.add(new JLabel("  $"));
		positionNorthCenterPanel.add(new JLabel(""));
		positionNorthCenterPanel.add(new JLabel("  Total Shares"));
		positionNorthCenterPanel.add(pTotalShares);
		positionNorthCenterPanel.add(new JLabel("  Shares"));
		positionNorthCenterPanel.add(new JLabel(""));
		positionNorthCenterPanel.add(new JLabel("  Current Value of Shares"));
		positionNorthCenterPanel.add(pCurValOfShares);
		positionNorthCenterPanel.add(new JLabel("  $"));
		positionNorthCenterPanel.add(new JLabel(""));
		positionNorthCenterPanel.add(new JLabel("  Debit"));
		positionNorthCenterPanel.add(pDebit);
		positionNorthCenterPanel.add(new JLabel("  $"));
		positionNorthCenterPanel.add(new JLabel(""));
		positionNorthCenterPanel.add(new JLabel("  Total Value:"));
		positionNorthCenterPanel.add(pTotalValue);
		positionNorthCenterPanel.add(new JLabel("  $"));

		positionNorthPanel.add(positionNorthNorthPanel,BorderLayout.NORTH);
		positionNorthPanel.add(positionNorthCenterPanel,BorderLayout.CENTER);
		
		positionPanel.add(positionNorthPanel,BorderLayout.NORTH);
			
		positionPanel.add(new JScrollPane(positionTable.getTable()),BorderLayout.CENTER);

		/*********************************************************/
		/****  History panel *************************************/
		
		JPanel historyPanel = new JPanel(new BorderLayout(20,20));
		JPanel historyNorthPanel= new JPanel(new FlowLayout(FlowLayout.CENTER));
		historyNorthPanel.add(new JLabel("  "));
		historyPanel.add(historyNorthPanel,BorderLayout.NORTH);
		
		historyPanel.add(new JScrollPane(historyTable.getTable()),BorderLayout.CENTER);
	
		
		/*********************************************************/
		
		JPanel panel  = new JPanel(new BorderLayout());
		
		tab = new JTabbedPane();
		tab.addTab("Trade",		tradePanel);
		tab.addTab("Chart", 	chartPanel);
		tab.addTab("Position", 	positionPanel);
		tab.addTab("History", 	historyPanel);
		tab.addChangeListener(new TabChangeListener());
		
		panel.add(tab,BorderLayout.CENTER);
		panel.add(marqueeTF,BorderLayout.SOUTH);
		
		marqueeMotion = new Thread() {
	      	public void run() {
	      		String marquee1;
	      		int i;	      		
	      		while(true){
	      			marquee1 = marquee;
	      			for (i=0; i < marquee1.length(); i++){
	      				
	      				marqueeTF.setText(marquee1.substring(i) + marquee);
	      				try{	
							Thread.sleep(100);
						}catch(InterruptedException e){}					      				
	      			}
	      		}	
	    	}	
	    };
	    marqueeMotion.start();
	    
	    JMenuBar menuBar = createMenuBar();
		setJMenuBar(menuBar);
	    		
		getContentPane().add(panel);
		setResizable(false);
		this.addWindowListener(new WL());
		pack();
	}
	
	
	//***********************************************************************//
	
	// show msg box
	public void msgBox(String msg){
		
		if(msg.equals("invalid_no"))
			JOptionPane.showMessageDialog(SMGame.this, 
		  		"Invalid number!", "Error",JOptionPane.ERROR_MESSAGE);

		else if(msg.equals("out_of_range"))
			JOptionPane.showMessageDialog(SMGame.this, 
			    "Nnumber out of range!", "Error",JOptionPane.ERROR_MESSAGE);
	
		else if(msg.equals("cash_shortage"))
			JOptionPane.showMessageDialog(SMGame.this, 
		  		"Sorry! You don't have enough cash", "Error",JOptionPane.ERROR_MESSAGE);

		else if(msg.equals("shares_shortage"))
			JOptionPane.showMessageDialog(SMGame.this, 
		  		"Sorry! You don't have enough shares...", "Error",JOptionPane.ERROR_MESSAGE);

		else if(msg.equals("debit_overflow"))
			JOptionPane.showMessageDialog(SMGame.this, 
				"Sorry! Your debit has exceeded the max available value - "+ maxDebit + "$ \n"
				+"You may start repaying...", "Error",JOptionPane.ERROR_MESSAGE);
		
		else if(msg.equals("zero_no"))
			JOptionPane.showMessageDialog(SMGame.this, 
		  		"Please, Enter a nonzero value...", "Error",JOptionPane.ERROR_MESSAGE);
		
		else if(msg.equals("no_debit"))
		  	JOptionPane.showMessageDialog(SMGame.this, 
		  		"You have no debits to repay...", "Attention",JOptionPane.INFORMATION_MESSAGE);
	
		else if(msg.equals("invert_ul"))
			JOptionPane.showMessageDialog(SMGame.this, 
		  		"Sorry! Upper value should be larger than lower...", "Error",JOptionPane.ERROR_MESSAGE);
	
	}
	
	// show msg box
	public void msgBox(String msg, float val){
		
		if(msg.equals("limited_borrow"))
			JOptionPane.showMessageDialog(SMGame.this,
					"Sorry! You can borrow only " + val + "$",
					"Error",JOptionPane.ERROR_MESSAGE);
		
		else if(msg.equals("limitd_repay"))
			JOptionPane.showMessageDialog(SMGame.this,
					"You has only repayed your debits - " + val + "$",
					"Attention",JOptionPane.INFORMATION_MESSAGE);
					
		else if(msg.equals("alert_rejected"))
			JOptionPane.showMessageDialog(SMGame.this,
					"Your alert has rejected, current price is " + val + "$",
					"Attention",JOptionPane.WARNING_MESSAGE);
						
	}		
  	
  	// show msg box
	public void alertMsgBox(String msgText){
		
		JOptionPane.showMessageDialog(SMGame.this,msgText,"Alert",JOptionPane.WARNING_MESSAGE);	
	}
	
  	// show exit msg box
	public void goodbye(){
		
		float money = getFloatValue(total.getText());
			if(money > 1000)
				JOptionPane.showMessageDialog(SMGame.this, 
		  				"Congratulations, You earned "+money+"$", "Goodbye",
		  				JOptionPane.PLAIN_MESSAGE);
			else
				JOptionPane.showMessageDialog(SMGame.this, 
		  				"Sorry! Your luck was bad...You may try again", "Goodbye",
		  				JOptionPane.PLAIN_MESSAGE);
	}
	
	// refreshAccountCB
	public void refreshBalance(Account account, float playerSharesPrice) 
		throws RemoteException{
		
		cash.setText(new Float(account.getCash()).toString());
		debit.setText(new Float(account.getDebit()).toString());
		shares.setText(new Float(playerSharesPrice).toString());
		total.setText(new Float(account.getCash() - account.getDebit() + playerSharesPrice).toString());
	}
	
	// refreshWinwersListCB
	public void refreshWinnersListCB(String winners){
		winnersList.setText(winners.substring(0,winners.length()-1));
	}
	
	// add action
	public void addAction(String stm){
		actions.append(stm);
		actions.setSelectionStart(actions.getText().length());
	}
	
	// add a record to history 
	public void addToHistory(String symbol, String type, int sharesNo, float sharePrice){
		
		String [] row = new String[5];

		String min = new Integer((gameTime -1 - currentTime)/60).toString();
		min = min.length() ==2? min : "0"+min;	
		String sec = new Integer(59 - currentTime%60).toString();
		sec = sec.length() ==2? sec : "0"+sec;
		
		row[0] = min + ":" + sec;					
		row[1] = type;
		row[2] = symbol;
		row[3] = new Integer(sharesNo).toString();
		row[4] = new Float(sharePrice).toString();
		historyTable.addRow(row);
	}
	
	// refreshMarquee
	public void refreshMarquee(Hashtable stocksPrices){
		
		String temp = "";
		float oldPrice, newPrice;
		float difference;
		
		for (int i=0; i < symbols.length; i++){
			temp  += symbols[i] + " ";
				
			oldPrice = ((Float)(this.stocksPrices.get(symbols[i]))).floatValue();
			newPrice = ((Float)(stocksPrices.get(symbols[i]))).floatValue();
				 
			difference = newPrice - oldPrice;

			temp += trunc(newPrice,scale) + " ";

			if (difference >= 0)
				temp += "+" + trunc(difference,scale);
			
			else 
				temp += trunc(difference,scale);
			
			temp += "   ";
		}
		
		marquee = temp;
		this.stocksPrices = stocksPrices;
	}
	
	String trunc(float val,int length){
		
		String result = new Float(val).toString();
		int i;
		
		for(i=0; i < result.length() && result.charAt(i) != '.'; i++);
		
		if (i+length>=result.length())
			return result;
		else 
			return result.substring(0,i+length+1);
	}
	
	public void refreshPosition() throws RemoteException{
		float totalPrice;
		int sharesNo;
		int shares;
		float price;
		String symbol;
		String [] row;
		Enumeration en;
		Account a;
		Hashtable ps;

		totalPrice = 0; 
		sharesNo = 0;
		ps = smg.getPlayerShares(player);
		en = ps.keys();
		row = new String[3];
		
		// get player account
		a = smg.getAccount(player);
		
		positionTable.clear();

		// getPlyaer SharesNo and totalSharesPrices
		while(en.hasMoreElements()){
			symbol = (String)en.nextElement();
			shares = ((Integer)ps.get(symbol)).intValue(); 
			price = ((Float)stocksPrices.get(symbol)).floatValue();
			
			totalPrice += (float)shares * price;
			sharesNo += shares;	
			
			if (shares>0){
//				row = new String[3];
				row[0] = symbol;
				row[1] = new Integer(shares).toString();
				row[2] = new Float(price).toString();
				positionTable.addRow(row);
			}
		}
		positionTable.setRowCount(symbols.length);						
		
		// set text fields
		pAvailableCash.setText(new Float(a.getCash()).toString());
		pDebit.setText(new Float(a.getDebit()).toString());						
		pTotalShares.setText(new Integer(sharesNo).toString());
		pCurValOfShares.setText(new Float(totalPrice).toString());
		pTotalValue.setText(new Float(totalPrice + a.getCash() - a.getDebit()).toString());
	}
	
	// menu
	private JMenuBar createMenuBar()
	{
		
		
		JMenuBar menuBar = new JMenuBar();
		JMenu game = new JMenu("Game");
		game.setMnemonic('g');

		JMenuItem exitGame = new JMenuItem("Exit Game");
		exitGame.setMnemonic('x');
	
		JMenu help = new JMenu("Help");
		help.setMnemonic('h');

		JMenuItem instructions = new JMenuItem("Instructions");
		instructions.setMnemonic('i');
		
		JMenuItem about = new JMenuItem("About");
		about.setMnemonic('a');
		
		ActionListener menuAction = new ActionListener() { 
			public void actionPerformed(ActionEvent e){
				String ac = e.getActionCommand();

				if (ac.equals("Exit Game")){
					try{
						if(currentTime >= 0)
							smg.exit(player);
					}
					catch(RemoteException ex){	
						System.out.println(ex.getMessage());
					}
					System.exit(0);
				}

				else if (ac.equals("Instructions")){
					
				}
				else if (ac.equals("About")){
					JLabel aboutLabel = new JLabel(new ImageIcon( "about.gif"));
					JOptionPane.showMessageDialog(SMGame.this,aboutLabel,"About",JOptionPane.PLAIN_MESSAGE);
				}
			}
		};
		
		
		exitGame.addActionListener(menuAction);
		instructions.addActionListener(menuAction);
		about.addActionListener(menuAction);
	
		game.add(exitGame);
		help.add(instructions);
		help.add(about);
		
		menuBar.add(game);
		menuBar.add(help);

		return menuBar;
	}
	
	// isValidFloat
	boolean isValidFloat(String floatStr){
		boolean dot = false;
		if (floatStr.trim().length()==0)
			return false;
		else {
			int length = floatStr.length();
			int i;
			char ch;
			for (i=0; i<length; i++){
				ch = floatStr.charAt(i);
				if (ch == '.' )
					if (dot)
						return false;
					else 
						dot = true;
				
				else if (ch!='0'&&ch!='1'&&ch!='2'&&ch!='3'&&ch!='4'&&ch!='5'&&ch!='6'&&ch!='7'&&ch!='8'&&ch!='9')
					return false;
			}
			return true;
		}
	}
	
	// getFloatValue
	float getFloatValue (String floatStr){
		return new Float(floatStr).floatValue();
	}
	
	// isValidInteger
	boolean isValidInteger(String integerStr){
		
		if (integerStr.trim().length()==0)
			return false;
		else {
			int length = integerStr.length();
			int i;
			char ch;
			for (i=0; i<length; i++){
				ch = integerStr.charAt(i);
				if (ch!='0'&&ch!='1'&&ch!='2'&&ch!='3'&&ch!='4'&&ch!='5'&&ch!='6'&&ch!='7'&&ch!='8'&&ch!='9')
					return false;
			}
			return true;
		}
	}
	
	// getIntegerValue
	int getIntegerValue (String integerStr){
		return new Integer(integerStr).intValue();
	}
	
	
	//***********************************************************************//
	class PaintCanvas extends JPanel{
		float[] chart={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	  	public PaintCanvas() {
	    	super();
	    	setOpaque(true);			//ÇááÇ ÔÝÇÝíÉ==ãÕãÊ
	    	setBackground(Color.white);
	  	}
	  	
	  	// overidden method called when "repaint()" called and at startup
		public void paintComponent(Graphics g) {
	    	super.paintComponent(g);
	    	int x0 = 100;
	    	int y0 = 400;
	    	int j;
	    	int base = 0;
	    	int step = 10;
	    	
	    	g.setColor(Color.black);
			g.setFont(new Font("Tahoma",Font.BOLD,18));
			
			g.drawString("Chart for last 15 minutes...",150,60);
			g.drawLine(x0-10,y0,x0+410,y0);
			g.drawLine(x0,y0+10,x0,y0-310);
			for (i=0;i<=20;i++)
				g.drawLine(x0-3,y0-i*15,x0,y0-i*15);
			
			for (i=0;i<=15;i++)
				g.drawLine(x0+i*25,y0,x0+i*25,y0+3);

			// Grid // 
			g.setColor(new Color(230,230,230));
			for (i=1;i<=10;i++)
				g.drawLine(x0+1,y0-i*30,x0+410,y0-i*30);
			
			for (i=1;i<=15;i++)
				g.drawLine(x0+i*25,y0-1,x0+i*25,y0-310);

			/////////
			g.setColor(Color.black);
			g.setFont(new Font("Tahoma",Font.TRUETYPE_FONT,10));
			
			for (i=0;i<=15;i++)
				g.drawString(new Integer(i).toString(),95+i*25,420);
			
			g.setFont(new Font("Tahoma",Font.BOLD,14));
			g.drawString("Price ($)",70,80);
			g.setFont(new Font("Tahoma",Font.BOLD,12));
			g.drawString("Time (min)",500,420);
			
			for (j=0;j<15&&chart[j]==0;j++);
			
			switch (getCase(chart)){
				
				case 1:	//25-35	
						base = 25;
						step = 1;
						break;
				
				case 2:	//15-45	
						base = 15;
						step = 3;
						break;
				
				case 3:	//0-100	
						base = 0;
						step = 10;
						break;
				
				default:break;
			}
			
			for (i=10;i>=0;i--)
				g.drawString(new Integer(i*step+base).toString(),75,105+(10-i)*30);
			
			g.setColor(Color.red);
			
			for (i=0;i<15-j;i++){
				
				int y1 = y0-((int)((chart[i+j]-base)*30/step));
				int y2 = y0-((int)((chart[i+j+1]-base)*30/step));
				
				int x1 = x0+25*i;
				int x2 = x0+25*(i+1);
				
				g.drawLine(x1,y1,x2,y2);
			}

	 	}
	 	
	 	// getCase
	 	int getCase(float[] ch){
			
			float max = 0;
			float min = 100;	
			int i;
			
			for (i= 0;i<timeScale+1;i++)
				if (ch[i]!=0){
					if (ch[i]<min) min = ch[i];
					if (ch[i]>max) max = ch[i];
				}			
			
			if (min>25&&max<35)
				return 1;
			
			else if (min>15&&max<45)
				return 2;
			
			return 3;			
		}
		
	 	// drawChart
		void drawChart(float[] ch){
	  		chart=ch;
	  		repaint();
		}
	}
	
	class ButtonEventHandler implements ActionListener {

  		public void actionPerformed(ActionEvent e){
  			
  			if(currentTime >= 0){
  			
	  			String ac = e.getActionCommand();
	  			try{
		  	
		  			if (ac.indexOf("Borrow")>=0){			// Borrow
		  			
						if (isValidFloat(borrowRepay.getText())){
							
							float borrowVal = getFloatValue(borrowRepay.getText());
							float debitVal = getFloatValue(debit.getText());
							
							if(debitVal >= maxDebit)
								msgBox("debit_overflow");
			  					
							else if(borrowVal+debitVal > maxDebit){
			  					
			  					msgBox("limited_borrow", maxDebit-debitVal);
			  					borrowRepay.setText(new Float(maxDebit - debitVal).toString());
							}
							
							else if(borrowVal == 0)
			  					msgBox("zero_no");
			  					
							else if(smg.borrow(player, borrowVal)){
			  					
			  					Account a = smg.getAccount(player);
			  					
				  				cash.setText(new Float(a.getCash()).toString());
				  									  
				  				debit.setText(new Float(a.getDebit()).toString());
				  				
				  				borrowRepay.setText("");
			  				}
			  			}
			  			else
			  				msgBox("invalid_no");
		  			}
		  			
		  			else if(ac.indexOf("Repay")>=0){		// Repay
		  				
		  				if (isValidFloat(borrowRepay.getText())){
		  					
		  					float repayVal = getFloatValue(borrowRepay.getText());
							float debitVal = getFloatValue(debit.getText());		  				
							
			  				if(repayVal == 0)
			  					msgBox("zero_no");
			  				
			  				else if(debitVal == 0)
			  					msgBox("no_debit");
			  					
			  				else if(smg.repay(player, repayVal)){
			  					
			  					Account a = smg.getAccount(player);
			  					
				  				cash.setText(new Float(a.getCash()).toString());
				  									  
				  				debit.setText(new Float(a.getDebit()).toString());
				  				
				  				if(repayVal > debitVal)
				  					msgBox("limitd_repay",debitVal);				  				
				  				borrowRepay.setText("");
			  				}
			
			  				else
			  					msgBox("cash_shortage");
			  			}			
		  				else
			  				msgBox("invalid_no");
		  			} 
		  			
		  			else if (ac.indexOf("Set Alert")>=0){		// Set Alert
		  				
		  				String lower = lowerPrice.getText();
		  				String upper = upperPrice.getText();
		  				String symbol = (String)symbolCBTrade.getSelectedItem();
		  				float price = ((Float)stocksPrices.get(symbol)).floatValue();
		  				
		  				if(lower.equals("") && isValidFloat(upper)){
		  					float u = getFloatValue(upper);
		  					
		  					if(u == 0)
		  						msgBox("zero_no");
		  					
		  					else if(u <= price)
		  						msgBox("alert_rejected", price);
		  						
		  					else{
			  					smg.setAlert(symbol, player,0,u);
			  				
			  					lowerPrice.setText("");
	  							upperPrice.setText("");
	  						}
			  													
					  	}
					  	
					  	else if(isValidFloat(lower) && upper.equals("")){
		  					
		  					float l = getFloatValue(lower);
		  					
		  					if(l == 0)
		  						msgBox("zero_no");
		  						
		  					else if(l >= price)
		  						msgBox("alert_rejected", price);
		  					
		  					else{	
		  						smg.setAlert(symbol,player,l,1000);		
		  					
		  						lowerPrice.setText("");
	  							upperPrice.setText("");
	  						}
					  	}
					  	
					  	else if(isValidFloat(upper) && isValidFloat(lower)){
		  					
		  					float u = getFloatValue(upper);
		  					float l = getFloatValue(lower);
		  					
		  					if (u == 0 || l == 0)
		  						msgBox("zero_no");
		  					
		  					else if ( u < l)
	  							msgBox("invert_ul");
	  						
		  					else if (u <= price || l >= price)
	  							msgBox("alert_rejected", price);
	  					
	  						else {
	  							smg.setAlert(symbol,player,l,u);
	  							
	  							lowerPrice.setText("");
	  							upperPrice.setText("");
	  						}
					  	}
					  	
					  	else
			  				msgBox("invalid_no");	
					  		
		  			
		  			}
		  			
		  			else if(ac.indexOf("Remove Alert")>=0){		// Remove Alert
		  				
		  				smg.removeAlert((String)symbolCBTrade.getSelectedItem(), player);	  				
		  			} 
		  			
		  			else if (ac.indexOf("Sell")>=0){ 			// Post Sell bid
		  				
		  				if (isValidInteger(tradeShares.getText())){
		  					
			  				String symbol = (String)symbolCBTrade.getSelectedItem();
			  				int sharesNo = getIntegerValue(tradeShares.getText());
			  				float price = ((Float)stocksPrices.get(symbol)).floatValue();
			  				
			  				if(sharesNo > 1000)
			  					msgBox("out_of_range");	
			  				
			  				else if(sharesNo == 0)
				  				msgBox("zero_no");
				  						  			
			  				else if(smg.postTradeBid(symbol, player, sharesNo, selling)){
				  				
				  				addToHistory(symbol, "Sell Bid", sharesNo, price);
				  				
				  				tradeShares.setText("");
	  							tradePrice.setText("");
				  			}
				  			
				  			else
				  				msgBox("shares_shortage");				  			
				  		}	
			  			else
			  				msgBox("invalid_no");				  			
		  			}
		  			
		  			else if(ac.indexOf("Buy")>=0){				// Post Buy bid
		  					
		  				if (isValidInteger(tradeShares.getText())){
		  					
			  				String symbol = (String)symbolCBTrade.getSelectedItem();
			  				int sharesNo = getIntegerValue(tradeShares.getText());
			  				float price = ((Float)stocksPrices.get(symbol)).floatValue();
			  				
			  				if(sharesNo > 1000)
				  				msgBox("out_of_range");	
				  						  		
				  			else if(sharesNo == 0)	
				  				msgBox("zero_no");
				  				
			  				else if(smg.postTradeBid(symbol, player, sharesNo, buying)){
				  				
				  				addToHistory(symbol, "Buy Bid", sharesNo, price);
				  				
				  				tradeShares.setText("");
	  							tradePrice.setText("");
				  			}
				  			
				  			else
				  				msgBox("cash_shortage");
			  			}
			  			else
			  				msgBox("invalid_no");			  		
		  			} 
		  			
		  			else if (ac.indexOf("Undo")>=0){			// Undo sell/buy bid
		  				
		  				String temp, symbol;
		  				char bidType;
		  				int sharesNo;
		  				int where;
						float price;
						
						temp = ((String)bidsCB.getSelectedItem()).trim();
		  				if (temp.length() != 0 ){
		  				
		  					where = temp.indexOf(":");
		  					symbol = temp.substring(0, where - 1);
		  					temp = temp.substring(where + 2);
		  					bidType = temp.charAt(0);
		  					sharesNo = new Integer(temp.substring(4)).intValue();
		  					
		  					price = ((Float)stocksPrices.get(symbol)).floatValue();
		  					if (bidType == 'S'){//sell
		  						if(smg.undoTrade(symbol, player, selling))
		  							addToHistory(symbol, "Undo Sell Bid", sharesNo, price);
		  					}
		  					else {
		  						if(smg.undoTrade(symbol, player, buying))
		  							addToHistory(symbol, "Undo Buy Bid", sharesNo, price);
		  					}
		  				}
		  				bidsCB.setSelectedIndex(0);
		  			}
		  			else if(ac.indexOf("Draw Chart")>=0){		// draw chart
			  					  			
		  				pc.drawChart(smg.getChart((String)symbolCBChart.getSelectedItem()));	
		  				
		  			} 
	  			}catch(RemoteException ex){
	  				System.err.println(ex.getMessage());
	  			}
	  		}
  		}
  		
 	}
 	
 	class TradeSharesKeyListener implements KeyListener {

  		public void keyPressed(KeyEvent e){}
  		public void keyTyped(KeyEvent e){}
  		public void keyReleased(KeyEvent e){		// Compute the Price
  			
  			if(isValidInteger(tradeShares.getText()) && currentTime >= 0){
	  			float price = getFloatValue(sharePrice.getText());
	  			int shareNo = getIntegerValue(tradeShares.getText());
	  			tradePrice.setText(new Float(price * (float)shareNo).toString());
  			}
			
  		}
 	}
 	
 	class TradePriceKeyListener implements KeyListener {

  		public void keyPressed(KeyEvent e){}	
  		public void keyTyped(KeyEvent e){}
  		public void keyReleased(KeyEvent e){		// Compute Share #
		
			if(isValidFloat(tradePrice.getText()) && currentTime >= 0){
				float price = getFloatValue(sharePrice.getText());
	  			float money = getFloatValue(tradePrice.getText());
	  			tradeShares.setText(new Integer((int)(money/price)).toString());
	  		}
  		}
 	}
 	
 	class TradeSymbolPopupMenuListener implements PopupMenuListener {

  		public void popupMenuCanceled(PopupMenuEvent e){}
  		public void popupMenuWillBecomeVisible(PopupMenuEvent e){}
  		public void popupMenuWillBecomeInvisible(PopupMenuEvent e){		// Sellect a symbol
  			
  			String symbol = (String)symbolCBTrade.getSelectedItem();
  			try{
  			sharePrice.setText(new Float(smg.getStockQuote(symbol)).toString());
  			availableShares.setText(new Integer(smg.getAvailableSharesForTrade(symbol)).toString());
  			}
  			catch(RemoteException ex){
  				System.out.println(ex.getMessage());
  			}
  		}
  	}
  	class ChartSymbolPopupMenuListener implements PopupMenuListener {

  		public void popupMenuCanceled(PopupMenuEvent e){}
  		public void popupMenuWillBecomeVisible(PopupMenuEvent e){}
  		public void popupMenuWillBecomeInvisible(PopupMenuEvent e){ 	// Sellect a symbol
			
			try{
				// draw chart
				pc.drawChart(smg.getChart((String)symbolCBChart.getSelectedItem()));
			}
			catch(RemoteException ex){
				System.err.println(ex.getMessage());
			}
  		}
  	}
 	 	
 	class BidPopupMenuListener implements PopupMenuListener {
		public void popupMenuCanceled(PopupMenuEvent e){}
  		public void popupMenuWillBecomeInvisible(PopupMenuEvent e){}
  		public void popupMenuWillBecomeVisible(PopupMenuEvent e){	// connect and get the bids
  		
  		
	  		bidsCB.removeAllItems();
	  		
	  		if(currentTime >= 0){
	  			
		  		bidsCB.addItem("");
		  		String symbol,item;
		  		Hashtable table ;
		  		Enumeration en;
		  		
		  		try{
			  		table = smg.getPlayerBuyBids(player);
			  		en = table.keys();
			  		while (en.hasMoreElements()){
			  			
			  			symbol = (String)en.nextElement();
			  			item = symbol + " : B : " + ((Integer)(table.get(symbol))).toString();
			  			bidsCB.addItem(item);
			  		}
			  		
			  		table = smg.getPlayerSellBids(player);
			  		en = table.keys();
			  		while (en.hasMoreElements()){
			  			
			  			symbol = (String)en.nextElement();
			  			item = symbol + " : S : " + ((Integer)(table.get(symbol))).toString();
			  			bidsCB.addItem(item);
			  		}
		  		}
		  		catch(RemoteException ex){
		  			System.out.println(ex.getMessage());
		  		}
	  		}
  		}
  	}
  	
  	class TabChangeListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			try{
				switch(tab.getSelectedIndex())
				{
				
				case 1:				// Chart	
						String s = (String)symbolCBTrade.getSelectedItem();
						symbolCBChart.setSelectedItem(s);
						pc.drawChart(smg.getChart(s));				
						break;
							
				case 2:				// position
						if(currentTime >= 0){
							refreshPosition();
						}
						
						break;
	
				default:
						break;
				}
			}
			catch(RemoteException ex){
				System.out.println(ex.getMessage());
			}
		}
	}
	
	public class WL implements WindowListener {
		public void  windowOpened(WindowEvent ev){}
		public void  windowClosed(WindowEvent ev){}
		public void  windowIconified(WindowEvent ev){}
		public void  windowDeiconified(WindowEvent ev){}
		public void  windowActivated(WindowEvent ev){}
		public void  windowDeactivated(WindowEvent ev){}
		public void  windowClosing(WindowEvent ev){
			
			try{
				if(currentTime >= 0)
					smg.exit(player);
			}
			catch(RemoteException ex){	
				System.out.println(ex.getMessage());
			}
			System.exit(0);
		}
	}
}