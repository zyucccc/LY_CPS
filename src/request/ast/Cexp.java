package request.ast;
import request.ast.interfaces.IASTvisitable;
import request.ast.interfaces.IASTvisitor;

import java.io.Serializable;

public abstract class Cexp implements IASTvisitable, Serializable {
	private static final long serialVersionUID = 1L;
	private Rand rand1;
	private Rand rand2;
	
	public Cexp(Rand r1, Rand r2) {
		this.rand1=r1;
		this.rand2=r2;
	}
	public  Rand getRand1() {
		return rand1;
	}
	public  Rand getRand2() {
		return rand2;
	}
	
	 @Override
		public <Result, Data, Anomaly extends Throwable> 
	    Result eval(IASTvisitor<Result, Data, Anomaly> visitor, Data data)
	            throws Anomaly {
	        return visitor.visit(this, data);
	    }
}
