package ast.astCont;

import ast.Cont;
import ast.Dirs;

public class DCont extends Cont{
	private Dirs dirs;
	private int nb;
	
	public DCont(Dirs dirs,int nb) {
		this.dirs=dirs;
		this.nb=nb;
	}
	public Dirs getDirs() {
		return dirs;
	}
	public int getNB() {
		return nb;
	}
}
