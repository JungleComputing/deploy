package ibis.deploy.monitoring.visualization.gridvision;

public class Mover {
	private Float[] oldLocation, currentLocation, newLocation;
	private Timer timer;
	
	private boolean locationChanged;
	
	public Mover() {
		oldLocation = new Float[3];
		oldLocation[0] = 0.0f;
		oldLocation[1] = 0.0f;
		oldLocation[2] = 0.0f;
		
		currentLocation = new Float[3];
		currentLocation[0] = 0.0f;
		currentLocation[1] = 0.0f;
		currentLocation[2] = 0.0f;
		
		newLocation = new Float[3];
		newLocation[0] = 0.0f;
		newLocation[1] = 0.0f;
		newLocation[2] = 0.0f;
		
		timer = new Timer(this);
		new Thread(timer).start();
		
		locationChanged = false;
	}
	
	public Float[] getCurrentCoordinates() {
		Float[] tempLocation = new Float[3];
		tempLocation[0] = currentLocation[0];
		tempLocation[1] = currentLocation[1];
		tempLocation[2] = currentLocation[2];
		return tempLocation;	
	}
	
	public void moveTo(Float[] newCenter) {		
		this.oldLocation[0] = this.currentLocation[0];
		this.oldLocation[1] = this.currentLocation[1];
		this.oldLocation[2] = this.currentLocation[2];
		
		this.newLocation[0] = currentLocation[0]-newCenter[0];
		this.newLocation[1] = currentLocation[1]-newCenter[1];
		this.newLocation[2] = currentLocation[2]-newCenter[2];
		
		timer.startTiming(2000);		
	}
	
	public void doMoveFraction(float fraction) {		
		if (fraction>1.0f || fraction < 0.0f) { return; }
		currentLocation[0] = oldLocation[0] + (fraction * ( newLocation[0] - oldLocation[0] ));
		currentLocation[1] = oldLocation[1] + (fraction * ( newLocation[1] - oldLocation[1] ));
		currentLocation[2] = oldLocation[2] + (fraction * ( newLocation[2] - oldLocation[2] ));

		locationChanged = true;
	}
	
	public boolean locationChanged() {
		if (locationChanged) {
			locationChanged = false;
			return true;
		} else {
			return false;
		}		
	}
}
