package request.ast.astCont;

import request.ast.Base;
import request.ast.Cont;

public class FCont extends Cont{
	private Base base;
	private double distaneMax;
	
	public FCont(Base base,double dM) {
		this.base=base;
		this.distaneMax=dM;
	}
	public Base  getBase() {
		return base;
	}
	public double getDistanceMax() {
		return distaneMax;
	}
}
