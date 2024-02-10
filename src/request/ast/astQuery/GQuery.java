package request.ast.astQuery;

import request.ast.Cont;
import request.ast.Gather;
import request.ast.Query;

public class GQuery extends Query<Object>{
	
	public GQuery (Gather gather,Cont cont) {
		super(gather,cont);
	}
	 
	 public Gather getGather() {
		 return (Gather)this.expr;
	 }
		

}
