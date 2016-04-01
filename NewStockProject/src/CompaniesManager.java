
// CompaniesManager

import java.util.*;

public class CompaniesManager{
	
	// There are three to ten companies
	private String[] comps;

	private Hashtable companies;
	
	// Constructor	
	public CompaniesManager(String[] comps){
		
		this.comps = comps;
		companies = new Hashtable();
		
		for (int i=0;i<comps.length;i++)
			companies.put(comps[i],new Company());
	}
	
	// getCompaniesNames
	public String[] getCompaniesNames(){
		return comps;
	}
	
	// getCompanies
	public Hashtable getCompanies(){
		return companies;
	}
	
	// Accessor
	public float getCurrentPrice(String symbol){
		if(companies.containsKey(symbol))
			return ((Company)(companies.get(symbol))).getCurrentPrice();
		return 0;	
	}
	
	// Accessor
	public Hashtable getAllPrices(){
		Hashtable ht = new Hashtable();
		Enumeration en = companies.keys();
		String symbol;
		float val;
		while(en.hasMoreElements()){
			symbol = (String)en.nextElement();
			val = ((Company)companies.get(symbol)).getCurrentPrice();
			ht.put(symbol, new Float(val));
		}
		return ht;
	}
	
	// Accessor
	public int getSharesNo(String symbol){
		if(companies.containsKey(symbol))
			return ((Company)(companies.get(symbol))).getSharesNo();
		return 0;	
	}
	
	// Accessor
	public float[] getChart(String symbol){
		if(companies.containsKey(symbol))
			return ((Company)(companies.get(symbol))).getChart().getChart();
		return null;
	}
	
	// increment shares
	public void incShares(String symbol, int val){
		if(companies.containsKey(symbol))
			((Company)(companies.get(symbol))).incShares(val);
	}
	
	// decrement shares
	public boolean decShares(String symbol, int val){
		if(companies.containsKey(symbol))
			return ((Company)(companies.get(symbol))).decShares(val);
		return false;
	}

	// acceptBid
	public int acceptBid(String symbol, int shareNo, String bidType){
		if(companies.containsKey(symbol))
			return ((Company)(companies.get(symbol))).acceptBid(shareNo, bidType);
		return 0;
	}
}