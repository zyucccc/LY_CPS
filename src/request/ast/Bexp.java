package request.ast;

import request.ast.interfaces.IASTvisitable;
import request.ast.interfaces.IASTvisitor;

public abstract class Bexp implements IASTvisitable {
	protected Bexp bexp1;
	protected Bexp bexp2;
	
//	public Bexp (Bexp bexp1,Bexp bexp2) {
//		this.bexp1=bexp1;
//		this.bexp2=bexp2;
//	}
//	
//	public Bexp (Bexp bexp1) {
//		this.bexp1=bexp1;
//	}
	
	 @Override
		public <Result, Data, Anomaly extends Throwable> 
	    Result eval(IASTvisitor<Result, Data, Anomaly> visitor, Data data)
	            throws Anomaly {
	        return visitor.visit(this, data);
	    }

}
