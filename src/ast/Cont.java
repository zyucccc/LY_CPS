package ast;

import ast.interfaces.IASTvisitable;
import ast.interfaces.IASTvisitor;

public abstract class Cont implements IASTvisitable{
	
	 @Override
		public <Result, Data, Anomaly extends Throwable> 
	    Result eval(IASTvisitor<Result, Data, Anomaly> visitor, Data data)
	            throws Anomaly {
	        return visitor.visit(this, data);
	    }

}
