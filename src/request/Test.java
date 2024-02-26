package request;

import java.util.ArrayList;
import java.util.Set;

import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import nodes.sensor.Sensor;
import request.ast.Direction;
import request.ast.astBase.ABase;
import request.ast.astBase.RBase;
import request.ast.astBexp.AndBExp;
import request.ast.astBexp.CExpBExp;
import request.ast.astBexp.SBExp;
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
import sensor_network.Position;
import sensor_network.QueryResult;

public class Test {
    public static void main(String[] args) {
    	ExecutionState data = new ExecutionState();  
        Interpreter interpreter = new Interpreter();


       
        
        CRand q=new CRand(3.0);
        GQuery q3=new GQuery(new RGather("température", new FGather("fumée")), new FCont(new ABase(null),3.0));
        GQuery q4=new GQuery(new RGather("température", new FGather("fumée")), new DCont(new RDirs(Direction.NW, new FDirs(Direction.NE)), 2));
        GQuery q2=new GQuery(new RGather("température", new FGather("fumée")), new ECont());
               

        try {
            
        	//Test Cexp
        	//test CExpBExp
            CExpBExp test_CExpBExp =new CExpBExp(
    				new GEQCExp(
    						new CRand(15.0), new CRand(50.0)
    						)
    				);
        	Boolean result_test_CExpBExp = (Boolean)interpreter.visit(test_CExpBExp, data);
            System.out.println("Test CExpBExp Result: " + result_test_CExpBExp);
            
            //Test Bexp
            //test SBExp
          PositionI position_bexp = new Position(0.0, 0.0);
          ProcessingNode processingNode_bexp = new ProcessingNode("Node_query",position_bexp);
          Sensor SensorData_bexp_Bool = new Sensor("Node_query","fumée",Boolean.class,false);
          processingNode_bexp.addSensorData("fumée", SensorData_bexp_Bool);
          //init executionState
          ExecutionState executionState_bexp = new ExecutionState(); 
          //mise a jour processing node dans executionData
          executionState_bexp.updateProcessingNode(processingNode_bexp);
            SBExp test_SBExp = new SBExp("fumée");
            Boolean result_test_SBExp = (Boolean)interpreter.visit(test_SBExp, executionState_bexp);
            System.out.println("Test SBExp Result: " + result_test_SBExp);
            
            
            //test AndBExp
            AndBExp test_AndBExp = new AndBExp(
    				new CExpBExp(
    							new GEQCExp(
    									new CRand(55.0), new CRand(50.0)
    									)
    							)
    				, new CExpBExp(
    							new GEQCExp(
    									new CRand(10), new CRand(3.0))));
            Boolean result_test_AndBExp = (Boolean)interpreter.visit(test_AndBExp, data);
            System.out.println("Test AndBExp Result: " + result_test_AndBExp);
            
            //Test Rand
            //test CRand
            CRand test_CRand = new CRand(10);
            double result_test_CRand = (double)interpreter.visit(test_CRand, data);
            System.out.println("Test Crand Result: " + result_test_CRand);
            //test SRand
            PositionI position = new Position(0.0, 0.0);
            ProcessingNode processingNode = new ProcessingNode("Node1",position);
            Sensor SensorData = new Sensor("Node1","temperature",double.class,35.0);
            processingNode.addSensorData("temperature", SensorData);
            ExecutionState executionState_test = new ExecutionState(); 
            executionState_test.updateProcessingNode(processingNode);
            SRand test_SRand = new SRand("temperature");
            double result_test_SRand = (double)interpreter.visit(test_SRand,executionState_test);
            System.out.println("Test Srand Result: " + result_test_SRand);
            
            
            //Test Direction
            //test Dirs
            Direction test_direction = Direction.NE;
            Direction result_test_Direction = (Direction)interpreter.visit(test_direction, data);
            System.out.println("Test Direction Result: " + result_test_Direction);
            //test RDirs
            RDirs test_RDirs = new RDirs(Direction.NW, new FDirs(Direction.NE));
            Set<Direction> result_test_RDirs = (Set<Direction>)interpreter.visit(test_RDirs, data);
            System.out.println("Test RDirs Result: " + result_test_RDirs);
            
            //Test Gather
            //test FGather
            FGather test_FGather = new FGather("fumée");
            ArrayList<String> result_test_FGather = (ArrayList<String>)interpreter.visit(test_FGather, data);
            System.out.println("Test FGather Result: " + result_test_FGather);
            //test RGather
            RGather test_RGather = new RGather("température", new FGather("fumée"));
            ArrayList<String> result_test_RGather = (ArrayList<String>)interpreter.visit(test_RGather, data);
            System.out.println("Test RGather Result: " + result_test_RGather);
            
            //Test Base
            //test ABase
            PositionI position1 = new Position(15.0, 6.0);
            ABase test_ABase = new ABase(position1);
            Position result_test_ABase = (Position)interpreter.visit(test_ABase, data);
            System.out.println("Test ABase Result: " + result_test_ABase);
            //test RBase
            RBase test_RBase = new RBase();
            Position result_test_RBase = (Position)interpreter.visit(test_RBase,executionState_test);
            System.out.println("Test RBase Result: " + result_test_RBase);
            
            //Test Query
            //test BQuery
            PositionI position_query = new Position(0.0, 0.0);
            ProcessingNode processingNode_query = new ProcessingNode("Node_query",position_query);
            Sensor SensorData_query_Double = new Sensor("Node_query","temperature",double.class,35.0);
            processingNode.addSensorData("temperature", SensorData_query_Double);
            Sensor SensorData_query_Bool = new Sensor("Node_query","fumée",Boolean.class,true);
            processingNode_query.addSensorData("fumée", SensorData_query_Bool);
            //init executionState
            ExecutionState executionState_query = new ExecutionState(); 
            //mise a jour processing node dans executionData
            executionState_query.updateProcessingNode(processingNode_query);
            
//          SBExp test_SBExp = new SBExp("fumée");
            BQuery test_BQuery=new BQuery(test_SBExp, new DCont(new FDirs(Direction.NE), 2));

        	QueryResult result_test_BQuery =   (QueryResult)interpreter.visit(test_BQuery, executionState_query);
            System.out.println("Test BQuery Result: \n" + result_test_BQuery);
            //test GQuery
//          FGather test_FGather = new FGather("fumée");
            GQuery test_GQuery=new GQuery(test_FGather, new DCont(new FDirs(Direction.NE), 2));
            QueryResult result_test_GQuery =   (QueryResult)interpreter.visit(test_GQuery, executionState_query);
            System.out.println("Test GQuery Result: \n" + result_test_GQuery);
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
       
        
    }
}