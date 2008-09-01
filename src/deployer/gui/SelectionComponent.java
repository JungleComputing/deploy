package deployer.gui;

import javax.swing.JPanel;

public interface SelectionComponent {

    public static final int DEFAULT_COMPONENT_WIDTH = 130;

    public JPanel getPanel();

    public Object[] getValues() throws Exception;

    public void update();

}
