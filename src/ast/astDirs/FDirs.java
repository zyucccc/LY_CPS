package ast.astDirs;

import ast.Dir;
import ast.Dirs;

public class FDirs extends Dirs{
	private Dir dir;
	
	public FDirs(Dir dir) {
		super(dir);
		
	}
	public Dir getDir() {
		return dir;
	}
}
