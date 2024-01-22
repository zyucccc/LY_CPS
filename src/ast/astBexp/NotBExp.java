package ast.astBexp;

import ast.Bexp;

public class NotBExp extends Bexp{
	private Bexp bexp;
	
	public NotBExp(Bexp b) {
		super(b);
	}
	
	public Bexp getBexp() {
		return bexp;
	}
}
