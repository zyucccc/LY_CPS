package request;

import java.util.ArrayList;
import java.util.Set;

import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import nodes.sensor.Sensor;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import request.ast.Direction;
import request.ast.Query;
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
    @SuppressWarnings("unchecked")
	public static void main(String[] args) {
    	ExecutionState data = new ExecutionState();  
        Interpreter interpreter = new Interpreter();

        try {
            
        	//Test Cexp
        	//test CExpBExp
            CExpBExp test_CExpBExp =new CExpBExp(
    				new GEQCExp(
    						new CRand(15.0), new CRand(50.0)
    						)
    				);
        	Boolean result_test_CExpBExp = (Boolean)test_CExpBExp.eval(interpreter, data);
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
            Boolean result_test_SBExp = (Boolean)test_SBExp.eval(interpreter, executionState_bexp);
//            Boolean result_test_SBExp = (Boolean)interpreter.visit(test_SBExp, executionState_bexp);
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
            Boolean result_test_AndBExp = (Boolean)test_AndBExp.eval(interpreter, data);
            System.out.println("Test AndBExp Result: " + result_test_AndBExp);
            
            //Test Rand
            //test CRand
            CRand test_CRand = new CRand(10);
            double result_test_CRand = (double)test_CRand.eval(interpreter, data);
            System.out.println("Test Crand Result: " + result_test_CRand);
            //test SRand
            PositionI position = new Position(0.0, 0.0);
            ProcessingNode processingNode = new ProcessingNode("Node1",position);
            Sensor SensorData = new Sensor("Node1","temperature",double.class,35.0);
            processingNode.addSensorData("temperature", SensorData);
            ExecutionState executionState_test = new ExecutionState(); 
            executionState_test.updateProcessingNode(processingNode);
            SRand test_SRand = new SRand("temperature");
            double result_test_SRand = (double)test_SRand.eval(interpreter, executionState_test);
//            double result_test_SRand = (double)interpreter.visit(test_SRand,executionState_test);
            System.out.println("Test Srand Result: " + result_test_SRand);
            
            
            //Test Direction
            //test Dirs
            Direction test_direction = Direction.NE;
            Direction result_test_Direction = (Direction)test_direction.eval(interpreter, data);
            System.out.println("Test Direction Result: " + result_test_Direction);
            //test RDirs
            RDirs test_RDirs = new RDirs(Direction.NW, new FDirs(Direction.NE));
            Set<Direction> result_test_RDirs = (Set<Direction>)test_RDirs.eval(interpreter, data);
            System.out.println("Test RDirs Result: " + result_test_RDirs);
            
            //Test Gather
            //test FGather
            FGather test_FGather = new FGather("fumée");
            ArrayList<String> result_test_FGather = (ArrayList<String>)test_FGather.eval(interpreter, data);
            System.out.println("Test FGather Result: " + result_test_FGather);
            //test RGather
            RGather test_RGather = new RGather("température", new FGather("fumée"));
            ArrayList<String> result_test_RGather = (ArrayList<String>)test_RGather.eval(interpreter, data);
            System.out.println("Test RGather Result: " + result_test_RGather);
            
            //Test Base
            //test ABase
            PositionI position1 = new Position(15.0, 6.0);
            ABase test_ABase = new ABase(position1);
            Position result_test_ABase = (Position)test_ABase.eval(interpreter, data);
            System.out.println("Test ABase Result: " + result_test_ABase);
            //test RBase
            RBase test_RBase = new RBase();
            Position result_test_RBase = (Position)test_RBase.eval(interpreter, executionState_test);
//            Position result_test_RBase = (Position)interpreter.visit(test_RBase,executionState_test);
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

        	QueryResult result_test_BQuery =   (QueryResult)test_BQuery.eval(interpreter, executionState_query);
            System.out.println("Test BQuery Result: \n" + result_test_BQuery);
            //test GQuery
//          FGather test_FGather = new FGather("fumée");
            GQuery test_GQuery=new GQuery(test_FGather, new DCont(new FDirs(Direction.NE), 2));
            QueryResult result_test_GQuery =   (QueryResult)test_GQuery.eval(interpreter, executionState_query);
            System.out.println("Test GQuery Result: \n" + result_test_GQuery);
            //test query
            Query<?> test_query = test_GQuery;
            QueryResult result_test_Query =   (QueryResult)test_query.eval(interpreter, executionState_query);
            System.out.println("Test Query Result: \n" + result_test_Query);
            
            //Test Cont
            //test ECont
            ExecutionState data1 = new ExecutionState();
            ECont test_ECont = new ECont();
            System.out.println("Test ECont:  ");
            System.out.println("Continualtion avant eval \n" + data1.toStringContinuation());
            test_ECont.eval(interpreter, data1);
            System.out.println("Continualtion after eval \n" + data1.toStringContinuation());
            //test DCont
            ExecutionState data2 = new ExecutionState();
            DCont test_DCont = new DCont(new RDirs(Direction.NW, new FDirs(Direction.NE)),5);
            System.out.println("Test DCont:  ");
            System.out.println("Continualtion avant eval : " + data2.toStringContinuation());
            test_DCont.eval(interpreter, data2);
            System.out.println("Continualtion after eval : " + data2.toStringContinuation());
            //test FCont
            ExecutionState data3 = new ExecutionState();
            FCont test_FCont = new FCont(new ABase(new Position(15.0, 6.0)),20.0);
            System.out.println("Test FCont:  ");
            System.out.println("Continualtion avant eval : " + data3.toStringContinuation());
            test_FCont.eval(interpreter, data3);
            System.out.println("Continualtion after eval : " + data3.toStringContinuation());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
       
        
    }
}