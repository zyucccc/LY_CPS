package request.ast.astDirs;

import request.ast.Dir;
import request.ast.Dirs;

public class FDirs extends Dirs{
	private Dir dir;
	
	public FDirs(Dir dir) {
		super(dir);
		
	}
	public Dir getDir() {
		return dir;
	}
}
