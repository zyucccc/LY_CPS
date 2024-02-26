package request.ast.astDirs;

import request.ast.Direction;
import request.ast.Dirs;
import request.ast.interfaces.IASTvisitor;

public class FDirs extends Dirs{
//	private Direction dir;
	
	public FDirs(Direction dir) {
		super(dir);
		
	}
	public Direction getDir() {
		return this.dir;
	}
	
	 @Override
		public <Result, Data, Anomaly extends Throwable> 
	    Result eval(IASTvisitor<Result, Data, Anomaly> visitor, Data data)
	            throws Anomaly {
	        return visitor.visit(this, data);
	    }
}
