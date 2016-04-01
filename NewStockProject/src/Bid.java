
// Bid class for setting bids

import java.util.*;

public class Bid{

	private Hashtable sellTable;
	private Hashtable buyTable;
	
	public Bid(){
		sellTable = new Hashtable();		
		buyTable = new Hashtable();		
	}

	// Method to set new Bid
	public void setBid(String player, int shareNo, String bidType){
	
		synchronized(sellTable){
			synchronized(buyTable){
			
				if (bidType.equals(SMG.sell)){
					
					if(buyTable.containsKey(player))
						buyTable.remove(player);
					
					else if (sellTable.containsKey(player))
						sellTable.remove(player);
					
					sellTable.put(player, new Integer(shareNo));
				}	 
				
				else {
					
					if(sellTable.containsKey(player))
						sellTable.remove(player);
					
					else if (buyTable.containsKey(player))
						buyTable.remove(player);
					
					buyTable.put(player, new Integer(shareNo));
				}
			}		
		}			
	}
	
	// remove bid
	public boolean removeBid(String player, String bidType){
		
		
		if (bidType.equals(SMG.sell)){
			synchronized(sellTable){
				if(sellTable.containsKey(player)){
					sellTable.remove(player);
					return true;
				}
			}
		}
		
		else if(bidType.equals(SMG.buy)){
			synchronized(buyTable){
				if(buyTable.containsKey(player)){
					buyTable.remove(player);
					return true;
				}
			}
		}
		return false;
	}
	
	// get sell bid shares 
	public int getSellBidShares(){
		
		synchronized(sellTable){
			int total = 0;
			Enumeration en = sellTable.elements();
			
			while(en.hasMoreElements())
				total += ((Integer)en.nextElement()).intValue();
			return total;
		}
	}
	
	// get buy bid shares 
	public int getBuyBidShares(){
		
		synchronized(buyTable){
			int total = 0;
			Enumeration en = buyTable.elements();
			
			while(en.hasMoreElements())
				total += ((Integer)en.nextElement()).intValue();
			return total;
		}
	}
	
	// Accessor: get sell bids
	public Hashtable getSellBids(){
		return sellTable;
	}
	
	// Accessor: get buy bids
	public Hashtable getBuyBids(){
		return buyTable;
	}
}