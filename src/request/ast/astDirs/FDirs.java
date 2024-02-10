package request.ast.astDirs;

import request.ast.Direction;
import request.ast.Dirs;

public class FDirs extends Dirs{
	private Direction dir;
	
	public FDirs(Direction dir) {
		super(dir);
		
	}
	public Direction getDir() {
		return dir;
	}
}
