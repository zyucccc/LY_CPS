package ast.astGather;

import ast.Gather;

public class RGather extends Gather{

	private final Gather gather;

	public RGather(int sensorId,Gather gather) {
		super(sensorId);
		// TODO Auto-generated constructor stub
		this.gather=gather;
	}
	public Gather getGather() {
		return gather;
	}

}
