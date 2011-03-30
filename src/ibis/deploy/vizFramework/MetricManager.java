package ibis.deploy.vizFramework;

import java.util.ArrayList;

import ibis.deploy.monitoring.collection.Collector;
import ibis.deploy.monitoring.collection.Ibis;
import ibis.deploy.monitoring.collection.Location;

public class MetricManager {
	private Collector collector;
	
	public MetricManager(final Collector collector){
		
		this.collector = collector;
		
		//Create update thread
		DataRefreshTimer updater = new DataRefreshTimer(this);
		new Thread(updater).start();
	}
	
	public void update(){
		if(collector.change()){
			Location root = collector.getRoot();
			System.out.println(root.getName());

			printLocations(root, "  ");
			
			System.out.println("------------------------------------");
		}
	}
	
	private void printLocations(Location root, String spacer){
		ArrayList<Location> dataChildren = root.getChildren();
		if(dataChildren == null || dataChildren.size() == 0){
			ArrayList<Ibis> ibises = root.getAllIbises();
			for(Ibis ibis:ibises){
				System.out.println(spacer + ibis.toString());
			}
		}
		for(Location loc:dataChildren){
			System.out.println(spacer + loc.getName());
			printLocations(loc, spacer.concat("  "));
		}
	}
	
	public Collector getCollector(){
		return collector;
	}
}

class DataRefreshTimer implements Runnable {
	private MetricManager mgr;
	
	private int UPDATE_INTERVAL = 1000;
	
	public DataRefreshTimer(MetricManager mgr) {
		this.mgr = mgr;
		UPDATE_INTERVAL = mgr.getCollector().getRefreshRate();
	}
	
	public void run() {
		while (true) {
			mgr.update();
			try {
				Thread.sleep(UPDATE_INTERVAL);
			} catch (InterruptedException e) {				
				break;
			}
		}
	}
	
}
