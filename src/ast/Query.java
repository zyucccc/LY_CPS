package ast;


public abstract class Query<T> {
	private Cont cont;
	private T expr;
	
	public Query (T expr,Cont cont) {
		this.cont = cont;
		this.expr = expr;
	}
	
	
}
