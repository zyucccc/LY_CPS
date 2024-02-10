package request.ast;

import request.ast.interfaces.IASTvisitable;
import request.ast.interfaces.IASTvisitor;

public abstract class Dirs implements IASTvisitable {
	private Direction dir;
	private Dirs dirs;
	
	public Dirs(Direction dir,Dirs dirs) {
		this.dir=dir;
		this.dirs=dirs;
	}
	public Dirs(Direction dir) {
		this.dir=dir;
	}
	
	 @Override
		public <Result, Data, Anomaly extends Throwable> 
	    Result eval(IASTvisitor<Result, Data, Anomaly> visitor, Data data)
	            throws Anomaly {
	        return visitor.visit(this, data);
	    }
	
}
