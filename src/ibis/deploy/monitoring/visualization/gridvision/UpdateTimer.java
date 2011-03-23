package ibis.deploy.monitoring.visualization.gridvision;

public class UpdateTimer implements Runnable {
	private JungleGoggles jv;
	
	public UpdateTimer(JungleGoggles jv) {
		this.jv = jv;
	}
	
	public void run() {
		while (true) {
			jv.doUpdateRequest();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {				
				break;
			}
		}
	}
	
}
