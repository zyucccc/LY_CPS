package ast.astQuery;

import ast.Cont;
import ast.Gather;
import ast.Query;

public class GQuery extends Query<Object>{
	
	public GQuery (Gather gather,Cont cont) {
		super(gather,cont);
	}
	 
	 public Gather getGather() {
		 return (Gather)this.expr;
	 }
		

}
