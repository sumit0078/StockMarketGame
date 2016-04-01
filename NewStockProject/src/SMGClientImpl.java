
// Implementation of the remote interface SMGClient

import javax.swing.*;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class SMGClientImpl implements SMGClient{

	private static final int rmiPort = 1099;
	private static final String rmiHost = "localhost";
			
	public SMGame smgGame;
	
	// Constructor
	public SMGClientImpl() throws RemoteException{
		super();
	}
	
	// set SMGame
	public void setSMGame(SMGame smgGame){
		this.smgGame = smgGame;
	}
	
	// activeAlertCB
	public void activeAlertCB(String symbol, float alertPrice, float currentPrice) 
		throws RemoteException{
			
		if(alertPrice > currentPrice)
			smgGame.alertMsgBox("Share price of " + symbol + " has falled to " + currentPrice + "$");
					
		else
			smgGame.alertMsgBox("Share price of " + symbol + " has raised to " + currentPrice + "$");
		
	}
	
	// getCurrentSymbolCB
	public String getCurrentSymbolCB() 
		throws RemoteException{
			
		return (String)smgGame.symbolCBTrade.getSelectedItem();
	}
	
	// refreshAvailSharesCB
	public void refreshAvailSharesCB(int shares) 
		throws RemoteException{
		
		smgGame.availableShares.setText((new Integer(shares)).toString());
	}
	
	// refreshAllStockQuoteCB
	public void refreshAllStockQuoteCB(Hashtable stockPrices) 
		throws RemoteException{
		
		smgGame.sharePrice.setText(((Float)(stockPrices.get(getCurrentSymbolCB()))).toString());

		smgGame.refreshMarquee(stockPrices);
	}
	
	// refreshAccountCB
	public void refreshBalanceCB(Account account, float playerSharesPrice) 
		throws RemoteException{
		
		smgGame.refreshBalance(account, playerSharesPrice);	
	}
	
	// AddActionCB
	public void addActionCB(String action)
		throws RemoteException{
		
		smgGame.addAction(action + "\n");
	}
	
	// addToHistoryCB
	public void addToHistoryCB(String symbol, String type, int sharesNo, float sharePrice)
		throws RemoteException{
			
		smgGame.addToHistory(symbol, type, sharesNo, sharePrice);
	}
		
	// refreshWinnersListCB
	public void refreshWinnersListCB(String winners)
		throws RemoteException{
		
		smgGame.refreshWinnersListCB(winners);
	}
	
	
	/******** main ********/
	public static void main(String args[]){
		try{
	
			/*****************************************************/
			SMGClientImpl client = new SMGClientImpl();
			UnicastRemoteObject.exportObject(client);                        
			/*****************************************************/
			
			SMGServer smg=(SMGServer)Naming.lookup("rmi://" 
													+ SMGClientImpl.rmiHost 
									                + ":"
									                + SMGClientImpl.rmiPort 
									                + "/smg");
			////////////////////
			JFrame reg = new SMGRegister(smg, client, client);
			reg.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			reg.setSize(270,180);
			reg.setVisible(true);
			///////////////////
		
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
}			