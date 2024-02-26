package request.ast.astRand;

import request.ast.Rand;
import request.ast.interfaces.IASTvisitor;

public class CRand extends Rand{
	private double val;
	
	public CRand (double val) {
		this.val = val;
	}
	
	public double getVal() {
		return this.val;
	}

	 @Override
		public <Result, Data, Anomaly extends Throwable> 
	    Result eval(IASTvisitor<Result, Data, Anomaly> visitor, Data data)
	            throws Anomaly {
	        return visitor.visit(this, data);
	    }

}
