package ast.astGather;

import ast.Gather;

public class RGather extends Gather{

	private final Gather gather;

	public RGather(String sensorId,Gather gather) {
		super(sensorId);
		this.gather=gather;
	}
	
	public Gather getGather() {
		return gather;
	}

}
