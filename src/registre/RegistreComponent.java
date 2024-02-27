package registre;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.*;
import registre.ports.LookupInboundPort;
//import registry.ports.RegistrationInboundPort;
//import registry.ports.LookupInboundPort;
import registre.ports.RegistrationInboundPort;


@OfferedInterfaces(offered = {RegistrationCI.class, LookupCI.class})
public class RegistreComponent extends AbstractComponent {
    protected String registrationInboundPortURI;
    protected String lookupInboundPortURI;
    protected String uriPrefix;

    protected RegistreComponent(String uriPrefix,String registrationInboundPortURI, String lookupInboundPortURI) throws Exception {
        super(uriPrefix , 1, 0); // 假设有一个调度线程池，没有普通线程池
        this.registrationInboundPortURI = registrationInboundPortURI;
        this.lookupInboundPortURI = lookupInboundPortURI;
        
        assert registrationInboundPortURI != null && !registrationInboundPortURI.isEmpty() : "InboundPort URI cannot be null or empty!";
        assert lookupInboundPortURI != null && !lookupInboundPortURI.isEmpty() : "InboundPort URI cannot be null or empty!";
        // 创建并发布Registration服务的入站端口
        RegistrationInboundPort registrationPort = new RegistrationInboundPort(registrationInboundPortURI, this);
        registrationPort.publishPort();

        // 创建并发布Lookup服务的入站端口
        LookupInboundPort lookupPort = new LookupInboundPort(lookupInboundPortURI, this);
        lookupPort.publishPort();
        
		if (AbstractCVM.isDistributed) {
			this.getLogger().setDirectory(System.getProperty("user.dir"));
		} else {
			this.getLogger().setDirectory(System.getProperty("user.home"));
		}
		
		this.getTracer().setTitle("Registre");
		this.getTracer().setRelativePosition(2, 0);
    }

    @Override
    public void start() throws ComponentStartException {
        super.start();
        this.logMessage("RegistreComponent started.");
    }
    
	 @Override
	    public void finalise() throws Exception {
	        super.finalise();
	        this.logMessage("RegistreComponent finalising...");
	    }

    @Override
    public void shutdown() throws ComponentShutdownException {
        super.shutdown();
        this.logMessage("RegistreComponent shutting down.");
    }

}
