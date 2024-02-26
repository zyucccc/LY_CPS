package request.ast.astCexp;

import request.ast.Cexp;
import request.ast.Rand;
import request.ast.interfaces.IASTvisitor;

public class EQCExp extends Cexp{

	public EQCExp(Rand r1, Rand r2) {
		super(r1, r2);
	}
	 @Override
		public <Result, Data, Anomaly extends Throwable> 
	    Result eval(IASTvisitor<Result, Data, Anomaly> visitor, Data data)
	            throws Anomaly {
	        return visitor.visit(this, data);
	    }
}
