package nodes;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.examples.basic_cs.components.URIConsumer;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PImplI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import ports.ClientOutboundPort;
import request.Request;
import request.ast.Query;
import request.ast.astCont.DCont;
import request.ast.astCont.ECont;
import request.ast.astGather.FGather;
import request.ast.astQuery.GQuery;
import sensor_network.QueryResult;

@RequiredInterfaces(required = {RequestingCI.class})
public class ClientComponent extends AbstractComponent {
	protected ClientOutboundPort client_port;
	
	protected ClientComponent(String uri, String outboundPortURI) throws Exception {
//        super(uri, 1, 0); // 1 scheduled thread pool, 0 simple thread pool
		super(uri, 0, 1);
        // init port (required interface)
        this.client_port = new ClientOutboundPort(outboundPortURI, this);
        //publish port(an outbound port is always local)
        this.client_port.localPublishPort();
        
        this.getTracer().setTitle("client") ;
		this.getTracer().setRelativePosition(1, 1) ;
        
        AbstractComponent.checkImplementationInvariant(this);
		AbstractComponent.checkInvariant(this);
    }
	public void sendRequest() throws Exception{
		 this.logMessage("ClientComponent Sending request....");
		GQuery test = new GQuery(new FGather("temperature"),new ECont());
        String requestURI = "gather-uri";	      
        RequestI request = new Request(requestURI,test,false,null);
        QueryResult result = (QueryResult) this.client_port.execute(request);
	}
	
	@Override
    public void start() throws ComponentStartException {
		this.logMessage("ClientComponent started.");
        super.start();
    }
	
	 @Override
	    public void execute() throws Exception {
		    this.logMessage("ClientComponent executed.");
		    this.runTask(
					new AbstractComponent.AbstractTask() {
						@Override
						public void run() {
							try {				
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
	     this.client_port.unpublishPort();
	     super.finalise();
	    }
	
	
	
}
