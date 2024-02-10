package request.ast.astDirs;

import request.ast.Direction;
import request.ast.Dirs;

public class RDirs extends Dirs{

	private Direction dir;
	private Dirs dirs;
	public RDirs(Direction dir, Dirs dirs) {
		super(dir, dirs);
	}
	public Direction getDir() {
		return dir;
	}
	public Dirs getdirs() {
		return dirs;
	}

}
