package ast.astDirs;

import ast.Dir;
import ast.Dirs;

public class RDirs extends Dirs{

	private Dir dir;
	private Dirs dirs;
	public RDirs(Dir dir, Dirs dirs) {
		super(dir, dirs);
	}
	public Dir getDir() {
		return dir;
	}
	public Dirs getdirs() {
		return dirs;
	}

}
