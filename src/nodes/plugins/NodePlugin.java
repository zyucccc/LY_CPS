package nodes.plugins;

import fr.sorbonne_u.components.AbstractPlugin;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import registre.interfaces.RegistrationCI;

public class NodePlugin extends AbstractPlugin {
    private static final long serialVersionUID = 1L;

        public NodePlugin() {
            super();
        }

    // -------------------------------------------------------------------------
    // Plug-in life-cycle
    // -------------------------------------------------------------------------
    @Override
    public void	installOn(ComponentI owner) throws Exception
    {
        super.installOn(owner);
        this.addRequiredInterface(RegistrationCI.class);
        this.addOfferedInterface(RequestingCI.class);
        this.addOfferedInterface(SensorNodeP2PCI.class);
    }

    @Override
    public void	initialise() throws Exception
    {
        super.initialise();
    }

    @Override
    public void	finalise() throws Exception
    {
        super.finalise();
    }

    @Override
    public void	uninstall() throws Exception
    {
        super.uninstall();
    }
    // -------------------------------------------------------------------------
    // Plug-in methods
    // -------------------------------------------------------------------------


}
