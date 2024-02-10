package request.ast.astBexp;

import request.ast.Bexp;
import request.ast.Cexp;

public class CExpBExp extends Bexp{
	private Cexp cexp;
	
	public CExpBExp(Cexp cexp) {
		this.cexp = cexp;
	}
	
	public Cexp getCexp() {
		return this.cexp;
	}

}
