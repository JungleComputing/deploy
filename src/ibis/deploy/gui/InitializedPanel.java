package ibis.deploy.gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

public class InitializedPanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 2528896847051125675L;

    private GUI gui;

    public InitializedPanel(GUI gui) {
        setLayout(new BorderLayout());
        this.gui = gui;
    }

    public void init() {
        MyTableModel model = new MyTableModel();
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new ExperimentEditorPanel(gui, model), new MonitoringPanel(gui,
                        model));
        splitPane.setOneTouchExpandable(true);
        add(splitPane, BorderLayout.CENTER);

    }

}
