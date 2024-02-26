package request.ast.astBexp;

import request.ast.Bexp;
import request.ast.Cexp;
import request.ast.interfaces.IASTvisitor;

public class CExpBExp extends Bexp{
	private Cexp cexp;
	
	public CExpBExp(Cexp cexp) {
		this.cexp = cexp;
	}
	
	public Cexp getCexp() {
		return this.cexp;
	}
	 @Override
		public <Result, Data, Anomaly extends Throwable> 
	    Result eval(IASTvisitor<Result, Data, Anomaly> visitor, Data data)
	            throws Anomaly {
	        return visitor.visit(this, data);
	    }

}
