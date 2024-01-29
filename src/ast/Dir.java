package ast;

import ast.interfaces.IASTvisitable;
import ast.interfaces.IASTvisitor;

public enum Dir implements IASTvisitable{
	 NE, NW, SE, SW;
	
	 @Override
		public <Result, Data, Anomaly extends Throwable> 
	    Result eval(IASTvisitor<Result, Data, Anomaly> visitor, Data data)
	            throws Anomaly {
	        return visitor.visit(this, data);
	    }
}
