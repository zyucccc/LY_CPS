package ast;


public abstract class Query<T> {
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
	
	
	
}
