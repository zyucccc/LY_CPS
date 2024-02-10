package request.ast.astRand;

import request.ast.Rand;

public class CRand extends Rand{
	private double val;
	
	public CRand (double val) {
		this.val = val;
	}
	
	public double getVal() {
		return this.val;
	}


}
