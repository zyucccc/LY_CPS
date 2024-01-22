package ast;

public abstract class Bexp {
	private Bexp bexp1;
	private Bexp bexp2;
	
	public Bexp (Bexp bexp1,Bexp bexp2) {
		this.bexp1=bexp1;
		this.bexp2=bexp2;
	}
	
	public Bexp (Bexp bexp1) {
		this.bexp1=bexp1;
	}

}
