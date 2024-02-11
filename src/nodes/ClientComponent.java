package nodes;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
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

@RequiredInterfaces(required = {RequestingCI.class})
public class ClientComponent extends AbstractComponent {
	protected ClientOutboundPort client_port;
	
	public ClientComponent(String uri, String requestingPortURI) throws Exception {
        super(uri, 1, 0); // 1 scheduled thread pool, 0 simple thread pool

        // init port (required interface)
        this.client_port = new ClientOutboundPort(requestingPortURI, this);
        //publish port(an outbound port is always local)
        this.client_port.localPublishPort();
    }
	
	@Override
    public void start() throws ComponentStartException {
		this.logMessage("ClientComponent started.");
        super.start();
    }
	
	 @Override
	    public void execute() throws Exception {
	        super.execute();	   
	        GQuery test = new GQuery(new FGather("temperature")  ,new ECont());
	        String requestURI = "gather-uri";	      
	        RequestI request = new Request(requestURI,test,false,null);
	        QueryResultI result = this.client_port.execute(request);

	    }
	 
	 @Override
	    public void finalise() throws Exception {
		 this.logMessage("stopping Client component.") ;
		 this.printExecutionLogOnFile("Client");
	     this.client_port.unpublishPort();
	     super.finalise();
	    }
	
	
	
}
