package request.ast;

import request.ast.interfaces.IASTvisitable;
import request.ast.interfaces.IASTvisitor;

public enum Dir implements IASTvisitable{
	 NE, NW, SE, SW;
	
	public Dir getDirection() {
        return this;
    }
	
	 @Override
		public <Result, Data, Anomaly extends Throwable> 
	    Result eval(IASTvisitor<Result, Data, Anomaly> visitor, Data data)
	            throws Anomaly {
	        return visitor.visit(this, data);
	    }
}
