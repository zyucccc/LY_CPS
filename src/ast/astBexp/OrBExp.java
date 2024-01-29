package ast.astBexp;

import ast.Bexp;

public class OrBExp extends Bexp{
	private  Bexp bexp1 ;
	private  Bexp bexp2 ;
	
	public OrBExp(Bexp bexp1,Bexp bexp2) {
		this.bexp1=bexp1;
		this.bexp2=bexp2;
	}
	public Bexp getBexp1() {
		return bexp1;
	}
	public Bexp getBexp2() {
		return bexp2;
	}
}
