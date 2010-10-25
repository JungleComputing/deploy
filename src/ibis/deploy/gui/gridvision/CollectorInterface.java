package ibis.deploy.gui.gridvision;

public interface CollectorInterface {
	
	public Loc[] getLocations();
	
	public Ibes[] getIbes(int level);
	
	public int getMaxLevels();
	
	public MetricList getAvailableMetrics();
	
	public MetricList getCurrentMetrics();
	
	public getValue(Ibes ibes, Metric metric);
	
	public getCommonDisplayAttributes(Metric metric);
	
	public getCommonDisplayAttributes(Loc location);
	
	public getLoc(IbisIdentifier ibis);
	
}
