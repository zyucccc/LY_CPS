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
		 return null;
	    }
	 
	    @Override
	    public QueryResultI visit(GQuery ast,ExecutionState data) throws Exception  {
		        ProcessingNodeI CurrentNode = data.getProcessingNode();
		        ArrayList<String> list_sensorid = (ArrayList<String>) ast.getGather().eval(this, data);
		        Cont cont = (Cont)ast.getCont().eval(this, data);
	            ArrayList<SensorDataI> list_result = new ArrayList<SensorDataI>();
	            for(String sensorid : list_sensorid) {
	            	list_result.add(CurrentNode.getSensorData(sensorid));
	            }
	           QueryResultI queryResult = new QueryResult(false,new ArrayList<String>(),true,list_result);
	           data.addToCurrentResult(queryResult);
	           return queryResult;
	    }
	    
		@Override
		public QueryResultI visit(BQuery ast, ExecutionState data) throws Exception {
			ProcessingNodeI CurrentNode = data.getProcessingNode();
			Cont cont = (Cont)ast.getCont().eval(this, data);
			Boolean bool_expr = (Boolean) ast.getBexp().eval(this, data);
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
		public Direction visit(Direction ast, ExecutionState data) throws Exception{
			return ast.getDirection();
		}

		@Override
		public Object visit(Dirs ast, ExecutionState data) throws Exception{
			return null;
		}
		
		@Override
		public Set<Direction> visit(FDirs ast, ExecutionState data) throws Exception {
			Set<Direction> directions = new HashSet<>();
			
			directions.add((Direction)ast.getDir().eval(this, data));
//			directions.add((Direction)visit(ast.getDir(),data));
			return directions;
		}

		@Override
		public Set<Direction> visit(RDirs ast, ExecutionState data) throws Exception {
			    Set<Direction> directions = new HashSet<>();
			    Direction dir = ast.getDir();
			    directions.add(dir); 
			    Set<Direction> dirsResult = (Set<Direction>)ast.getdirs().eval(this, data) ;
			    if(dirsResult != null) {
			    directions.addAll(dirsResult);
			    }
			    return directions; 
		}

//gather
		@Override
	    public Object visit(Gather ast, ExecutionState data) throws Exception {
			return null;
	    }

		@Override
		public ArrayList<String> visit(FGather ast, ExecutionState data) throws Exception {
			ArrayList<String> ids = new ArrayList<String>(); 
			ids.add(ast.getSensorID());
			return ids;
		}

		@Override
		public ArrayList<String> visit(RGather ast, ExecutionState data) throws Exception {
			ArrayList<String> ids = new ArrayList<String>();   		
	        Gather gathers=ast.getGather();
	        ArrayList<String> ids_suite =(ArrayList<String>) gathers.eval(this, data);
	        String id = ast.getSensorID();
	        ids.add(id);
	        ids.addAll(ids_suite);
	        return ids;
		}
		
//rand
	    @Override
	    public Object visit(Rand ast, ExecutionState data) throws Exception {
	    	return null;
	    }
	    
		@Override
		public Double visit(CRand ast, ExecutionState data) throws Exception {
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
		public PositionI visit(ABase ast, ExecutionState data) throws Exception {
			return ast.getPosition();
		}

		@Override
		public PositionI visit(RBase ast, ExecutionState data) throws Exception {
			ProcessingNodeI CurrentNode = data.getProcessingNode();
			return CurrentNode.getPosition();
		}

//Bexp
		@Override
		public Object visit(Bexp ast, ExecutionState data) throws Exception {
			return null;
		}
		
		@Override
		public Boolean visit(AndBExp ast, ExecutionState data) throws Exception {
			
			Boolean left = (Boolean) ast.getBexp1().eval(this, data);
		    Boolean right = (Boolean) ast.getBexp2().eval(this, data);
		   
		    return left&&right;	
		}

		 @Override
		  public Boolean visit(CExpBExp ast, ExecutionState data) throws Exception {
		    Boolean cexp = (Boolean) ast.getCexp().eval(this, data);
		    return cexp;
		  }

		@Override
		public Boolean visit(NotBExp ast, ExecutionState data) throws Exception {
			Boolean bo = (Boolean) ast.getBexp().eval(this, data);
			return bo;
		}

		@Override
		public Boolean visit(OrBExp ast, ExecutionState data) throws Exception {
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
			return null;
		}
		
		 @Override
		  public Boolean visit(EQCExp ast, ExecutionState data) throws Exception {
		   
		   double r1=(double) ast.getRand1().eval(this, data);
		   double r2=(double) ast.getRand2().eval(this, data);
		   return r1==r2;
		   
		  }

		  @Override
		  public Boolean visit(GCExp ast, ExecutionState data) throws Exception {
			  double r1=(double) ast.getRand1().eval(this, data);
			   double r2=(double) ast.getRand2().eval(this, data);

			   return r1>r2;
		  }

		  @Override
		  public Boolean visit(GEQCExp ast, ExecutionState data) throws Exception {
			  double r1=(double) ast.getRand1().eval(this, data);
			  double r2=(double) ast.getRand2().eval(this, data);

			   return r1>=r2;
		  }

		  @Override
		  public Boolean visit(LCExp ast, ExecutionState data) throws Exception {
			  double r1=(double) ast.getRand1().eval(this, data);
			  double r2=(double) ast.getRand2().eval(this, data);

			   return r1<r2;
		  }

		  @Override
		  public Boolean visit(LEQCExp ast, ExecutionState data) throws Exception {
			  double r1=(double) ast.getRand1().eval(this, data);
			  double r2=(double) ast.getRand2().eval(this, data);

			   return r1<=r2;
		  }
		  
		
//cont
		@Override
		public Object visit(Cont ast, ExecutionState data) throws Exception {
			return null;
		}

		@Override
		public Object visit(DCont ast, ExecutionState data) throws Exception {
			if(!(data.isContinuationSet())) {
				data.setIsContinuationSet();
				int nbSauts = ast.getNbSauts();
				Set<Direction> directions = (Set<Direction>) ast.getDirs().eval(this, data);
				data.setDirectional(directions,nbSauts);
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
	            double distanceMax = ast.getDistanceMax();					
				data.setFlooding(distanceMax,base);
			}
			return null;
		}


}
