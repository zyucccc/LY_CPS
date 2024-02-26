package request.ast.astCont;

import request.ast.Base;
import request.ast.Cont;
import request.ast.interfaces.IASTvisitor;

public class FCont extends Cont{
	private Base base;
	private double distaneMax;
	
	public FCont(Base base,double dM) {
		this.base=base;
		this.distaneMax=dM;
	}
	public Base  getBase() {
		return base;
	}
	public double getDistanceMax() {
		return distaneMax;
	}
	
	 @Override
		public <Result, Data, Anomaly extends Throwable> 
	    Result eval(IASTvisitor<Result, Data, Anomaly> visitor, Data data)
	            throws Anomaly {
	        return visitor.visit(this, data);
	    }
}
