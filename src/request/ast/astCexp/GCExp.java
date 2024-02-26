package request.ast.astCexp;

import request.ast.Cexp;
import request.ast.Rand;
import request.ast.interfaces.IASTvisitor;

public class GCExp extends Cexp{

	public GCExp(Rand r1, Rand r2) {
		super(r1, r2);
		// TODO Auto-generated constructor stub
	}
	
	 @Override
		public <Result, Data, Anomaly extends Throwable> 
	    Result eval(IASTvisitor<Result, Data, Anomaly> visitor, Data data)
	            throws Anomaly {
	        return visitor.visit(this, data);
	    }

}
