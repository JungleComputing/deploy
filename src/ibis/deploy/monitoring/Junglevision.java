package ibis.deploy.monitoring;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.deploy.monitoring.collection.Collector;
import ibis.deploy.monitoring.simulator.FakeManagementService;
import ibis.deploy.monitoring.simulator.FakeRegistryService;
import ibis.deploy.monitoring.visualization.gridvision.swing.GogglePanel;

public class Junglevision {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger("ibis.deploy.gui.junglevision.Junglevision");
	
    public Junglevision() {}
	
    /**
     * main function for the standalone Junglevision program. Not to be used once integrated into deploy.
     */
    public static void main(String[] args) {
    	//Ibis/JMX variables
    	FakeRegistryService regInterface = new FakeRegistryService();
    	FakeManagementService manInterface = new FakeManagementService(regInterface);
    	
    	//Data interface
        Collector collector = ibis.deploy.monitoring.collection.impl.Collector.getCollector(manInterface, regInterface);
		new Thread(collector).start();
		
		//Setup the standalone frame
		final JFrame jframe = new JFrame("JungleGoggles");
		jframe.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				jframe.dispose();
				System.exit(0);
			}
		});
		jframe.getContentPane().add(new GogglePanel(collector), BorderLayout.CENTER );
		jframe.setSize(1800, 1100);
		jframe.setVisible(true);
	}
}