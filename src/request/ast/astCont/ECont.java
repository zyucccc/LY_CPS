package request.ast.astCont;

import request.ast.Cont;
import request.ast.interfaces.IASTvisitor;

public class ECont extends Cont{
	public ECont() {
		
	}
	 @Override
		public <Result, Data, Anomaly extends Throwable> 
	    Result eval(IASTvisitor<Result, Data, Anomaly> visitor, Data data)
	            throws Anomaly {
	        return visitor.visit(this, data);
	    }
}