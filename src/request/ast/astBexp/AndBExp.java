package request.ast.astBexp;

import request.ast.Bexp;

public class AndBExp extends Bexp{
	
	private  Bexp bexp1 ;
	private  Bexp bexp2 ;
	
	public AndBExp (Bexp bexp1,Bexp bexp2) {
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