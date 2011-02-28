package ibis.deploy.gui.gridvision;

/**
 * The data gathering module's representation of an Ibis Pool. 
 */
public interface Pool {
	
	public String getName();
	
	public Ibis[] getIbises();
}