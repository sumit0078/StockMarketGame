
// SMGComputerPlayer

import java.util.*;
import java.lang.*;

public class ComputerPlayer extends Thread {
	
	private final int maxBuyShares = 15;
		
	private String[] weightedCompanies;
	private String[] companies;
	private float [] prices;
	private double [] weights;
	
	private String myName;
	private Exchange exchange;
	private CompaniesManager compsManager;
	private Bank bank;
	private Account account;
	private int level;
	private int index = 0;
	private int size;
	private int sleepTime;
		
	public ComputerPlayer(String myName, Exchange exchange,
							 CompaniesManager compsManager, Bank bank, int level){
		
		this.myName = myName;
		this.exchange = exchange;
		this.compsManager = compsManager;
		this.bank = bank;
		this.level = level;
		
			
		// register
		while(exchange.register(myName) < 0);
		
		account = bank.getAccount(myName);
		size = compsManager.getCompaniesNames().length;
		weightedCompanies = new String[size];
		companies = new String[size];
		prices = new float[size];
		weights = new double[size];
	}
	
	public void run(){
	
		try{	
			sleepTime = getSleepTime();
			sleep(40*1000);
			
			while(true){
			
				getCompniesPrices();
				computeWeights();
					
				if (Math.random()>0.7){ //buy
					if (Math.random()<0.8)
						buy(weightedCompanies[0], getWeight(weightedCompanies[0]));
					else 
						buy(weightedCompanies[1], getWeight(weightedCompanies[1]));
					
					index = weightedCompanies.length -1;
					undoBuy( weightedCompanies[index] );
					undoBuy( weightedCompanies[index-1] );	
				}
				
				else {  //sell
					index = weightedCompanies.length-1;
					if (Math.random()<0.8)
						if (sell(weightedCompanies[index]))
							repay();
					else 
						if (sell( weightedCompanies[index-1]))
							repay();
					
					undoSell(weightedCompanies[0]);
					undoSell(weightedCompanies[1]);
				}	
				sleep(sleepTime*1000);
			}	
		}
		catch(InterruptedException ex){
			System.err.println(ex.getMessage());
		}
	}
	
	// buy
	private void buy(String symbol, double weight){
		
		int wantedShares = (int)(Math.abs(weight)*maxBuyShares);
		int index   = indexOf(symbol);
		float price = wantedShares*prices[index];
		int acceptedShares;
		float wantedCash;
		
		if (account.getCash() < price){
			wantedCash = price - account.getCash();
			acceptedShares = (int)((account.getCash()+borrow(wantedCash, weight))/prices[index]);
			setBid(symbol,acceptedShares,SMG.buy);	
		}
		else 
			setBid(symbol,wantedShares,SMG.buy);
		
	}
	
	// sell
	private boolean sell(String symbol){
		
		int sharesNo = exchange.getPlayerShares(symbol, myName);
		if(sharesNo > 0){
			
			return setBid(symbol, sharesNo, SMG.sell);
		}
		return false;			
	}
	
	// undo buy
	private void undoBuy(String symbol){
		exchange.undoTrade(symbol, myName, SMG.buy);
	}
	
	// undo sell
	private void undoSell(String symbol){
		exchange.undoTrade(symbol, myName, SMG.sell);
	}
	
	// set bid
	private boolean setBid(String symbol, int shares,String tradeType){
		return exchange.setBid(symbol, myName, shares, tradeType);		
	}
	
	// borrow
	private float borrow(float wantedCash,double weight){
		
		float acceptedCash = wantedCash*(float)Math.abs(weight);
		if(bank.borrow(myName, acceptedCash))
			return acceptedCash;	
		
		acceptedCash = wantedCash*(float)(Math.abs(weight)*2.0/3.0);
		if(bank.borrow(myName, acceptedCash))
			return acceptedCash;	
		
		acceptedCash = wantedCash*(float)(Math.abs(weight)*2.0/3.0);
		if(bank.borrow(myName, acceptedCash))
			return acceptedCash;	
		
		return 0;
	}
	
	// repay
	private void repay(){
		
		bank.repay(myName, account.getCash()/5);		
	}
	
	// getCompniesPrices
	private void getCompniesPrices(){
		
		Hashtable h = exchange.getAllStockQuotes();
		Enumeration en = h.keys();
		int i = 0;
		
		while(en.hasMoreElements()){
				
			companies[i] = (String)en.nextElement();	
			prices[i] = ((Float)h.get(companies[i])).floatValue();
			i++;
		}
	}
	
	// computeWeights
	private void computeWeights(){
		
		int    i,j;
		double X,X2,SUM_X,SUM_X2, Y, Y2, XY,SUM_Y ,SUM_Y2,SUM_XY;
		double slope;
		double weight;
		double num,den;
		double dissipation;
	
		int N =0;
			
		for (i=0;i<size; i++){
		
			float[] chart = compsManager.getChart(companies[i]);
			
			for (j = 0; j<SMG.timeScale+1 && chart[j]==0; j++);
			N = SMG.timeScale+1-j;
			int base = SMG.timeScale+1-N;
			
			SUM_X  = 0;
			SUM_X2 = 0;
			SUM_Y  = 0;
			SUM_Y2 = 0;
			SUM_XY = 0;
			
			for(j=0;j<N;j++){
						
				X = j;
				Y = chart[base+j];
				X2 = X*X;
				Y2 = Y*Y;
				XY = Y*X;
				SUM_X  += X;
				SUM_Y  += Y;
				SUM_X2 += X2;	
				SUM_Y2 += Y2;
				SUM_XY += XY;
			}
			
			num  = SUM_XY*N - SUM_Y*SUM_X;
			den = SUM_X2*N - SUM_X*SUM_X;
			// slope
			slope = (den>0.0000001?(num/den):100000);
			den = Math.sqrt((SUM_X2*N - SUM_X*SUM_X)*(SUM_Y2*N-SUM_Y*SUM_Y));
			
			// dissipation
			dissipation = (den>0.0000001?(num/den):1.0);
			
			switch (level){
			
				case 1:	// Novice
						weight = ((2*slope+Math.abs(dissipation))*0.1+Math.random()*0.9)*N/50.0;
						break;
				
				case 2: // Basic
						weight = (2*slope+Math.abs(dissipation) + (Math.random()*0.5-0.25))*N/50.0 ;
						break;
				
					
				case 3: // Standard
						weight = (2*slope+Math.abs(dissipation)
								+( (exchange.getBuyBidShares(companies[i])
								   -exchange.getSellBidShares(companies[i]))*0.001)
								+(Math.random()*0.5-0.25)+(1.0-prices[i]/50.0))*N/50.0;
						break;
			
				case 4: // Expert
						weight = (2*slope+Math.abs(dissipation)
								+( (exchange.getBuyBidShares(companies[i])
								   -exchange.getSellBidShares(companies[i]))*0.005)
								+(1.0-prices[i]/40.0))*N/50.0;
						break;
				
				default: //default
						weight = 0;
						break;
			}
			weights[i] = weight;
		}		
		rearrangeWeightes();
	}
	
	// rearrangeWeighted
	private void rearrangeWeightes(){

		double [] tempWeights = new double[size];
		int[] a = new int[size+1];
		double tempWeight;
		int tempa,i,j;
		
		for (i=0;i<size;i++){
			a[i] = i;
			tempWeights[i] = weights[i];
		}
			
		// bubble sort		
		for (i=0;i<companies.length;i++)
			for (j=0;j<companies.length-1-i;j++)
				if ( tempWeights[j] < tempWeights[j+1] ){
					
					tempWeight 		= tempWeights[j];
					tempWeights[j] 	= tempWeights[j+1];
				 	tempWeights[j+1]= tempWeight;
				 	
				 	tempa 	= a[j];
					a[j] 	= a[j+1];
				 	a[j+1] 	= tempa;
			 	}
		
		for (i=0;i<companies.length;i++)
			weightedCompanies[i] = companies[a[i]];
	}
	
	// weight
	private double getWeight(String symbol){
		return weights[indexOf(symbol)];		
	}
	
	// indexOf
	private int indexOf(String symbol){
		
		int i = 0;
		for (i=0; i<companies.length; i++)			
			if (companies[i].equals(symbol))				
				return i;
		return -1;	
	}
	
	// getSleepTime
	private int getSleepTime(){
		
		switch(level){
			
			case 1:return 20;
			case 2:return 15;
			case 3:return 10;
			case 4:return 5;
		}
		return 8;
	}
	
	// restart
	public void die()
		throws java.rmi.RemoteException{
			
		exchange.removePlayer(myName);
		bank.removeAccount(myName);
		stop();
	}
}