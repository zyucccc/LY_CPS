package ast.astBexp;

import ast.Bexp;

public class AndBExp extends Bexp{
	
	private final Bexp bexp1 ;
	private final Bexp bexp2 ;
	
	public AndBExp(Bexp b1,Bexp b2) {
		this.bexp1 = b1;
		this.bexp2 = b2;
	}
	
	public Bexp getBexp1() {
		return bexp1;
	}
	public Bexp getBexp2() {
		return bexp2;
	}
}
