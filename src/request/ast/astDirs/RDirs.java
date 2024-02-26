package request.ast.astDirs;

import request.ast.Direction;
import request.ast.Dirs;
import request.ast.interfaces.IASTvisitor;

public class RDirs extends Dirs{

//	private Direction dir;
//	private Dirs dirs;
	public RDirs(Direction dir, Dirs dirs) {
		super(dir, dirs);
	}
	public Direction getDir() {
		return this.dir;
	}
	public Dirs getdirs() {
		return this.dirs;
	}
	
	 @Override
		public <Result, Data, Anomaly extends Throwable> 
	    Result eval(IASTvisitor<Result, Data, Anomaly> visitor, Data data)
	            throws Anomaly {
	        return visitor.visit(this, data);
	    }

}
