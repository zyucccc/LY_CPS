package ast;

public abstract class Dirs {
	private Dir dir;
	private Dirs dirs;
	
	public Dirs(Dir dir,Dirs dirs) {
		this.dir=dir;
		this.dirs=dirs;
	}
	public Dirs(Dir dir) {
		this.dir=dir;
	}
	
}
