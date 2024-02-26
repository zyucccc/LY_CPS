package request.ast.astCont;

import request.ast.Cont;
import request.ast.Dirs;
import request.ast.interfaces.IASTvisitor;

public class DCont extends Cont{
	private Dirs dirs;
	private int nb;
	
	public DCont(Dirs dirs,int nb) {
		this.dirs=dirs;
		this.nb=nb;
	}
	public Dirs getDirs() {
		return dirs;
	}
	public int getNbSauts() {
		return nb;
	}
	
	 @Override
		public <Result, Data, Anomaly extends Throwable> 
	    Result eval(IASTvisitor<Result, Data, Anomaly> visitor, Data data)
	            throws Anomaly {
	        return visitor.visit(this, data);
	    }
}
