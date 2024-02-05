package ast.interpreter;


import java.util.List;
import java.util.Vector;

import ast.Base;
import ast.Bexp;
import ast.Cexp;
import ast.Cont;
import ast.Dir;
import ast.Dirs;
import ast.Gather;
import ast.Query;
import ast.Rand;
import ast.astGather.FGather;
import ast.astGather.RGather;
import ast.astQuery.BQuery;
import ast.astQuery.GQuery;
import ast.astRand.CRand;
import ast.astRand.SRand;
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

        if (ast instanceof FGather) {

            return ast.getSensorID();

        }else if(ast instanceof RGather) {

            List<Object> ids = new Vector<Object>();        

            Gather gathers=((RGather) ast).getGather();

            Object id = ast.getSensorID();

            ids.add(id);

            while(gathers instanceof RGather) {

                ids.add(gathers.getSensorID());

                gathers=((RGather) gathers).getGather();

            }

            if(gathers instanceof FGather) {

                ids.add(gathers.getSensorID());

            }

            return ids;

            

        }

        throw new Exception("Unknown Query type");

    }

    @Override

    public Object visit(Rand ast, Object data) throws Exception {

        // TODO Auto-generated method stub

         if (ast instanceof SRand) {

             return ((SRand) ast).getSensorId();

         }else if (ast instanceof CRand) {

             return ((CRand) ast).getVal();

         }

         throw new Exception("Unknown Query type");

    }

}
