package request;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
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
import request.ExecutionState;
import sensor_network.Position;
import sensor_network.QueryResult;
import nodes.sensor.Sensor;

/**
 * The class <code>JUnitTest</code> implements the JUnit test for the langage request.
 * il y a 17 tests pour tester l'interprete de manière complète.
 */
@SuppressWarnings("unused")
class JUnitTest {

    private Interpreter interpreter;
    private ExecutionState executionState;
    private ProcessingNode processingNode;

    @BeforeEach
    void setUp() {
        interpreter = new Interpreter();
        executionState = new ExecutionState();
        
        //init
        PositionI position = new Position(0.0, 0.0);
        processingNode = new ProcessingNode("Node1", position);
        Sensor SensorData = new Sensor("Node1", "temperature", Double.class, 35.0);
        Sensor sensorData = new Sensor("Node1", "fumée", Boolean.class, false);
        processingNode.addSensorData("temperature", SensorData);
        processingNode.addSensorData("fumée", sensorData);
        executionState.updateProcessingNode(processingNode);
    }
    
    @Test
    void testCExpBExp() throws Exception {
        CExpBExp cExpBExp = new CExpBExp(new GEQCExp(new CRand(15.0), new CRand(50.0)));
        Boolean result = (Boolean) cExpBExp.eval(interpreter, executionState);
        assertFalse(result);
    }
    
    @Test
    void testSBExp() throws Exception {
        SBExp sbExp = new SBExp("fumée");
        Boolean result = (Boolean) sbExp.eval(interpreter, executionState);
        assertFalse(result);
    }
    
    @Test
    void testAndBExp() throws Exception {
        AndBExp andBExp = new AndBExp(
                new CExpBExp(new GEQCExp(new CRand(55.0), new CRand(50.0))),
                new CExpBExp(new GEQCExp(new CRand(10), new CRand(3.0)))
        );
        Boolean result = (Boolean) andBExp.eval(interpreter, executionState);
        assertTrue(result);
    }
    
    @Test
    void testCRand() throws Exception {
        CRand cRand = new CRand(10);
        Object result = cRand.eval(interpreter, executionState);
        assertEquals(10.0, result);
    }

    @Test
    void testSRand() throws Exception {
        Sensor sensorData = new Sensor("Node1", "temperature", Double.class, 35.0);
        processingNode.addSensorData("temperature", sensorData);
        SRand sRand = new SRand("temperature");
        Object result = sRand.eval(interpreter, executionState);
        assertEquals(35.0, result);
    }

    @Test
    void testDirection() throws Exception {
        Direction testDirection = Direction.NE;
        Direction result = (Direction) testDirection.eval(interpreter, executionState);
        assertEquals(Direction.NE, result);
    }

    @Test
    void testRDirs() throws Exception {
        RDirs rDirs = new RDirs(Direction.NW, new FDirs(Direction.NE));
        @SuppressWarnings("unchecked")
		Set<Direction> result = (Set<Direction>) rDirs.eval(interpreter, executionState);
        assertTrue(result.contains(Direction.NW) && result.contains(Direction.NE));
    }

    @Test
    void testFGather() throws Exception {
        FGather fGather = new FGather("fumée");
        @SuppressWarnings("unchecked")
		ArrayList<String> result = (ArrayList<String>) fGather.eval(interpreter, executionState);
        assertTrue(result.contains("fumée"));
    }

    @Test
    void testRGather() throws Exception {
        RGather rGather = new RGather("temperature", new FGather("fumée"));
        @SuppressWarnings("unchecked")
		ArrayList<String> result = (ArrayList<String>) rGather.eval(interpreter, executionState);
        assertTrue(result.contains("temperature") && result.contains("fumée"));
    }

    @Test
    void testABase() throws Exception {
        PositionI position1 = new Position(15.0, 6.0);
        ABase aBase = new ABase(position1);
        Position result = (Position) aBase.eval(interpreter, executionState);
        assertEquals(15.0, result.getX());
        assertEquals(6.0, result.getY());
    }

    @Test
    void testRBase() throws Exception {
        RBase rBase = new RBase();
        Position result = (Position) rBase.eval(interpreter, executionState);
        assertEquals(0.0, result.getX());
        assertEquals(0.0, result.getY());
    }

    @Test
    void testBQuery() throws Exception {
        // new environment
        PositionI position_query = new Position(0.0, 0.0);
        ProcessingNode processingNode_query = new ProcessingNode("Node_query", position_query);
        Sensor sensorData_double = new Sensor("Node_query", "temperature", Double.class, 35.0);
        Sensor sensorData_bool = new Sensor("Node_query", "fumée", Boolean.class, true);
        processingNode_query.addSensorData("temperature", sensorData_double);
        processingNode_query.addSensorData("fumée", sensorData_bool);
        executionState.updateProcessingNode(processingNode_query);

        SBExp sbExp = new SBExp("fumée");
        BQuery bQuery = new BQuery(sbExp, new DCont(new FDirs(Direction.NE), 2));

        QueryResult result = (QueryResult) bQuery.eval(interpreter, executionState);

        assertNotNull(result, "res not null");
        assertTrue(result.isBooleanRequest(), "should bool request");
        assertFalse(result.isGatherRequest(), "should not gather request");
        assertEquals(1, result.positiveSensorNodes().size(), "1 positive node");
        assertTrue(result.positiveSensorNodes().contains("Node_query"), "node_query");
    }


    @Test
    void testVisitGQuery() throws Exception {
        // Tes GQuery
        GQuery gQuery = new GQuery(new FGather("temperature"), new ECont());
        Object result = gQuery.eval(interpreter, executionState);
        
        //assert
        assertTrue(result instanceof QueryResult);
        QueryResult queryResult = (QueryResult) result;
        assertFalse(queryResult.isBooleanRequest());
        assertTrue(queryResult.isGatherRequest());
        assertEquals(1, queryResult.gatheredSensorsValues().size());
        assertEquals(35.0, queryResult.gatheredSensorsValues().get(0).getValue());
    }
    
    @Test
    void testECont() throws Exception {
        ExecutionState data1 = new ExecutionState();
        ECont test_ECont = new ECont();

        assertFalse(data1.isContinuationSet(), "before eval，continuation not set");
        test_ECont.eval(interpreter, data1);
        assertTrue(data1.isContinuationSet(), "after eval，continuation should set");
    }

    @Test
    void testDCont() throws Exception {
        ExecutionState data2 = new ExecutionState();
        DCont test_DCont = new DCont(new RDirs(Direction.NW, new FDirs(Direction.NE)), 5);

        assertFalse(data2.isContinuationSet(), "before eval，continuation not set");
        test_DCont.eval(interpreter, data2);
        assertTrue(data2.isContinuationSet(), "after eval，continuation should set");
        assertTrue(data2.isDirectional(), "set direction");
        assertEquals(5, data2.getHops(), "nb saut == 5");
    }

    @Test
    void testFCont() throws Exception {
        ExecutionState data3 = new ExecutionState();
        FCont test_FCont = new FCont(new ABase(new Position(15.0, 6.0)), 20.0);

        assertFalse(data3.isContinuationSet(), "before eval，continuation not set");
        test_FCont.eval(interpreter, data3);
        assertTrue(data3.isContinuationSet(), "after eval，continuation should set");
        assertTrue(data3.isFlooding(), "set flooding");
        assertEquals(20.0, data3.getMaximalDistance(), "maxdistance 20.0");
    }

    
    

}
