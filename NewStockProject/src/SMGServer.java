
// Remote Interface: SMGServer

import java.rmi.*;
import java.util.*;

public interface SMGServer extends Remote{
	
	public int register(SMGClient aClient, String player) throws RemoteException;
		
	public int getGameTime() throws RemoteException;

	public String[] getCompaniesNames() throws RemoteException;
	
	public float getStockQuote(String symbol) throws RemoteException;
	
	public Hashtable getAllStockQuotes() throws RemoteException;

	public void refreshWinnersList() throws RemoteException;

	public int getAvailableSharesForTrade(String symbol) throws RemoteException;
	
	public Account getAccount(String player) throws RemoteException;
	
	public Hashtable getPlayerShares(String player) throws RemoteException;

	public float getPlayerSharesPrice(String player) throws RemoteException; 

	public boolean borrow (String player, float val) throws RemoteException;

	public boolean repay (String player, float val) throws RemoteException;

	public boolean postTradeBid (String symbol, String player, int shareNo, String bidType) throws RemoteException;
	
	public boolean undoTrade(String symbol, String player, String bidType) throws RemoteException;
	
	public Hashtable getPlayerSellBids(String player) throws RemoteException;

	public Hashtable getPlayerBuyBids(String player) throws RemoteException;

	public void setAlert(String symbol, String player, float lower, float upper) throws RemoteException;	

	public void removeAlert(String symbol, String player) throws RemoteException;
	
	public float[] getChart(String symbol) throws RemoteException;
	
	public void exit(String player) throws RemoteException;
}
