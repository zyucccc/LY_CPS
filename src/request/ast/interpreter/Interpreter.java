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
import request.ProcessingNode;
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
//		 if (ast instanceof GQuery) {
//	        	return visit((GQuery)ast,data);
//	        }else if(ast instanceof BQuery) {
//	            return visit((BQuery)ast,data);
//	        }
//	        throw new Exception("Unknown query type");
		 return null;
	    }
	 
	    @Override
	    public Object visit(GQuery ast,ExecutionState data) throws Exception  {
		        ProcessingNodeI CurrentNode = data.getProcessingNode();
		        ArrayList<String> list_sensorid = (ArrayList<String>) ast.getGather().eval(this, data);
//	            ArrayList<String> list_sensorid = (ArrayList<String>) visit(ast.getGather(),data);
		        Cont cont = (Cont)ast.getCont().eval(this, data);
//		        Cont cont = (Cont)visit(ast.getCont(),data);
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
//			Cont cont = (Cont)visit(ast.getCont(),data);
			Cont cont = (Cont)ast.getCont().eval(this, data);
			Boolean bool_expr = (Boolean) ast.getBexp().eval(this, data);
//			Boolean bool_expr = (Boolean) visit(ast.getBexp(),data);
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
//			 if (ast instanceof FDirs) {
//		        	return visit((FDirs)ast,data);
//		        }else if(ast instanceof RDirs) {
//		            return visit((RDirs)ast,data);
//		        }
//			 throw new Exception("Unknown gathers type");
			return null;
		}
		
		@Override
		public Object visit(FDirs ast, ExecutionState data) throws Exception {
			Set<Direction> directions = new HashSet<>();
			
			directions.add((Direction)ast.getDir().eval(this, data));
//			directions.add((Direction)visit(ast.getDir(),data));
			return directions;
		}

		@Override
		public Object visit(RDirs ast, ExecutionState data) throws Exception {
			    Set<Direction> directions = new HashSet<>();
//			    ArrayList<Direction> directions = new ArrayList<Direction>();		    
			    Direction dir = ast.getDir();
			    directions.add(dir); 
			    Set<Direction> dirsResult = (Set<Direction>)ast.getdirs().eval(this, data) ;
//			    Set<Direction> dirsResult = (Set<Direction>) visit(ast.getdirs(), data);
			    if(dirsResult != null) {
			    directions.addAll(dirsResult);
			    }
			    return directions; 
		}

//gather
		@Override
	    public Object visit(Gather ast, ExecutionState data) throws Exception {
//	        if (ast instanceof FGather) {
//	        	return visit((FGather)ast,data);
//	        }else if(ast instanceof RGather) {
//	            return visit((RGather)ast,data);
//	        }
//	        throw new Exception("Unknown gathers type");
			return null;
	    }

		@Override
		public Object visit(FGather ast, ExecutionState data) throws Exception {
			ArrayList<String> ids = new ArrayList<String>(); 
			ids.add(ast.getSensorID());
			return ids;
		}

		@Override
		public Object visit(RGather ast, ExecutionState data) throws Exception {
			ArrayList<String> ids = new ArrayList<String>();   		
	        Gather gathers=ast.getGather();
	        ArrayList<String> ids_suite =(ArrayList<String>) gathers.eval(this, data);
//	        ArrayList<String> ids_suite =(ArrayList<String>) visit(gathers,data);	        
	        String id = ast.getSensorID();
	        ids.add(id);
	        ids.addAll(ids_suite);
	        return ids;
		}
		
//rand
	    @Override
	    public Object visit(Rand ast, ExecutionState data) throws Exception {
//	         if (ast instanceof SRand) {
//	        	 return visit((SRand)ast,data);
//	         }else if (ast instanceof CRand) {
//	             return visit((CRand)ast,data);         		
//	         }
//	         throw new Exception("Unknown Rand type");
	    	return null;
	    }
	    
		@Override
		public Object visit(CRand ast, ExecutionState data) throws Exception {
			return  ast.getVal();
		}

		@Override
		public Object visit(SRand ast, ExecutionState data) throws Exception {
			ProcessingNode node=(ProcessingNode)data.getProcessingNode();
	        String id= ast.getSensorId();
	        SensorDataI sensorData = node.getSensorData(id);
	        return sensorData.getValue();
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
//			if (ast instanceof AndBExp) {
//				return visit((AndBExp)ast,data);
//	        }else if (ast instanceof CExpBExp) {
//	        	return visit((CExpBExp)ast,data);	
//	        }else if (ast instanceof NotBExp) {
//	        	return visit((NotBExp)ast,data);
//	        }else if (ast instanceof OrBExp) {
//	        	return visit((OrBExp)ast,data);
//	        }else if (ast instanceof SBExp) {
//	        	return visit((SBExp)ast,data);
//	        }
//			throw new Exception("Unknown Bexp type");
			return null;
		}
		
		@Override
		public Object visit(AndBExp ast, ExecutionState data) throws Exception {
			
			Boolean left = (Boolean) ast.getBexp1().eval(this, data);
		    Boolean right = (Boolean) ast.getBexp2().eval(this, data);
		   
		    return left&&right;	
		}

		 @Override
		  public Object visit(CExpBExp ast, ExecutionState data) throws Exception {
		     Boolean cexp = (Boolean) ast.getCexp().eval(this, data);
//			 Object cexp = visit( ast.getCexp(), data);
		    
		    return cexp;
		  }

		@Override
		public Object visit(NotBExp ast, ExecutionState data) throws Exception {
			Boolean bo = (Boolean) ast.getBexp().eval(this, data);
			
			return bo;
		}

		@Override
		public Object visit(OrBExp ast, ExecutionState data) throws Exception {
			Boolean left = (Boolean) ast.getBexp1().eval(this, data);
		    Boolean right = (Boolean) ast.getBexp2().eval(this, data);
		   
		    return left||right;	
		}

		@Override
		public Object visit(SBExp ast, ExecutionState data) throws Exception {
			ProcessingNodeI node=data.getProcessingNode();
		     String sensorId = ast.getSensorID();
		     SensorDataI sensorData = node.getSensorData(sensorId);
		     return sensorData.getValue();
		}

//cexp
		@Override
		public Object visit(Cexp ast, ExecutionState data) throws Exception {
//			if (ast instanceof EQCExp) {
//				return visit((EQCExp)ast,data);
//
//	        }else if (ast instanceof GCExp) {
//	        	return visit((GCExp)ast,data);	
//	        }else if (ast instanceof GEQCExp) {
//	        	return visit((GEQCExp)ast,data);
//	        }else if (ast instanceof LCExp) {
//	        	return visit((LCExp)ast,data);
//	        }else if (ast instanceof LEQCExp) {
//	        	return visit((LEQCExp)ast,data);
//	        }
//			throw new Exception("Unknown Cexp type");
			return null;
		}
		
		 @Override
		  public Object visit(EQCExp ast, ExecutionState data) throws Exception {
		   
		   double r1=(double) ast.getRand1().eval(this, data);
		   double r2=(double) ast.getRand2().eval(this, data);
		   
		   //=
		   return r1==r2;
		   
		  }

		  @Override
		  public Object visit(GCExp ast, ExecutionState data) throws Exception {
			  double r1=(double) ast.getRand1().eval(this, data);
			   double r2=(double) ast.getRand2().eval(this, data);
			   
			   //=
			   return r1>r2;
		  }

		  @Override
		  public Object visit(GEQCExp ast, ExecutionState data) throws Exception {
			  double r1=(double) ast.getRand1().eval(this, data);
			   double r2=(double) ast.getRand2().eval(this, data);
			   
			   //=
			   return r1>=r2;
		  }

		  @Override
		  public Object visit(LCExp ast, ExecutionState data) throws Exception {
			  double r1=(double) ast.getRand1().eval(this, data);
			   double r2=(double) ast.getRand2().eval(this, data);
			   
			   //=
			   return r1<r2;
		  }

		  @Override
		  public Object visit(LEQCExp ast, ExecutionState data) throws Exception {
			  double r1=(double) ast.getRand1().eval(this, data);
			   double r2=(double) ast.getRand2().eval(this, data);
			   
			   
			   return r1<=r2;
		  }
		  
		
//cont
		@Override
		public Object visit(Cont ast, ExecutionState data) throws Exception {
//			if (ast instanceof DCont) {
//				return visit((DCont)ast,data);
//	        }else if (ast instanceof ECont) {
//	        	return visit((ECont)ast,data);	
//	        }else if (ast instanceof FCont) {
//	        	return visit((FCont)ast,data);
//	        }
//			throw new Exception("Unknown Cont type");
			return null;
		}

		@Override
		public Object visit(DCont ast, ExecutionState data) throws Exception {
			if(!(data.isContinuationSet())) {
				data.setIsContinuationSet();
				int nbSauts = ast.getNbSauts();
				Set<Direction> directions = (Set<Direction>) ast.getDirs().eval(this, data);
//				Set<Direction> directions = (Set<Direction>) visit(ast.getDirs(),data);			
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
			if(!(data.isContinuationSet())) {
				data.setIsContinuationSet();
				PositionI base = (PositionI) ast.getBase().eval(this, data);
//				PositionI base = (PositionI) visit(ast.getBase(),data);
	            double distanceMax = ast.getDistanceMax();					
				data.setFlooding(true, distanceMax,base);
			}
			return null;
		}


}
