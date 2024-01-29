package ast;

public abstract class Cexp {
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
}
