
// Exchange

import java.rmi.*;
import java.util.*;

public class Exchange extends java.lang.Thread{
	
	private Hashtable symbolsTable;
	private Hashtable playersList;
	private Hashtable clients;
	private Hashtable currentPrices;
	private AlertsManager alertsManager;
	private Bank bank;
	private CompaniesManager cm;

	// Constructor	
	public Exchange (Bank bank, CompaniesManager cm, AlertsManager alertsManager, Hashtable clients){
		symbolsTable = new Hashtable();
		playersList = new Hashtable();
		currentPrices = new Hashtable();
		this.bank = bank;
		this.cm = cm;
		this.clients = clients;
		this.alertsManager = alertsManager;
		this.start();
	}
	
	// register
	public int register(String playerName){
		
		synchronized(playersList){
			
			if(playersList.size() >= SMG.maxPlayersNo)				
				return -1;
			
			else if(playersList.containsKey(playerName))
				return -2;
				
			else{
				playersList.put(playerName, new PlayerShares());
				bank.newAccount(playerName);
				return 0;
			}
		}
	}
	
	// set a bid
	public boolean setBid (String symbol, String player, int shareNo, String bidType){
	
		Bid b;
		Account playerAccount = bank.getAccount(player);
		PlayerShares s = (PlayerShares)playersList.get(player);			
		if(playerAccount == null || s == null)
			return false;			
		if(playerAccount.getCash() < cm.getCurrentPrice(symbol) * shareNo && bidType.equals(SMG.buy))
			return false;		
		if((s.getSharesNo(symbol) < shareNo) && bidType.equals(SMG.sell))
			return false;		
		if(shareNo > 1000 || shareNo <= 0)
			return false;
						
		synchronized(symbolsTable){
			if(!symbolsTable.containsKey(symbol))
				symbolsTable.put(symbol,new Bid());
			}
		b = (Bid)symbolsTable.get(symbol);	
		b.setBid(player, shareNo, bidType);
	
		// callback
		try{
			if(bidType.equals(SMG.sell))
				refreshAvailableSharesCallback(symbol);
			broadcastCallback(player +" post a "+ bidType + " bid (" + shareNo +" shares of " + symbol+")");
			}catch(RemoteException e){
				System.err.println(e.getMessage());
			}			
		return true;
	}
	
	// undo bid
	public boolean undoTrade(String symbol, String player, String bidType){
		
		Bid b;
		int shareNo = 0;
		
		if(symbolsTable.containsKey(symbol)){
			b = (Bid)symbolsTable.get(symbol);
			
			if(b != null){
				
				if(bidType.equals(SMG.sell)){			
					Integer n = (Integer)(b.getSellBids().get(player));
					if(n != null)
						shareNo = n.intValue();
					else
						return false;
				}
				else if(bidType.equals(SMG.buy)){			
					Integer n = (Integer)(b.getBuyBids().get(player));
					if(n != null)
						shareNo = n.intValue();
					else
						return false;
				}
					
				if(b.removeBid(player, bidType)){
						
					try{
						broadcastCallback(player + " undo a " + bidType + " bid (" + shareNo +" shares of " + symbol+")");
					}catch(RemoteException e){
						System.err.println(e.getMessage());
					}
					return true;
				}
			}
		}
		return false;
	}
	
	// get sell bid shares 
	public int getSellBidShares(String symbol){
		
		if(symbolsTable.containsKey(symbol))
			return ((Bid)(symbolsTable.get(symbol))).getSellBidShares();
		return 0;
	}
	
	// get buy bid shares 
	public int getBuyBidShares(String symbol){
		
		if(symbolsTable.containsKey(symbol))
			return ((Bid)(symbolsTable.get(symbol))).getBuyBidShares();
		return 0;
	}
	
	// get avialable shares for trade
	public int getAvailableSharesForTrade(String symbol){
		
		return cm.getSharesNo(symbol) + getSellBidShares(symbol);
	}
	
	// getPlyaerShares
	public Hashtable getPlayerShares(String player){
		if(playersList.containsKey(player))
			return ((PlayerShares)playersList.get(player)).getAllShares();
		return null;
	}
	
	// getPlyaerShares
	public int getPlayerShares(String symbol, String player){
		if(playersList.containsKey(player))
			return ((PlayerShares)playersList.get(player)).getSharesNo(symbol);
		return 0;
	}
	
	// getPlyaerSharesPrice
	public float getPlayerSharesPrice(String player){
		
		float total = 0;
		String symbol;
		Hashtable h = getPlayerShares(player);
		Enumeration en = h.keys();
		
		while(en.hasMoreElements()){
			symbol = (String)en.nextElement();
			total += ((Integer)h.get(symbol)).intValue() * cm.getCurrentPrice(symbol);
		}
		return total;
	}

	// get player  sell bids
	public Hashtable getPlayerSellBids(String player){
	
		Enumeration symbols;
		Enumeration players;
		Hashtable allPlayersSellBids;
		Hashtable playerSellBids;
		String thisSymbol;
		Bid b;
		
		playerSellBids = new Hashtable();
		symbols = symbolsTable.keys();
		while(symbols.hasMoreElements()){
			
			thisSymbol = (String)symbols.nextElement();
			b = (Bid)symbolsTable.get(thisSymbol);
			allPlayersSellBids = (Hashtable)b.getSellBids();
		
			if(allPlayersSellBids.containsKey(player))
				
				// a table of [symbol:shareNo]
				playerSellBids.put(thisSymbol, allPlayersSellBids.get(player));
		}
		return playerSellBids;
	}
	
	// get player buy bids
	public Hashtable getPlayerBuyBids(String player){
	
		Enumeration symbols;
		Enumeration players;
		Hashtable allPlayersBuyBids;
		Hashtable playerBuyBids;
		String thisSymbol;
		Bid b;
		
		playerBuyBids = new Hashtable();
		symbols = symbolsTable.keys();
		while(symbols.hasMoreElements()){
			
			thisSymbol = (String)symbols.nextElement();
			b = (Bid)symbolsTable.get(thisSymbol);
			allPlayersBuyBids = (Hashtable)b.getBuyBids();
		
			if(allPlayersBuyBids.containsKey(player))
				// a table of [symbol:shareNo]
				playerBuyBids.put(thisSymbol, allPlayersBuyBids.get(player));
		}
		return playerBuyBids;
	}
	
	// perform selling
	private boolean sell(String player, String symbol, int shareNo){
	
		Account playerAccount = bank.getAccount(player);
		PlayerShares s = (PlayerShares)playersList.get(player);
		
		if(s.decShares(symbol, shareNo)){
			synchronized(playerAccount){		
				playerAccount.incCash(cm.getCurrentPrice(symbol)*shareNo);
			}
			return true;
		}
		return false;
	}
	
	// perform buying
	private boolean buy(String player, String symbol, int shareNo){
		
		PlayerShares s = (PlayerShares)playersList.get(player);
		Account playerAccount = bank.getAccount(player);
		
		synchronized(playerAccount){
			if(playerAccount.decCash(cm.getCurrentPrice(symbol)*shareNo)){
				s.incShares(symbol, shareNo);
				return true;
			}
			return false;
		}
	}
	
	// remove player from the playersList and remove all of its bids
	public void removePlayer(String player)
		throws RemoteException{
		
		if(playersList.containsKey(player)){
			prepareToRemove(player);
			playersList.remove(player);
		}
	}

	// prepareToRemove
	private void prepareToRemove(String player)
		throws RemoteException{

		Hashtable hs = getPlayerSellBids(player);
		Hashtable hb = getPlayerBuyBids(player);
		
		Enumeration ens = hs.keys();
		Enumeration enb = hb.keys();
		
		String symbol;
		while(ens.hasMoreElements()){
			symbol = (String)ens.nextElement();
			undoTrade(symbol, player, SMG.sell);
			refreshAvailableSharesCallback(symbol);
		}

		while(enb.hasMoreElements())
			undoTrade((String)enb.nextElement(), player, SMG.buy);
			

		Hashtable allShares = ((PlayerShares)playersList.get(player)).getAllShares();
		Enumeration ensh = allShares.keys();
		String sym;
		
		while(ensh.hasMoreElements()){
			sym = (String)ensh.nextElement();
			cm.incShares(sym, ((Integer)allShares.get(sym)).intValue());
			refreshAvailableSharesCallback(sym);
		}
	}
	
	// getAllStockQuotes
	public Hashtable getAllStockQuotes(){
		
		Hashtable cs = cm.getCompanies();
		Enumeration en = cs.keys();
		Company c;
		String s;
				
		synchronized(currentPrices){
			currentPrices.clear();
			
			while (en.hasMoreElements()){
				s = (String)en.nextElement();
				c = (Company)cs.get(s);				
				currentPrices.put(s, new Float(c.getCurrentPrice()));
			}
		}
		return currentPrices;
	}
	
	// timer
	void timer (){
		
		Hashtable cs = cm.getCompanies();
		Enumeration en = cs.keys();
		Company c;
		String s;
		float newPrice;
		
		synchronized(currentPrices){
			currentPrices.clear();
			
			while (en.hasMoreElements()){
				s = (String)en.nextElement();
				c = (Company)cs.get(s);
				
				newPrice = (float)(c.getCurrentPrice()
						 + ((getBuyBidShares(s)-getSellBidShares(s))*0.01)
						 + (float)(java.lang.Math.random()*3.0 - 1.5));
						 
				newPrice = newPrice>3?newPrice:3;
				newPrice = newPrice<100?newPrice:100;
				
				c.setCurrentPrice(newPrice);
				
				currentPrices.put(s, new Float(newPrice));
			}
		}
		
		try{
			alertsManager.testAlerts(currentPrices, clients);
		}catch(RemoteException e){
			System.err.println(e.getMessage());
		}
	}
	
	// callback
	private void refreshAvailableSharesCallback(String symbol) 
		throws RemoteException {
		
		Enumeration en;
	  	SMGClient aClient;
	  		  	
	  	synchronized(clients){
	  		en = clients.keys();		  	
		  	while(en.hasMoreElements()){
			  	aClient = (SMGClient)clients.get(en.nextElement());
			  	if(symbol.equals(aClient.getCurrentSymbolCB()))
			  		aClient.refreshAvailSharesCB(getAvailableSharesForTrade(symbol));
			}
		}
	}
	
	// callback
	private void refreshBalanceCallback(String player) 
		throws RemoteException {

	  	SMGClient aClient;
		if(clients.containsKey(player)){ 
			aClient = (SMGClient)clients.get(player);
			aClient.refreshBalanceCB(bank.getAccount(player), getPlayerSharesPrice(player));		 
		}
	}
	
	// callback
	public void refreshWinnersListCallback()
		throws RemoteException{
		
		float[] scores = new float[SMG.maxPlayersNo];
		String[] names = new String[SMG.maxPlayersNo];
		
		float  tempScore;
		String tempPlayer;
		String winners = "";
		
		Enumeration players;
		String thisPlayer;
		SMGClient thisClient;
		Account thisAccount;
		int i=0,j=0;
		int count = 0;
		
			players = playersList.keys();
			while(players.hasMoreElements()){
				
				thisPlayer = (String)players.nextElement();
				thisAccount = bank.getAccount(thisPlayer);
				
				scores[i] =   thisAccount.getCash() 
							- thisAccount.getDebit()
							+ getPlayerSharesPrice(thisPlayer);
				
				names[i] = thisPlayer;
				i++;
				count ++;
			}
			
			// bubble sort		
			for (i=0;i<count;i++)
				for (j=0;j<count-1-i;j++)
					if ( scores[j] < scores[j+1] ){
						
						tempScore 	= scores[j];
						scores[j] 	= scores[j+1];
					 	scores[j+1] = tempScore;
					 	
					 	tempPlayer 	= names[j];
						names[j] 	= names[j+1];
					 	names[j+1] 	= tempPlayer;
				 	}
			
			for (i=0;i<count;i++)		
				winners += resize(names[i],8) + " " + trunc(scores[i],2,8) + "\n";
	
		synchronized(clients){
					
			players = clients.elements();	
			while(players.hasMoreElements()){
				
				thisClient = (SMGClient)players.nextElement();
				thisClient.refreshWinnersListCB(winners);			
			}
		}
	}
	
	// callback
	public void broadcastCallback(String stm)
		throws RemoteException{
		Enumeration en;

	  	synchronized(clients){	
			en = clients.elements();		  	
			while(en.hasMoreElements())
				((SMGClient)en.nextElement()).addActionCB(stm);
		}
	}
	
	// callback
	private void addToHistoryCallback(String player, String symbol, String type, int shareNo, float sharePrice)
		throws RemoteException{
		
		if(clients.containsKey(player)){
			SMGClient aClient = (SMGClient)clients.get(player);
			aClient.addToHistoryCB(symbol, type, shareNo, sharePrice);
		}
	}
	
	// resize 
	private String resize(String str, int len){
	
		if (str.length() > len)
			
			return str.substring(0,len-2)+"..";
		
		else {
			int i;
			int count= len - str.length();
			String ret = str;
			for (i=0;i<count ;i++)
				ret+=" ";
						
			return ret;
		}
	}
	
	// truncate
	private String trunc(float val,int length,int charCount){
		
		String result = new Float(val).toString();
		String ret;
		int i;
		
		for(i=0; i < result.length() && result.charAt(i) != '.'; i++);
		
		if (i+length>=result.length())
			ret = result;
		else 
			ret = result.substring(0,i+length+1);
			
		
		if (ret.length() > charCount)
			
			return ret.substring(0,charCount);
		
		else {
			int count= charCount - ret.length();
			for (i=0;i<count ;i++)
				ret+=" ";	
			return ret;
		}
	}

	// run method
	public void run(){

		Enumeration symbols;
		Enumeration sPlayer;
		Enumeration bPlayer;
		Hashtable sellTable;
		Hashtable buyTable;
		String thisSymbol;
		String sp;
		String bp;
		Bid b;
		int sellShareNo, sellShareNoTemp;
		int buyShareNo, buyShareNoTemp;
		int acceptedShares;
		
		try{
		// loop for ever
			while(true){
				symbols = symbolsTable.keys();
				
				// trade among players
				while(symbols.hasMoreElements()){
					
					thisSymbol = (String)symbols.nextElement();
					b = (Bid)symbolsTable.get(thisSymbol);
					
					sellTable = b.getSellBids();
					sPlayer = sellTable.keys();
					
					while(sPlayer.hasMoreElements() && getBuyBidShares(thisSymbol) > 0){

						sp = (String)sPlayer.nextElement();
						sellShareNo = ((Integer)sellTable.get(sp)).intValue();
						sellShareNoTemp = sellShareNo;
						
						while(sellShareNo > 0 && getBuyBidShares(thisSymbol) > 0){

							buyTable = b.getBuyBids();
							bPlayer = buyTable.keys();
						
							bp = (String)bPlayer.nextElement();
							buyShareNo = ((Integer)buyTable.get(bp)).intValue();
							buyShareNoTemp = buyShareNo;

							if(sellShareNo > buyShareNo){
								if(sell(sp, thisSymbol, buyShareNo)){
									if(buy(bp, thisSymbol, buyShareNo)){

										sellShareNo -= buyShareNo;
										buyShareNo = 0;
										
										b.removeBid(bp, SMG.buy);
										b.removeBid(sp, SMG.sell);
										b.setBid(sp, sellShareNo, SMG.sell);
										
										// callback
										refreshAvailableSharesCallback(thisSymbol);
										refreshBalanceCallback(bp);
										refreshBalanceCallback(sp);
										refreshWinnersListCallback();
										addToHistoryCallback(bp, thisSymbol, SMG.buy, 
													   		 buyShareNoTemp,
													  		 cm.getCurrentPrice(thisSymbol));
									    addToHistoryCallback(sp, thisSymbol, SMG.sell, 
									   						 buyShareNoTemp,
									   						 cm.getCurrentPrice(thisSymbol));
										broadcastCallback(bp + " bought " 
															 + buyShareNoTemp
															 + " shares of " + thisSymbol
															 + " from " + sp);
									}
									else
										// undo the sell operation
										buy(sp, thisSymbol, buyShareNo);
								}
							}
							
							else if(sellShareNo < buyShareNo){

								if(sell(sp, thisSymbol, sellShareNo)){
									if(buy(bp, thisSymbol, sellShareNo)){
								
										buyShareNo -= sellShareNo;
										sellShareNo =0;
										
										b.removeBid(bp, SMG.buy);
										b.removeBid(sp, SMG.sell);
										b.setBid(bp, buyShareNo, SMG.buy);
										
										// callback
										refreshAvailableSharesCallback(thisSymbol);
										refreshBalanceCallback(bp);
										refreshBalanceCallback(sp);
										refreshWinnersListCallback();
										addToHistoryCallback(bp, thisSymbol, SMG.buy, 
													   		 sellShareNoTemp,
													  		 cm.getCurrentPrice(thisSymbol));
									    addToHistoryCallback(sp, thisSymbol, SMG.sell, 
									   						 sellShareNoTemp,
									   						 cm.getCurrentPrice(thisSymbol));
										broadcastCallback(bp + " bought " 
															 + sellShareNoTemp
															 + " shares of " + thisSymbol
															 + " from " + sp);
									}
									else
										// undo the sell operation
										buy(sp, thisSymbol, sellShareNo);
								}
							}
							
							else{

								if(sell(sp, thisSymbol, sellShareNo)){
									if(buy(bp, thisSymbol, sellShareNo)){
								
										buyShareNo = 0;
										sellShareNo = 0;
										
										b.removeBid(bp, SMG.buy);
										b.removeBid(sp, SMG.sell);
										
										// callback
										refreshAvailableSharesCallback(thisSymbol);
										refreshBalanceCallback(bp);
										refreshBalanceCallback(sp);
										refreshWinnersListCallback();
										addToHistoryCallback(bp, thisSymbol, SMG.buy, 
													   		 buyShareNoTemp,
													  		 cm.getCurrentPrice(thisSymbol));
									    addToHistoryCallback(sp, thisSymbol, SMG.sell, 
									   						 buyShareNoTemp,
									   						 cm.getCurrentPrice(thisSymbol));
										broadcastCallback(bp + " bought " 
															 + buyShareNoTemp
															 + " shares of " + thisSymbol
															 + " from " + sp);
									}
									else
										// undo the sell operation
										buy(sp, thisSymbol, sellShareNo);
								}
							}
						}
					
						sellTable = b.getSellBids();
						sPlayer = sellTable.keys();
					}		
				}
				
				// Trade among players and companies
				symbols = symbolsTable.keys();
				
				while(symbols.hasMoreElements()){

					thisSymbol = (String)symbols.nextElement();
					b = (Bid)symbolsTable.get(thisSymbol);
					
					// sell operation
					sellTable = b.getSellBids();
					sPlayer = sellTable.keys();
					while(sPlayer.hasMoreElements()){

						sp = (String)sPlayer.nextElement();
						sellShareNo = ((Integer)sellTable.get(sp)).intValue();
						
						acceptedShares = cm.acceptBid(thisSymbol, sellShareNo, SMG.sell);
					
						if(acceptedShares > 0){
							if(sell(sp, thisSymbol, acceptedShares)){
								cm.incShares(thisSymbol, acceptedShares);
							
								sellShareNo -= acceptedShares;
								b.removeBid(sp, SMG.sell);
								if(sellShareNo > 0)
									b.setBid(sp, sellShareNo, SMG.sell);
									
								// callback	
								refreshAvailableSharesCallback(thisSymbol);
								refreshBalanceCallback(sp);
								refreshWinnersListCallback();
								addToHistoryCallback(sp, thisSymbol, SMG.sell, 
													 acceptedShares,
													 cm.getCurrentPrice(thisSymbol));		
								broadcastCallback(thisSymbol +" bought "+ acceptedShares +" shares from "+ sp);
							}
						}
					}
					
					// buy operation
					buyTable = b.getBuyBids();
					bPlayer = buyTable.keys();
					while(bPlayer.hasMoreElements()){

						bp = (String)bPlayer.nextElement();
						buyShareNo = ((Integer)buyTable.get(bp)).intValue();
						
						acceptedShares = cm.acceptBid(thisSymbol, buyShareNo, SMG.buy);
						
						if(acceptedShares > 0){
							if(buy(bp, thisSymbol, acceptedShares)){
								if(cm.decShares(thisSymbol, acceptedShares)){
							
									buyShareNo -= acceptedShares;
									b.removeBid(bp, SMG.buy);
									if(buyShareNo > 0)
										b.setBid(bp, buyShareNo, SMG.buy);
									
									// callback	
									refreshAvailableSharesCallback(thisSymbol);
									refreshBalanceCallback(bp);
									refreshWinnersListCallback();
									addToHistoryCallback(bp, thisSymbol, SMG.buy, 
													     acceptedShares,
													     cm.getCurrentPrice(thisSymbol));
									broadcastCallback(bp +" bought "+ acceptedShares +" shares from "+ thisSymbol);
								}
								else
									// undo the buy operation
									sell(bp, thisSymbol, acceptedShares);
							}
						}
					}
				}
				
				// sleep a specified amount of time (about 0.5 second)
				Thread.sleep(SMG.brokerSleepTimerPeriod);
				
			}
		}
		catch(InterruptedException e){
			System.err.println(e.getMessage());
		}
		catch(RemoteException e){
			System.err.println(e.getMessage());
		}
	}		
}
