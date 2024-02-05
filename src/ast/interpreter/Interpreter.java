package ast.interpreter;


import ast.Base;
import ast.Bexp;
import ast.Cexp;
import ast.Cont;
import ast.Dir;
import ast.Dirs;
import ast.Gather;
import ast.Query;
import ast.Rand;
import ast.astQuery.BQuery;
import ast.astQuery.GQuery;
import ast.interfaces.IASTvisitor;

public class Interpreter implements IASTvisitor<Object, Object, Exception> {

	 @Override
	    public Object visit(Query<?> ast, Object data) throws Exception  {
	        // 根据Query类型进行处理
	        if (ast instanceof GQuery) {
	            
	        } else if (ast instanceof BQuery) {
	            
	        }
	        throw new Exception("Unknown Query type");
	    }

	@Override
	public Object visit(Base ast, Object data) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Bexp ast, Object data) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Cexp ast, Object data) throws Exception{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Cont ast, Object data) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Dir ast, Object data) throws Exception{
		
		return null;
	}

	@Override
	public Object visit(Dirs ast, Object data) throws Exception{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Gather ast, Object data) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Rand ast, Object data) throws Exception{
		// TODO Auto-generated method stub
		return null;
	}

}
