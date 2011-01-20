package ibis.deploy.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import ibis.deploy.gui.misc.Utils;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class DetachableTab {

    private final String title;
    private final JTabbedPane tabPane;
    private final ImageIcon icon;
    private final DetachableTabComponent tabComponent;
    private final JPanel contents;

    public DetachableTab(String title, String iconPath, JPanel contents,
            JTabbedPane tabPane) {
        this.tabPane = tabPane;
        this.contents = contents;
        this.title = title;
        
        icon = Utils.createImageIcon(iconPath, title);
        
        tabComponent = new DetachableTabComponent(this,
                icon, title);
        
        addTab();
    }

    public void addTab() {
        tabPane.addTab(title, contents);
        int index = tabPane.indexOfTab(title);
        tabPane.setTabComponentAt(index, tabComponent);
    }

    // called from button in tab component
    public void detach() {
        int index = tabPane.indexOfTab(title);

        if (index != -1) {
            tabPane.remove(index);
        }

        new DetachedFrame(this);

    }

    private class DetachedFrame extends JFrame {
        private static final long serialVersionUID = 1L;

        DetachedFrame(DetachableTab tab) {
            setTitle("Ibis Deploy - " + title);

            setIconImage(icon.getImage());

            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(contents, BorderLayout.CENTER);

            setPreferredSize(new Dimension(GUI.DEFAULT_SCREEN_WIDTH,
                    GUI.DEFAULT_SCREEN_HEIGHT));

            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent we) {
                    Component component = we.getComponent();
                    if (component instanceof DetachedFrame) {
                        DetachedFrame frame = (DetachedFrame) component;

                        // get rid of frame
                        frame.dispose();
                        // add tab again
                        addTab();
                    }

                }

            });

            // Display the window.
            pack();
            setVisible(true);
        }

    }

}
