package request.ast;

import request.ast.interfaces.IASTvisitable;
import request.ast.interfaces.IASTvisitor;

import java.io.Serializable;

public abstract class Dirs implements IASTvisitable, Serializable {
	private static final long serialVersionUID = 1L;
	protected Direction dir;
	protected Dirs dirs;
	
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
