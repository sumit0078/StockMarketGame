
// Constants global variables

public class SMG {
		
	// Constants in: SMGServerImpl class
	public static final int rmiPort = 1099;
	public static final String rmiHost = "127.0.0.1";
	public static final int varUpdateTimerPeriod = 30; // seconds
	public static int gameTime = 5*60; // seconds

	// Constants in: Bank class
	public static final int maxAccountNo = 10;
	
	// Constants in: Account class
	public static final float initialCash = 1000;
	public static final float maxDebit = 1000;
	public static final float cashInterest = (float)0.01;
	public static final float debitInterest = (float)0.05;
	
	// Constants in: Chart, SMGComputerPlayer classes
	public static final int timeScale = 15;
	
	// Constants in: Exchange class
	public static final int computerPlayersNo = 2;
	public static final int brokerSleepTimerPeriod = 500; // milli seconds
	
	// Constants in: Company class
	public static final float initialPrice =30;
	public static final int companyShares = 1000;
	public static final int maxPlayersNo =10;

	// Constants in: Bid, Exchange, Company classes
	public static final String sell = "sell";
	public static final String buy = "buy";
	
	// Constants in: StartServer, Exchange
	public static final String computer1 = "Comp1";
	public static final String computer2 = "Comp2";

	// Constants in: startServer class
	public static final String companies[] = {"Sun","Oracle","Microsoft","Cisco","Intel","IBM"}; 	

}
