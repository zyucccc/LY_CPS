package ast.astBexp;

import ast.Bexp;

public class NotBExp extends Bexp{
	private Bexp bexp;
	
	public NotBExp(Bexp b) {
		this.bexp=b;
	}
	
	public Bexp getBexp() {
		return bexp;
	}
}
