package request.ast.interpreter;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI;
import request.ExecutionState;
import request.ast.Base;
import request.ast.Bexp;
import request.ast.Cexp;
import request.ast.Cont;
import request.ast.Direction;
import request.ast.Dirs;
import request.ast.Gather;
import request.ast.Query;
import request.ast.Rand;
import request.ast.astBase.ABase;
import request.ast.astBase.RBase;
import request.ast.astBexp.AndBExp;
import request.ast.astBexp.CExpBExp;
import request.ast.astBexp.NotBExp;
import request.ast.astBexp.OrBExp;
import request.ast.astBexp.SBExp;
import request.ast.astCexp.EQCExp;
import request.ast.astCexp.GCExp;
import request.ast.astCexp.GEQCExp;
import request.ast.astCexp.LCExp;
import request.ast.astCexp.LEQCExp;
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
import request.ast.interfaces.IASTvisitor;
import sensor_network.QueryResult;

public class Interpreter implements IASTvisitor<Object, ExecutionState, Exception> {

	
	 @Override
	    public Object visit(Query<?> ast,ExecutionState data) throws Exception  {
		 return null; 
	    }
	 
	    @Override
	    public Object visit(GQuery ast,ExecutionState data) throws Exception  {
		        ProcessingNodeI CurrentNode = data.getProcessingNode();
	            ArrayList<String> list_sensorid = (ArrayList<String>) visit(ast.getGather(),data);
	            Cont cont = (Cont)visit(ast.getCont(),data);
	            ArrayList<SensorDataI> list_result = new ArrayList<SensorDataI>();
	            for(String sensorid : list_sensorid) {
	            	list_result.add(CurrentNode.getSensorData(sensorid));
	            }
	           QueryResultI queryResult = new QueryResult(false,new ArrayList<String>(),true,list_result);
	           data.addToCurrentResult(queryResult);
	           return queryResult;
	    }
	    
		@Override
		public Object visit(BQuery ast, ExecutionState data) throws Exception {
			ProcessingNodeI CurrentNode = data.getProcessingNode();
			Cont cont = (Cont)visit(ast.getCont(),data);
			Boolean bool_expr = (Boolean) visit(ast.getBexp(),data);
			ArrayList<String> list_Nodeid = new ArrayList<String>();
			if (bool_expr) {
				list_Nodeid.add(CurrentNode.getNodeIdentifier());
		    }
			QueryResultI queryResult = new QueryResult(true,list_Nodeid,false,new ArrayList<SensorDataI>());
			data.addToCurrentResult(queryResult);
	           return queryResult;
		}
	 
//dir
	    @Override
		public Object visit(Direction ast, ExecutionState data) throws Exception{
			return ast.getDirection();
		}

		@Override
		public Object visit(Dirs ast, ExecutionState data) throws Exception{
			return null;
		}
		
		@Override
		public Object visit(FDirs ast, ExecutionState data) throws Exception {
//			ArrayList<Direction> directions = new ArrayList<Direction>();
			Set<Direction> directions = new HashSet<>();
			directions.add((Direction)visit(ast.getDir(),data));
			return directions;
		}

		@Override
		public Object visit(RDirs ast, ExecutionState data) throws Exception {
			    Set<Direction> directions = new HashSet<>();
//			    ArrayList<Direction> directions = new ArrayList<Direction>();		    
			    Direction dir = ast.getDir();
			    directions.add(dir); 
			    directions.addAll((Set<Direction>)visit(ast.getdirs(),data));
			    return directions; 
		}

//gather
		@Override
	    public Object visit(Gather ast, ExecutionState data) throws Exception {
	        if (ast instanceof FGather) {
	        	return visit((FGather)ast,data);
	        }else if(ast instanceof RGather) {
	            return visit((RGather)ast,data);
	        }
	        throw new Exception("Unknown gathers type");
	    }

		@Override
		public Object visit(FGather ast, ExecutionState data) throws Exception {
			return ast.getSensorID();
		}

		@Override
		public Object visit(RGather ast, ExecutionState data) throws Exception {
			ArrayList<Object> ids = new ArrayList<Object>();      
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
		
//rand
	    @Override
	    public Object visit(Rand ast, ExecutionState data) throws Exception {
	         if (ast instanceof SRand) {
	        	 return visit((SRand)ast,data);
	         }else if (ast instanceof CRand) {
	             return visit((CRand)ast,data);         		
	         }
	         throw new Exception("Unknown Rand type");
	    }
	    
		@Override
		public Object visit(CRand ast, ExecutionState data) throws Exception {
			return ((CRand) ast).getVal();
		}

		@Override
		public Object visit(SRand ast, ExecutionState data) throws Exception {
			ProcessingNodeI node=data.getProcessingNode();
	        String id= ast.getSensorId();
	        Object valeur=node.getSensorData(id);
	        return valeur;
		}
		
//base
		@Override
		public Object visit(Base ast, ExecutionState data) throws Exception {
			return null;
		}
		
		@Override
		public Object visit(ABase ast, ExecutionState data) throws Exception {
			return ast.getPosition();
		}

		@Override
		public Object visit(RBase ast, ExecutionState data) throws Exception {
			ProcessingNodeI CurrentNode = data.getProcessingNode();
			return CurrentNode.getPosition();
		}

//Bexp
		@Override
		public Object visit(Bexp ast, ExecutionState data) throws Exception {
			if (ast instanceof AndBExp) {
				return visit((AndBExp)ast,data);
	        }else if (ast instanceof CExpBExp) {
	        	return visit((CExpBExp)ast,data);	
	        }else if (ast instanceof NotBExp) {
	        	return visit((NotBExp)ast,data);
	        }else if (ast instanceof OrBExp) {
	        	return visit((OrBExp)ast,data);
	        }else if (ast instanceof SBExp) {
	        	return visit((SBExp)ast,data);
	        }
			throw new Exception("Unknown Bexp type");
		}
		
		@Override
		public Object visit(AndBExp ast, ExecutionState data) throws Exception {
			Object left = visit(ast.getBexp1(), data);
		    Object right = visit(ast.getBexp2(), data);
		   
		    return ((Boolean)left)&&((Boolean)right);	
		}

		@Override
		public Object visit(CExpBExp ast, ExecutionState data) throws Exception {
			 Object cexp = visit( ast.getCexp(), data);
			 Object bool=visit((Cexp)cexp,data);
			 
			 return ((Boolean)bool);
		}

		@Override
		public Object visit(NotBExp ast, ExecutionState data) throws Exception {
			Object bo = visit( ast.getBexp(), data);
			
			return ((Boolean)bo);
		}

		@Override
		public Object visit(OrBExp ast, ExecutionState data) throws Exception {
			 Object left = visit(ast.getBexp1(), data);
			 Object right = visit( ast.getBexp2(), data);
			 
			 return ((Boolean)left)||((Boolean)right); 
		}

		@Override
		public Object visit(SBExp ast, ExecutionState data) throws Exception {
			ProcessingNodeI node=data.getProcessingNode();
		     String sensorId = ast.getSensorID();
		     Object bo=node.getSensorData(sensorId);
	     
		     return (Boolean)bo;
		}

//cexp
		@Override
		public Object visit(Cexp ast, ExecutionState data) throws Exception {
			if (ast instanceof EQCExp) {
				return visit((EQCExp)ast,data);

	        }else if (ast instanceof GCExp) {
	        	return visit((GCExp)ast,data);	
	        }else if (ast instanceof GEQCExp) {
	        	return visit((GEQCExp)ast,data);
	        }else if (ast instanceof LCExp) {
	        	return visit((LCExp)ast,data);
	        }else if (ast instanceof LEQCExp) {
	        	return visit((LEQCExp)ast,data);
	        }
			throw new Exception("Unknown Cexp type");
		}
		
		@Override
		public Object visit(EQCExp ast, ExecutionState data) throws Exception {
			
			Object rand1=visit(ast.getRand1(),data);
			Object rand2=visit(ast.getRand2(),data);
			Object r1=visit((Rand)rand1,data);
			Object r2=visit((Rand)rand2,data);
			//=
			return ((double)r1)==((double)r2);
			
		}

		@Override
		public Object visit(GCExp ast, ExecutionState data) throws Exception {
			// TODO Auto-generated method stub》
			Object rand1=visit(ast.getRand1(),data);
			Object rand2=visit(ast.getRand2(),data);
			Object r1=visit((Rand)rand1,data);
			Object r2=visit((Rand)rand2,data);
			
			return ((double)r1)>=((double)r2);
		}

		@Override
		public Object visit(GEQCExp ast, ExecutionState data) throws Exception {
			Object rand1=visit(ast.getRand1(),data);
			Object rand2=visit(ast.getRand2(),data);
			Object r1=visit((Rand)rand1,data);
			Object r2=visit((Rand)rand2,data);
			
			return ((double)r1)>=((double)r2);
		}

		@Override
		public Object visit(LCExp ast, ExecutionState data) throws Exception {
			Object rand1=visit(ast.getRand1(),data);
			Object rand2=visit(ast.getRand2(),data);
			Object r1=visit((Rand)rand1,data);
			Object r2=visit((Rand)rand2,data);
			
			return ((double)r1)<((double)r2);
		}

		@Override
		public Object visit(LEQCExp ast, ExecutionState data) throws Exception {
			Object rand1=visit(ast.getRand1(),data);
			Object rand2=visit(ast.getRand2(),data);
			Object r1=visit((Rand)rand1,data);
			Object r2=visit((Rand)rand2,data);
			
			return ((double)r1)<=((double)r2);
		}
		
//cont
		@Override
		public Object visit(Cont ast, ExecutionState data) throws Exception {
			if (ast instanceof DCont) {
				return visit((DCont)ast,data);
	        }else if (ast instanceof ECont) {
	        	return visit((ECont)ast,data);	
	        }else if (ast instanceof FCont) {
	        	return visit((FCont)ast,data);
	        }
			throw new Exception("Unknown Cont type");
		}

		@Override
		public Object visit(DCont ast, ExecutionState data) throws Exception {
			if(!(data.isContinuationSet())) {
				data.setIsContinuationSet();
				int nbSauts = ast.getNbSauts();
				Set<Direction> directions = (Set<Direction>) visit(ast.getDirs(),data);			
				data.setDirectional(true, directions,nbSauts);
			}
			return null;
		}

		@Override
		public Object visit(ECont ast, ExecutionState data) throws Exception {
			if(!(data.isContinuationSet())) {
				data.setIsContinuationSet();
			}
			return null;
		}

		@Override
		public Object visit(FCont ast, ExecutionState data) throws Exception {
			if (data.isFlooding()) {
				PositionI root_position = (PositionI)visit(ast.getBase(),data);
				double maxDistance = ast.getDistanceMax();
				ProcessingNodeI CurrentNode = data.getProcessingNode();
				PositionI current_position = CurrentNode.getPosition();
				
			}
			return null;
		}


}
