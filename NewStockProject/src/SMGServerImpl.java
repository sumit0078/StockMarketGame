
// Implementation of the remote interface SMGServer

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class SMGServerImpl extends UnicastRemoteObject implements SMGServer {
		
	private Hashtable clients;
	private CompaniesManager compsManager;
	private Bank bank;
	private Exchange exchange;
	private AlertsManager alertsManager;
	
//	private SMGClient client;
	
	// Constructor
	public SMGServerImpl(String[] companies, int level1, int level2, int gameTime)
		throws RemoteException{
			
		super();
		
		SMG.gameTime = gameTime*60;
		final int l1 = level1;
		final int l2 = level2;
		
		clients = new Hashtable();		
		compsManager = new CompaniesManager(companies);
		bank = new Bank();
		alertsManager = new AlertsManager();
		exchange = new Exchange(bank, compsManager, alertsManager, clients);
		
		try{			
			Naming.rebind("rmi://" + SMG.rmiHost + ":" + SMG.rmiPort + "/smg",this); 
			
			System.out.println("Registration has completed successfully...");
						
		}catch(Exception e){
			System.out.println("Failed to start RMI server: " + e.getMessage());
		}
		
		// activateCompPlayers
		Thread activateCompPlayers = new Thread() {
			ComputerPlayer comp1;
			ComputerPlayer comp2;
			public void run() {
				try{
				  	while(true){
						
				  		comp1 = new ComputerPlayer(SMG.computer1,exchange,compsManager,
				  								    bank, l1<=4?l1:(int)(Math.random()*4+1));
					  	comp1.start();
					  	exchange.broadcastCallback("Welcome to " + "Computer1");
				  		refreshWinnersList();
					  	sleep(SMG.gameTime/3*1000);
					  	
					  	comp2 = new ComputerPlayer(SMG.computer2,exchange, compsManager,
					  							    bank, l2<=4?l2:(int)(Math.random()*4+1));
					  	comp2.start();
					  	exchange.broadcastCallback("Welcome to " + "Computer2");
						refreshWinnersList();
				  		sleep(2*SMG.gameTime/3*1000);
						
						comp1.die();
						exchange.broadcastCallback("Computer1" + " left us");
				  		refreshWinnersList();
				  		
				  		sleep(SMG.gameTime/3*1000);
				  		comp2.die();
				  		exchange.broadcastCallback("Computer2" + " left us");
				  		refreshWinnersList();
					}
				}catch(InterruptedException e){	
		  			System.out.println(e.getMessage());
		  		}
		  		catch(RemoteException e){	
		  			System.out.println(e.getMessage());
		  		}	
		  	}
		  };
		  
		  activateCompPlayers.start();
				
		Thread timer = new Thread() {
		  public void run() {
		  	try{
		  		while(true){
				  	// sleep a minute
				  	Thread.sleep(SMG.varUpdateTimerPeriod * 1000);
				  	
				  	bank.timer();
				  	exchange.timer();
				  					  	
				  	// every timer tick callback
				  	callback();
			  }
		  	}
		  	catch(InterruptedException e){	
		  		System.out.println(e.getMessage());
		  	}
		  	catch(RemoteException e){	
		  		System.out.println(e.getMessage());
		  	}
		  }
		  
		  // private method: every timer tick callback
		  private void callback() throws RemoteException{
			 	Enumeration en;
			  	SMGClient aClient;
			  	String player;

			  	Hashtable ht = compsManager.getAllPrices();
			  	
			  	synchronized(clients){	
			  		en = clients.keys();		  	
				  	while(en.hasMoreElements()){
				  		player = (String)en.nextElement();
				  		aClient = (SMGClient)clients.get(player);
				  		
				  		aClient.refreshBalanceCB(bank.getAccount(player), exchange.getPlayerSharesPrice(player));
				  		exchange.refreshWinnersListCallback();
				  		aClient.refreshAllStockQuoteCB(ht);
				}
			  }
			}
		};
		timer.start(); 
	}
		
	// register
	public int register(SMGClient aClient, String player)
		throws RemoteException{
		
		int flag;
		
		synchronized(clients){
			
			if(clients.size() >= (SMG.maxPlayersNo - SMG.computerPlayersNo))
				return -1;
					
			if(clients.containsKey(player))
				return -2;
				
			flag = exchange.register(player);
			if(flag == 0){
				clients.put(player, aClient);
				
				// callback
				broadcastCallback(player, "Welcome to " + player);
			}
		}
		return flag;
	}
	
	// getGameTime
	public int getGameTime()
		throws RemoteException{
		
		return SMG.gameTime;
	}

	// getCompaniesNames
	public String[] getCompaniesNames()
		throws RemoteException{
		
		return compsManager.getCompaniesNames();

	}
	
	// getStockQuote
	public float getStockQuote(String symbol)
		throws RemoteException{
		
		return compsManager.getCurrentPrice(symbol);
	}
	
	// getAllStockQuotes
	public Hashtable getAllStockQuotes()
		throws RemoteException{
			return exchange.getAllStockQuotes();
	}
	
	public void refreshWinnersList()
		throws RemoteException{
			exchange.refreshWinnersListCallback();
	}

	// getAvailableShares
	public int getAvailableSharesForTrade(String symbol)
		throws RemoteException{
			
		return exchange.getAvailableSharesForTrade(symbol);
	}
	
	// getAccount
	public Account getAccount(String player)
		throws RemoteException{
			
		return bank.getAccount(player);
	}
	
	// getPlayerShares
	public Hashtable getPlayerShares(String player)
		throws RemoteException{
		return exchange.getPlayerShares(player);
	}
	
	// getPlayerSharesPrice
	public float getPlayerSharesPrice(String player)
		throws RemoteException{
		return exchange.getPlayerSharesPrice(player);
	}
	
	// borrow
	public boolean borrow (String player, float val)
		throws RemoteException{
			
		return bank.borrow(player, val);
	}
	
	// repay
	public boolean repay (String player, float val)
		throws RemoteException{
			
		return bank.repay(player, val);
	}
	
	// postTradeBid
	public boolean postTradeBid (String symbol, String player, int shareNo, String bidType)
		throws RemoteException{

		return exchange.setBid(symbol,player,shareNo,bidType);
	}
	
	// undoTrade
	public boolean undoTrade(String symbol, String player, String bidType)
		throws RemoteException{
		
		return exchange.undoTrade(symbol, player, bidType);
	}
	
	// getPlayerSellBids
	public Hashtable getPlayerSellBids(String player)
		throws RemoteException{
			
		return exchange.getPlayerSellBids(player);
	}
	
	// getPlayerBuyBids
	public Hashtable getPlayerBuyBids(String player)
		throws RemoteException{
			
		return exchange.getPlayerBuyBids(player);
	}
	
	// setAlert
	public void setAlert(String symbol, String player, float lower, float upper)
		throws RemoteException{
			
		alertsManager.setAlert(symbol, player, lower, upper);
	}
	
	// removeAlert
	public void removeAlert(String symbol, String player)
		throws RemoteException{
			
		alertsManager.removeAlert(symbol, player);
	}
	
	// getChart
	public float[] getChart(String symbol)
		throws RemoteException{
			
		return compsManager.getChart(symbol);
	}
	
	// exit
	public void exit(String player)
		throws RemoteException{

					
		synchronized(clients){
			clients.remove(player);
		}
		
		exchange.removePlayer(player);
		bank.removeAccount(player);
		alertsManager.removePlayerAlerts(player);
		
		// callback
		broadcastCallback(player, player + " left us");
		exchange.refreshWinnersListCallback();
	}
	
	// callback
	private void broadcastCallback(String player, String stm)
		throws RemoteException{
		Enumeration en;
		String p;
		
	  	synchronized(clients){	
			en = clients.keys();		  	
			while(en.hasMoreElements()){
				p = (String)en.nextElement();
				if(!player.equals(p))
					((SMGClient)clients.get(p)).addActionCB(stm);
			}
		}
	}
}