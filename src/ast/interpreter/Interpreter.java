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
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;

public class Interpreter implements IASTvisitor<Object, ExecutionStateI, Exception> {

	 @Override
	    public Object visit(Query<?> ast,ExecutionStateI data) throws Exception  {
	        // 根据Query类型进行处理
	        if (ast instanceof GQuery) {
	            
	        } else if (ast instanceof BQuery) {
	            
	        }
	        throw new Exception("Unknown Query type");
	    }

	@Override
	public Object visit(Base ast, ExecutionStateI data) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Bexp ast, ExecutionStateI data) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Cexp ast,ExecutionStateI data) throws Exception{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Cont ast, ExecutionStateI data) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Dir ast, ExecutionStateI data) throws Exception{
		
		return null;
	}

	@Override
	public Object visit(Dirs ast, ExecutionStateI data) throws Exception{
		// TODO Auto-generated method stub
		return null;
	}

	@Override

    public Object visit(Gather ast, ExecutionStateI data) throws Exception {

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

    public Object visit(Rand ast, ExecutionStateI data) throws Exception {

        // TODO Auto-generated method stub

         if (ast instanceof SRand) {

             return ((SRand) ast).getSensorId();

         }else if (ast instanceof CRand) {

             return ((CRand) ast).getVal();

         }

         throw new Exception("Unknown Query type");

    }

}
