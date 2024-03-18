package client;

import java.util.concurrent.TimeUnit;

import client.ports.ClientOutboundPort;
import client.ports.ClientRegistreOutboundPort;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;

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
	public void sendRequest_direction() throws Exception{
		this.logMessage("----------------- Query Resultat (Direction) ------------------");
		 this.logMessage("ClientComponent Sending request Direction....");
		 int nb_saut = 1;
		GQuery test = new GQuery(new FGather("temperature"),new DCont(new FDirs(Direction.NE),nb_saut));
        String requestURI = "gather-uri";	      
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
        String requestURI = "gather-uri2";	      
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
                    ((ClientComponent)this.getTaskOwner()).sendRequest_direction() ;
                    ((ClientComponent)this.getTaskOwner()).sendRequest_flooding() ;
               
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
