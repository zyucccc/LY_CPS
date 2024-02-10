package request.ast.astCont;

import request.ast.Cont;
import request.ast.Dirs;

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
