package request.ast.astBexp;

import request.ast.Bexp;
import request.ast.interfaces.IASTvisitor;

public class NotBExp extends Bexp{
	private Bexp bexp;
	
	public NotBExp(Bexp b) {
		this.bexp=b;
	}
	
	public Bexp getBexp() {
		return bexp;
	}
	
	 @Override
		public <Result, Data, Anomaly extends Throwable> 
	    Result eval(IASTvisitor<Result, Data, Anomaly> visitor, Data data)
	            throws Anomaly {
	        return visitor.visit(this, data);
	    }
}
