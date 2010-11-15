package ibis.deploy.gui.deployViz.helpers;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Timer;

import javax.swing.AbstractAction;

import ibis.ipl.support.management.AttributeDescription;

//import ibis.deploy.gui.gridvision.dataholders.*;
//import ibis.deploy.gui.gridvision.metrics.link.*;
//import ibis.deploy.gui.gridvision.metrics.node.*;
//import ibis.deploy.gui.gridvision.metrics.special.*;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.server.ManagementServiceInterface;
import ibis.ipl.server.RegistryServiceInterface;

public class MetricsManager extends Thread {

    private ManagementServiceInterface manInterface;
    private RegistryServiceInterface regInterface;
    private int refreshrate = 2000;
    private Timer refreshTimer;

    public MetricsManager(ManagementServiceInterface manInterface,
            RegistryServiceInterface regInterface) {

        this.manInterface = manInterface;
        this.regInterface = regInterface;

        AbstractAction updateVisualizationAction = new AbstractAction() {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent arg0) {
                try {
                    getIbisesInfo();
                } catch (Exception exc) {
                    exc.printStackTrace();
                }
            }

        };

        refreshTimer = new Timer(refreshrate, updateVisualizationAction);
        refreshTimer.start();
    }

    public void stopTimer() {
        refreshTimer.stop();

        //TODO - this is thrown when the application is closed
        // "x error of failed request badwindow invalid window parameter" error,
        // which seems to occur when the performance data is returned after the
        // main window closes
    }

    public void run() {
        while (true) {
            // update();
            getIbisesInfo();
            try {
                Thread.sleep(refreshrate);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void getIbisesInfo() {
        Map<String, Integer> newSizes = new HashMap<String, Integer>();
        IbisIdentifier[] ibises = null;

        try {
            newSizes = regInterface.getPoolSizes();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (Map.Entry<String, Integer> entry : newSizes.entrySet()) {
            String poolName = entry.getKey();

            try {
                ibises = regInterface.getMembers(poolName);
            } catch (IOException e) {
                e.printStackTrace();
            }

            AttributeDescription load = new AttributeDescription(
                    "java.lang:type=OperatingSystem", "SystemLoadAverage");

            AttributeDescription cpu = new AttributeDescription(
                    "java.lang:type=OperatingSystem", "ProcessCpuTime");

            AttributeDescription connections = new AttributeDescription("ibis",
                    "connections");

            // for each ibis, print these attributes
            if (ibises != null) {
                for (IbisIdentifier ibis : ibises) {
                    try {
                        System.err
                                .println(ibis
                                        + " connected to = "
                                        + Arrays
                                                .toString((IbisIdentifier[]) manInterface
                                                        .getAttributes(ibis,
                                                                connections)[0]));

                    } catch (Exception e) {
                        System.err.println("Could not get management info: ");
                        e.printStackTrace();
                    }
                }

            }
        }
    }
}
