package request.ast.astQuery;

import request.ast.Bexp;
import request.ast.Cont;
import request.ast.Query;
import request.ast.interfaces.IASTvisitor;

public class BQuery extends Query<Object>{
	
	public BQuery (Bexp bexp,Cont cont) {
		super(bexp,cont);
	}
	
	
	 public Bexp getBexp() {
		 return (Bexp)this.expr;
	 }
	 
	 @Override
		public <Result, Data, Anomaly extends Throwable> 
	    Result eval(IASTvisitor<Result, Data, Anomaly> visitor, Data data)
	            throws Anomaly {
	        return visitor.visit(this, data);
	    }

}
