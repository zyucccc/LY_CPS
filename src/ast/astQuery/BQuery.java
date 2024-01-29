package ast.astQuery;

import ast.Bexp;
import ast.Cont;
import ast.Gather;
import ast.Query;

public class BQuery extends Query<Object>{
	
	public BQuery (Bexp bexp,Cont cont) {
		super(bexp,cont);
	}
	
	
	 public Bexp getBexp() {
		 return (Bexp)this.expr;
	 }

}
