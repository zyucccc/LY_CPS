package ast.astBexp;

import ast.Bexp;

public class OrBExp extends Bexp{
	private  Bexp bexp1 ;
	private  Bexp bexp2 ;
	
	public OrBExp(Bexp b1,Bexp b2) {
		super(b1,b2);
	}
	
	public Bexp getBexp1() {
		return bexp1;
	}
	public Bexp getBexp2() {
		return bexp2;
	}
}
