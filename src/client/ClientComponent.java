package client;

import java.util.concurrent.TimeUnit;

import client.ports.ClientOutboundPort;
import client.ports.ClientRegistreOutboundPort;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.examples.basic_cs.components.URIConsumer;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PImplI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import request.Request;
import request.ast.Query;
import request.ast.astCont.DCont;
import request.ast.astCont.ECont;
import request.ast.astGather.FGather;
import request.ast.astQuery.GQuery;
import sensor_network.ConnectionInfo;
import sensor_network.EndPointDescriptor;
import sensor_network.QueryResult;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI;
import nodes.connectors.NodeClientConnector;

@RequiredInterfaces(required = {RequestingCI.class,LookupCI.class})
public class ClientComponent extends AbstractComponent {
	protected ClientOutboundPort client_node_port;
	protected ClientRegistreOutboundPort client_registre_port;
	
	protected ClientComponent(String uri, String Client_Node_outboundPortURI,String Client_Registre_outboundPortURI) throws Exception {
//        super(uri, 1, 0); // 1 scheduled thread pool, 0 simple thread pool
		super(uri, 0, 1);
        // init port (required interface)
        this.client_node_port = new ClientOutboundPort(Client_Node_outboundPortURI, this);
        //publish port(an outbound port is always local)
        this.client_node_port.localPublishPort();
        
        //client - registre - LookupCI
        this.client_registre_port = new ClientRegistreOutboundPort(Client_Registre_outboundPortURI,this);
        this.client_registre_port.localPublishPort();
        
        this.getTracer().setTitle("client") ;
		this.getTracer().setRelativePosition(0, 0) ;
        
        AbstractComponent.checkImplementationInvariant(this);
		AbstractComponent.checkInvariant(this);
    }
	public void sendRequest() throws Exception{
		 this.logMessage("ClientComponent Sending request....");
		GQuery test = new GQuery(new FGather("temperature"),new ECont());
        String requestURI = "gather-uri";	      
        RequestI request = new Request(requestURI,test,false,null);
        QueryResult result = (QueryResult) this.client_node_port.execute(request);
        this.logMessage("ClientComponentr Receive resultat de request:");
        this.logMessage("" + result);
	}
	
	public void findEtConnecterByIdentifer(String NodeID) throws Exception {
		this.logMessage("ClientComponent search and connect Node :" + NodeID);
		ConnectionInfo connectionInfo = (ConnectionInfo) this.client_registre_port.findByIdentifier(NodeID);
		if (connectionInfo != null) {
		String InboundPortURI = ((EndPointDescriptor)connectionInfo.endPointInfo()).getURI();
		this.logMessage("ClientComponent start connect : Uri obtenue to connect :" + InboundPortURI);
		try {
		this.client_node_port.doConnection(InboundPortURI, NodeClientConnector.class.getCanonicalName());
		}catch(Exception e) {
			 this.logMessage("Exception occurred during connection: " + e.toString());
		        // 这里可以选择打印更详细的异常堆栈信息
		        e.printStackTrace();
		}
		this.logMessage("ClientComponent : Connection established with Node: " + NodeID);
		}else {
			this.logMessage("Node " + NodeID + " not found or cannot connect.");
		}
	}
	
	@Override
    public void start() throws ComponentStartException {
		this.logMessage("ClientComponent started.");
        super.start();
        this.scheduleTask(new AbstractComponent.AbstractTask() {
            @Override
            public void run() {
                try {
                    // 尝试连接到指定的节点
                    String NodeID = "node2";
                    ((ClientComponent)this.getTaskOwner()).findEtConnecterByIdentifer(NodeID);
                    // 如果需要，此处可以继续执行其他任务，如发送请求等
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 3000, TimeUnit.MILLISECONDS); 
    }
	
	 @Override
	    public void execute() throws Exception {
		    this.logMessage("ClientComponent executed.");
		    this.runTask(
					new AbstractComponent.AbstractTask() {
						@Override
						public void run() {
							try {				
								//connecter au Node 
//								String NodeID = "node2";
//								((ClientComponent)this.getTaskOwner()).findEtConnecterByIdentifer(NodeID);
								//envoyer query
								((ClientComponent)this.getTaskOwner()).sendRequest() ;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}) ;	    

	    }
	 
	 @Override
	    public void finalise() throws Exception {
		 this.logMessage("stopping Client component.") ;
		 this.printExecutionLogOnFile("Client");
	     this.client_node_port.unpublishPort();
	     super.finalise();
	    }
	
	
	
}
