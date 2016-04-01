
// Remote Interface: SMGClient

import java.rmi.*;
import java.util.*;

public interface SMGClient extends Remote{
	
	public void activeAlertCB(String symbol, float alertPrice, float currentPrice) throws RemoteException;
	
	public String getCurrentSymbolCB() throws RemoteException;
		
	public void refreshAvailSharesCB(int shares) throws RemoteException;
	
	public void refreshAllStockQuoteCB(Hashtable stockPrices) throws RemoteException;
		
	public void refreshBalanceCB(Account account, float playerSharesPrice) throws RemoteException;
	
	public void addActionCB(String action) throws RemoteException;
	
	public void addToHistoryCB(String symbol, String type, int sharesNo, float sharePrice) throws RemoteException;
	
	public void refreshWinnersListCB(String winners) throws RemoteException;
}
	