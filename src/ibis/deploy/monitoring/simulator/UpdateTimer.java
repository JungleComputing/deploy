package ibis.deploy.monitoring.simulator;

public class UpdateTimer implements Runnable {
	private FakeService service;
	
	public UpdateTimer(FakeService service) {
		this.service = service;
	}
	
	public void run() {
		while (true) {
			service.doUpdate();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {				
				break;
			}
		}
	}
	
}
