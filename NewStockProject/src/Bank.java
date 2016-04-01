
// Bank

import java.util.*;

public class Bank{
	
	private Hashtable accounts;
	
	// Constructor
	public Bank(){
		accounts = new Hashtable();		
	}
	
	// new account
	public boolean newAccount(String player){
		synchronized (accounts){
			if(accounts.size() >= SMG.maxPlayersNo)
				return false;
			if(accounts.containsKey(player))
				return false;
			
			accounts.put(player, new Account());
			return true;
		}		
	}
	
	// remove account
	public boolean removeAccount(String player){
		synchronized (accounts){
			if(accounts.containsKey(player)){	
				accounts.remove(player);
				return true;
			}
			return false;
		}		
	}
		
	// get account
	public Account getAccount(String player){
		if(accounts.containsKey(player))
			return (Account)accounts.get(player);
		return null;
	} 	
	
	// borrow
	public boolean borrow (String player, float val){
		
		Account thisAccount;
		thisAccount = getAccount(player);
		
		if(thisAccount != null){
			synchronized(thisAccount){
				return thisAccount.borrow(val);
			}
		}
		return false;
	}
	
	// repay
	public boolean repay (String player, float val){
		
		Account thisAccount;
		thisAccount = getAccount(player);
		
		if(thisAccount != null){
			synchronized(thisAccount){
				return thisAccount.repay(val);
			}
		}
		return false;
	}
	
	// timer
	public void timer(){
		synchronized(accounts){
			Enumeration en = accounts.elements();
			Account thisAccount;

			while(en.hasMoreElements()){
				thisAccount = (Account)en.nextElement();
				thisAccount.computeInterest();
			}
		}
	}
}
