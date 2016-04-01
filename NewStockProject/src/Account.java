
// Account

import java.util.*;
import java.io.*;

public class  Account implements Serializable{
		
	private float cash;
	private float debit;
	
	// Constructor
	public Account(){
		cash = SMG.initialCash;
		debit = 0;
	}
	
	// mutator: increment cash
	public void incCash(float val){
			cash += val;
	}
	
	// mutator: decrement cash
	public boolean decCash(float val){
		if( cash >= val){
			cash -= val;
			return true;
		}
		return false;
	}
	
	// mutator: increment debits
	public boolean borrow(float val){
		if(debit + val > SMG.maxDebit)
			return false;
			
		debit += val;
		cash += val;
		return true;
	}
	
	// mutator: decrement debits
	public boolean repay(float val){
		float newVal=val;
		
		if(cash - newVal < 0)
			return false;
			
		if(debit - newVal < 0)
			newVal = debit;
			
		debit -= newVal;
		cash -= newVal;
		return true;
	}
	
	// mutator: compute interests
	public void computeInterest(){	
		cash += cash * SMG.cashInterest;
		debit += debit * SMG.debitInterest;
	}
	
	// Accessor
	public float getCash(){
		return cash;
	}
	
	// Accessor
	public float getDebit(){
		return debit;
	}
}