package request.ast;

import request.ast.interfaces.IASTvisitable;
import request.ast.interfaces.IASTvisitor;

import java.io.Serializable;

public enum Direction implements IASTvisitable, Serializable {

	 NE, NW, SE, SW;

	private static final long serialVersionUID = 1L;
	public Direction getDirection() {
        return this;
    }
	
	 @Override
		public <Result, Data, Anomaly extends Throwable> 
	    Result eval(IASTvisitor<Result, Data, Anomaly> visitor, Data data)
	            throws Anomaly {
	        return visitor.visit(this, data);
	    }
}
