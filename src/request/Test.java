package request;

import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;

import request.ast.Direction;
import request.ast.astBase.ABase;
import request.ast.astBexp.AndBExp;
import request.ast.astBexp.CExpBExp;
import request.ast.astCexp.GEQCExp;
import request.ast.astCont.DCont;
import request.ast.astCont.ECont;
import request.ast.astCont.FCont;
import request.ast.astDirs.FDirs;
import request.ast.astDirs.RDirs;
import request.ast.astGather.FGather;
import request.ast.astGather.RGather;
import request.ast.astQuery.BQuery;
import request.ast.astQuery.GQuery;
import request.ast.astRand.CRand;
import request.ast.astRand.SRand;
import request.ast.interpreter.Interpreter;

public class Test {
    public static void main(String[] args) {
    	 ExecutionState data = new ExecutionState(); 

         
         Interpreter interpreter = new Interpreter();
       
        BQuery q1=new BQuery(new AndBExp(
        								new CExpBExp(
        											new GEQCExp(
        													new SRand("température"), new CRand(50.0)
        													)
        											)
        								, new CExpBExp(
        											new GEQCExp(
        													new SRand("fumée"), new CRand(3.0))))
        		,  new DCont(new FDirs(Direction.NE), 2));

       
        
        CRand q=new CRand(3.0);
        GQuery q3=new GQuery(new RGather("température", new FGather("fumée")), new FCont(new ABase(null),3.0));
        GQuery q4=new GQuery(new RGather("température", new FGather("fumée")), new DCont(new RDirs(Direction.NW, new FDirs(Direction.NE)), 2));
        GQuery q2=new GQuery(new RGather("température", new FGather("fumée")), new ECont());
        CExpBExp t1=new CExpBExp(
				new GEQCExp(
						new CRand(15.0), new CRand(50.0)
						)
				);
        AndBExp t2= new AndBExp(
				new CExpBExp(
							new GEQCExp(
									new CRand(55.0), new CRand(50.0)
									)
							)
				, new CExpBExp(
							new GEQCExp(
									new CRand(10), new CRand(3.0))));
       

        try {
        	Object result =   interpreter.visit(q1, data);
        	//QueryResult result =  (QueryResult) interpreter.visit(q, data);
           
            System.out.println("Test Query Result: " + result);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
       
        
    }
}