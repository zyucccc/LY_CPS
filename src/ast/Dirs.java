package ast;

import ast.interfaces.IASTvisitable;
import ast.interfaces.IASTvisitor;

public abstract class Dirs implements IASTvisitable {
	private Dir dir;
	private Dirs dirs;
	
	public Dirs(Dir dir,Dirs dirs) {
		this.dir=dir;
		this.dirs=dirs;
	}
	public Dirs(Dir dir) {
		this.dir=dir;
	}
	
	 @Override
		public <Result, Data, Anomaly extends Throwable> 
	    Result eval(IASTvisitor<Result, Data, Anomaly> visitor, Data data)
	            throws Anomaly {
	        return visitor.visit(this, data);
	    }
	
}
