
// PlayersList

import java.util.*;

public class PlayerShares{
	
	Hashtable s;
		
	// Constructor: initiate variables
	public PlayerShares(){
		s = new Hashtable();
	}
	
	// mutator: increment shares
	public void incShares(String symbol, int val){
		
		synchronized(s){
			int newVal = val;
			if(s.containsKey(symbol)){
				newVal += ((Integer)s.get(symbol)).intValue();	
				s.remove(symbol);
			}
			s.put(symbol, new Integer(newVal));
		}
	}
	
	// mutator: decrement shares
	public boolean decShares(String symbol, int val){
	
		synchronized(s){
			int sharesNo;
			if(s.containsKey(symbol)){
				sharesNo = ((Integer)s.get(symbol)).intValue();	
				if( sharesNo >= val){
					sharesNo -= val;
					s.remove(symbol);
					if(sharesNo > 0)
						s.put(symbol, new Integer(sharesNo));
					return true;
				}
			}
			return false;
		}
	}
	
	// getSharesNo
	public int getSharesNo(String symbol){
		
		if(s.containsKey(symbol))
			return ((Integer)s.get(symbol)).intValue();	
		return 0;
	}
	
	// getAllShares
	public Hashtable getAllShares(){
			return s;
	}
}
