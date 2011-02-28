package ibis.deploy.gui.gridvision;

/**
 * A link between two elements that exist within the managed universe. 
 */
public interface Link extends Element {
	
	public Location getLocation();
	
	public Link[] getChildren();
}