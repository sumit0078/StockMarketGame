
// Chart

public class Chart{
	
	private float[] prices;
	int i;
	
	// Constructor
	public Chart(float initialPrice){
		prices = new float[SMG.timeScale+1];	
		prices[SMG.timeScale] = initialPrice;
		for(i=0; i<SMG.timeScale; i++)
			prices[i] = 0;
	}
	
	// get chart
	public float[] getChart(){
		return prices;
	}

	// update chart
	public void update(float newPrice){
		
		for(i=0; i<SMG.timeScale; i++)
			prices[i] = prices[i+1];			
		prices[i] = newPrice;
	}	
}