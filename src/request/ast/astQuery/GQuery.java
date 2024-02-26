package request.ast.astQuery;

import request.ast.Cont;
import request.ast.Gather;
import request.ast.Query;
import request.ast.interfaces.IASTvisitor;

public class GQuery extends Query<Object>{
	
	public GQuery (Gather gather,Cont cont) {
		super(gather,cont);
	}
	 
	 public Gather getGather() {
		 return (Gather)this.expr;
	 }
		
	 
	 @Override
		public <Result, Data, Anomaly extends Throwable> 
	    Result eval(IASTvisitor<Result, Data, Anomaly> visitor, Data data)
	            throws Anomaly {
	        return visitor.visit(this, data);
	    }

}
