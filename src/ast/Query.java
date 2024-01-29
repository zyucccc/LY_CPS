package ast;

import ast.interfaces.IASTvisitable;
import ast.interfaces.IASTvisitor;

public abstract class Query<T> implements IASTvisitable {
	protected Cont cont;
	protected T expr;
	
	public Query (T expr,Cont cont) {
		this.cont = cont;
		this.expr = expr;
	}
	
//	public abstract Cont getCont();
	public Cont getCont() {
		return this.cont;
	}
	
	  @Override
		public <Result, Data, Anomaly extends Throwable> 
	    Result eval(IASTvisitor<Result, Data, Anomaly> visitor, Data data)
	            throws Anomaly {
	        return visitor.visit(this, data);
	    }
	
}
