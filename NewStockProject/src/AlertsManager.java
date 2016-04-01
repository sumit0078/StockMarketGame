
// Alert

import java.util.*;

public class AlertsManager{
	
	ArrayList alerts;
	
	// Constructor
	public AlertsManager(){
		alerts = new ArrayList();
	}
	
	// setAlert
	public void setAlert(String symbol, String player, float lowPrice, float upperPrice){
		
		removeAlert(symbol, player);
		synchronized(alerts){
			alerts.add(new Alert(symbol, player, lowPrice, upperPrice));
		}
	}
	
	// remove the alert of the given symbol
	public void removeAlert(String symbol, String player){
		
		Alert thisAlert;
		synchronized(alerts){
			for (int i =0; i< alerts.size(); i++){
				thisAlert = (Alert)alerts.get(i);
			
				if (player.equals(thisAlert.player)
					&& symbol.equals(thisAlert.symbol)){
					
					alerts.remove(i);
					i = alerts.size(); // end the for loop
				}
			}	
		}
	}
	
	// remove all alerts of the given player
	public void removePlayerAlerts(String player){
		
		Alert thisAlert;
		synchronized(alerts){
			for (int i =0; i< alerts.size(); i++){
				thisAlert = (Alert)alerts.get(i);
			
				if (player.equals(thisAlert.player))
					alerts.remove(i);
			}	
		}
	}

	// test active alerts
	public void testAlerts(Hashtable currentPrices, Hashtable clients)
		throws java.rmi.RemoteException{
		
		Alert thisAlert;
		String symbol;
		String player;
		float curPrice;
		float alertPrice;
		
		synchronized(alerts){
			for (int i =0; i< alerts.size(); i++){
				
				thisAlert = (Alert)alerts.get(i);
				symbol = thisAlert.symbol;
				player = thisAlert.player;
				curPrice = ((Float)currentPrices.get(symbol)).floatValue();
				
				alertPrice = thisAlert.testAlert(curPrice);
				if(alertPrice >= 0){
					((SMGClient)clients.get(player)).activeAlertCB(symbol, alertPrice, curPrice);
					alerts.remove(i);
				}
			}
		}
	}
}