package cvm;

import fr.sorbonne_u.components.AbstractPort;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.cvm.AbstractDistributedCVM;

public class DistributedCVM extends AbstractDistributedCVM {
    protected static final String JVM_URI1 = "jvm1";
    protected static final String JVM_URI2 = "jvm2";
    protected static final String JVM_URI3 = "jvm3";
    protected static final String JVM_URI4 = "jvm4";
    protected static final String JVM_URI5 = "jvm5";

    public DistributedCVM(String[] args)throws Exception {
        super(args);
    }

    @Override
    public void instantiateAndPublish() throws Exception {
        if(AbstractCVM.getThisJVMURI().equals(JVM_URI1)){}
        else if (AbstractCVM.getThisJVMURI().equals(JVM_URI2)){}
        else if (AbstractCVM.getThisJVMURI().equals(JVM_URI3)){}
        else if (AbstractCVM.getThisJVMURI().equals(JVM_URI4)){}
        else if (AbstractCVM.getThisJVMURI().equals(JVM_URI5)){}
        else {
            System.out.println("Unknown JVM URI... " + AbstractCVM.getThisJVMURI());
        }
        super.instantiateAndPublish();
    }

    @Override
    public void interconnect() throws Exception {
        if(AbstractCVM.getThisJVMURI().equals(JVM_URI1)){}
        else if (AbstractCVM.getThisJVMURI().equals(JVM_URI2)){}
        else if (AbstractCVM.getThisJVMURI().equals(JVM_URI3)){}
        else if (AbstractCVM.getThisJVMURI().equals(JVM_URI4)){}
        else if (AbstractCVM.getThisJVMURI().equals(JVM_URI5)){}
        else {
            System.out.println("Unknown JVM URI... " + AbstractCVM.getThisJVMURI());
        }
        super.interconnect();
    }

    public static void main(String[] args) {
        try {
            DistributedCVM dcvm = new DistributedCVM(args);
            dcvm.startStandardLifeCycle(10000L);
            Thread.sleep(10000L);
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
    }
    }
}
