package request.ast.astQuery;

import request.ast.Bexp;
import request.ast.Cont;
import request.ast.Query;

public class BQuery extends Query<Object>{
	
	public BQuery (Bexp bexp,Cont cont) {
		super(bexp,cont);
	}
	
	
	 public Bexp getBexp() {
		 return (Bexp)this.expr;
	 }

}
