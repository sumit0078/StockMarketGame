
// Company

public class Company{
	
	private float price;
	private int shares;
	private Chart chart;
	
	// Constructor
	public Company(){
		price = SMG.initialPrice;
		shares = SMG.companyShares;
		chart = new Chart(SMG.initialPrice);
	}
	
	// mutator: set current price
	public void setCurrentPrice(float pr){
		price = pr;
		chart.update(price);
	}
	
	// mutator: increment shares
	public void incShares(int val){
		shares += val;
	}
	
	// mutator: decrement shares
	public boolean decShares(int val){
		if( shares >= val){
			shares -= val;
			return true;
		}
		return false;
	}
	
	// Accessor
	public float getCurrentPrice(){
		return price;
	}
	
	// Accessor
	public int getSharesNo(){
		return shares;
	}
	
	// Accessor
	public Chart getChart(){
		return chart;
	}
	
	// acceptBid
	public int acceptBid(int shareNo, String bidType){
		
		if(bidType.equals(SMG.sell)){
			if(shares > SMG.companyShares*98/100)
				return Math.random()<0.005?(int)(Math.random()*shareNo + 1):0;
			else
				return Math.random()<0.05?(int)(Math.random()*shareNo + 1):0;
		}
	
		else{ 	// buy
			if(shares > SMG.companyShares*98/100)
				return shareNo>50?((int)(Math.random()*shareNo)):shareNo;
			else if((shares - shareNo) < SMG.companyShares*5/100)
				return 0;
			else
				return Math.random()<0.05?(int)(Math.random()*shareNo + 1):0;	
		}	
	}	
}