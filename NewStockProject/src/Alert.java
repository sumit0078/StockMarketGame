
// Alert

public class  Alert{
	public String symbol;
	public String player;
	public float lowPrice;
	public float upperPrice;
	
	// Constructor: initiate variables
	public Alert(String symbol, String player, float lowPrice, float upperPrice){
		this.symbol = symbol;
		this.player = player;
		this.lowPrice = lowPrice;
		this.upperPrice = upperPrice;
	}
	
	// test alert
	public float testAlert(float curPrice){
		if(curPrice <= lowPrice)
			return lowPrice;
		if(curPrice >= upperPrice)
			return curPrice;
		return -1;
	}
}
