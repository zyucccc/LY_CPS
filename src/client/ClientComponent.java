package client;

import java.util.concurrent.TimeUnit;

import client.ports.ClientAsynRequestInboundPort;
import client.ports.ClientOutboundPort;
import client.ports.ClientRegistreOutboundPort;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.ports.PortI;
import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import request.Request;
import request.ast.Direction;
import request.ast.Query;
import request.ast.astBase.RBase;
import request.ast.astBexp.SBExp;
import request.ast.astCont.DCont;
import request.ast.astCont.ECont;
import request.ast.astCont.FCont;
import request.ast.astDirs.FDirs;
import request.ast.astDirs.RDirs;
import request.ast.astGather.FGather;
import request.ast.astQuery.GQuery;
import request.ast.astQuery.BQuery;
import sensor_network.ConnectionInfo;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestResultCI;
import sensor_network.EndPointDescriptor;
import sensor_network.QueryResult;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI;
import nodes.connectors.NodeClientConnector;
import nodes.ports.SensorNodeInboundPort;

@RequiredInterfaces(required = {RequestingCI.class,LookupCI.class})
@OfferedInterfaces(offered = {RequestResultCI.class})
public class ClientComponent extends AbstractComponent {
	protected ClientOutboundPort client_node_port;
	protected ClientRegistreOutboundPort client_registre_port;
	protected String ClientAsyncInboundPortURI = null;
	
	protected ClientComponent(String uri, String Client_Node_outboundPortURI,String Client_Registre_outboundPortURI,String Client_AsynRequest_inboundPortURI) throws Exception {
//        super(uri, 1, 0); // 1 scheduled thread pool, 0 simple thread pool
		super(uri, 0, 1);
		
		//publish InboundPort
		 PortI InboundPort_AsynRequest = new ClientAsynRequestInboundPort(Client_AsynRequest_inboundPortURI, this);
		 InboundPort_AsynRequest.publishPort();
		
        // init port (required interface)
        this.client_node_port = new ClientOutboundPort(Client_Node_outboundPortURI, this);
        //publish port(an outbound port is always local)
        this.client_node_port.localPublishPort();
        
        //client - registre - LookupCI
        this.client_registre_port = new ClientRegistreOutboundPort(Client_Registre_outboundPortURI,this);
        this.client_registre_port.localPublishPort();
        
        //Async port URI
        this.ClientAsyncInboundPortURI = Client_AsynRequest_inboundPortURI;
        
        this.getTracer().setTitle("client") ;
		this.getTracer().setRelativePosition(0, 0) ;
        
        AbstractComponent.checkImplementationInvariant(this);
		AbstractComponent.checkInvariant(this);
    }
	
	//Parti Sync Send request:
	public void sendRequest_direction() throws Exception{
		this.logMessage("----------------- Query Resultat (Direction) ------------------");
		 this.logMessage("ClientComponent Sending request Direction....");
		 int nb_saut = 1;
		GQuery test = new GQuery(new FGather("temperature"),new DCont(new FDirs(Direction.NE),nb_saut));
        String requestURI = "gather-request-uri";	      
        RequestI request = new Request(requestURI,test,false,null);
        QueryResult result = (QueryResult) this.client_node_port.execute(request);
        this.logMessage("ClientComponentr Receive resultat de request:");
        this.logMessage("" + result);
        this.logMessage("----------------------------------");
	}
        
	
	public void sendRequest_flooding() throws Exception{
		this.logMessage("----------------- Query Resultat (Flooding)------------------");
		 this.logMessage("ClientComponent Sending request Flooding....");
		double max_distance =8.0;
		BQuery test = new BQuery(new SBExp("fumée"),new FCont(new RBase(),max_distance));
        String requestURI = "flood-request-uri";	      
        RequestI request = new Request(requestURI,test,false,null);
        QueryResult result = (QueryResult) this.client_node_port.execute(request);
        this.logMessage("ClientComponentr Receive resultat de request:");
        this.logMessage("" + result);
        this.logMessage("----------------------------------");
	}
	
	public void findEtConnecterByIdentifer(String NodeID) throws Exception {
		this.logMessage("---------------Connect to Node-------------------");
		this.logMessage("ClientComponent search and connect Node :" + NodeID);
		ConnectionInfo connectionInfo = (ConnectionInfo) this.client_registre_port.findByIdentifier(NodeID);
		if (connectionInfo != null) {
		String InboundPortURI = ((EndPointDescriptor)connectionInfo.endPointInfo()).getURI();
//		this.logMessage("ClientComponent start connect : Uri obtenue to connect :" + InboundPortURI);
//		try {
//		this.logMessage("Test name class: "+NodeClientConnector.class.getCanonicalName());
		this.client_node_port.doConnection(InboundPortURI, NodeClientConnector.class.getCanonicalName());
//		}catch(Exception e) {
//			 this.logMessage("Exception occurred during connection: " + e.toString());
//		        e.printStackTrace();
//		}
		this.logMessage("ClientComponent : Connection established with Node: " + NodeID);
		}else {
			this.logMessage("Node " + NodeID + " not found or cannot connect.");
		}
		this.logMessage("----------------------------------");
	}
	
	//////////////////////////////////////////////////////////////////////////////
	// Partie Async
	//Request Direction Async
	public void sendRequest_direction_Asyn()throws Exception{
        if(this.ClientAsyncInboundPortURI!=null) {
    	this.logMessage("----------------- Query Request Async (Direction) ------------------");
   		this.logMessage("ClientComponent Sending Request Async Direction....");
		EndPointDescriptor endpointDescriptor = new EndPointDescriptor(ClientAsyncInboundPortURI);
        ConnectionInfo connection_info = new ConnectionInfo("client",endpointDescriptor);
        int nb_saut = 2;
		GQuery test = new GQuery(new FGather("temperature"),new DCont(new FDirs(Direction.NE),nb_saut));
        String requestURI = "gather-request-Async-uri";	      
        RequestI request = new Request(requestURI,test,true,connection_info);
        this.client_node_port.executeAsync(request);
        
        }else {
        	System.err.println("ClientAsyncInboundPortURI NULL");
        }	
	}
	
	public void sendRequest_flooding_Asyn() throws Exception{
		this.logMessage("----------------- Query Resultat Async (Flooding)------------------");
		this.logMessage("ClientComponent Sending request Async Flooding....");
		
		EndPointDescriptor endpointDescriptor = new EndPointDescriptor(ClientAsyncInboundPortURI);
	    ConnectionInfo connection_info = new ConnectionInfo("client",endpointDescriptor);
		 
		double max_distance =8.0;
		BQuery test = new BQuery(new SBExp("fumée"),new FCont(new RBase(),max_distance));
        String requestURI = "flood-request-Async-uri";	      
        RequestI request = new Request(requestURI,test,true,connection_info);
       
        this.client_node_port.executeAsync(request);
	}
	
	
	
	
	public void	acceptRequestResult(String requestURI,QueryResultI result) throws Exception{
		this.logMessage("-----------------Receive Request Async Resultat ------------------");
		this.logMessage("Receive resultat du request: "+requestURI + "\n Query Result: " + result );
	}
	
	@Override
    public void start() throws ComponentStartException {
		this.logMessage("ClientComponent started.");
        super.start();
        this.scheduleTask(new AbstractComponent.AbstractTask() {
            @Override
            public void run() {
                try {
                    // essayer de connecter de node indiquee
                    String NodeID = "node1";
                    ((ClientComponent)this.getTaskOwner()).findEtConnecterByIdentifer(NodeID);
                    
                    //request Sync
                    ((ClientComponent)this.getTaskOwner()).sendRequest_direction() ;
                    ((ClientComponent)this.getTaskOwner()).sendRequest_flooding() ;
                    
                    //request Async        
                    ((ClientComponent)this.getTaskOwner()).sendRequest_direction_Asyn() ;
                    ((ClientComponent)this.getTaskOwner()).sendRequest_flooding_Asyn() ;
               
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 5000, TimeUnit.MILLISECONDS); 
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
//								((ClientComponent)this.getTaskOwner()).sendRequest() ;
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
		 
		 if (this.client_node_port.connected()) {
	            this.client_node_port.doDisconnection();
	        }
	     this.client_node_port.unpublishPort();
	     if (this.client_registre_port.connected()) {
	            this.client_registre_port.doDisconnection();
	        }
	     this.client_registre_port.unpublishPort();
	     super.finalise();
	    }
	
	
	
}
