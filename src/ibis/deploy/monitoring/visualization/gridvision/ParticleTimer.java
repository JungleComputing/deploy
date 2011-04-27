package ibis.deploy.monitoring.visualization.gridvision;

public class ParticleTimer implements Runnable {
	private static final int APS = 25; 
	private JungleGoggles goggles;
	
	private long startTime = 0;
	private int waitTime;
    
    public ParticleTimer(JungleGoggles goggles) {
    	this.goggles = goggles;
    	
    	waitTime = 1000 / APS;
    }

    public void startTiming(long timeToComplete) {
    	this.startTime = System.currentTimeMillis();
        
    }

	public void run() {
		while(true) {
			long currentTime = System.currentTimeMillis();
			long elapsed = currentTime - startTime;
			startTime = currentTime;
			
			float fraction = elapsed / 1000f;
			goggles.doParticleMoves(fraction);
			
			try {
				if (waitTime - elapsed > 0) { 
					Thread.sleep(waitTime - elapsed);
				} else {
					Thread.sleep(0);
				}
			} catch (InterruptedException e) {				
				e.printStackTrace();
			}
		}
	}
}
