package ast.astBexp;

import ast.Bexp;
import ast.Cexp;

public class CExpBExp extends Bexp{
	private Cexp cexp;
	
	public CExpBExp(Cexp cexp) {
		this.cexp = cexp;
	}
	
	public Cexp getCexp() {
		return this.cexp;
	}

}
