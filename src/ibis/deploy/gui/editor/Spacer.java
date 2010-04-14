package ibis.deploy.gui.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

public class Spacer extends JPanel {
    private static final long serialVersionUID = 2750127182956648302L;

    public Spacer(String labelText) {
        super();

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        JLabel label = new JLabel(labelText);
        Font font = label.getFont();
        label.setFont(font.deriveFont(Font.BOLD));
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        add(label, BorderLayout.WEST);
        revalidate();

        JPanel separatorPanel = new JPanel();
        separatorPanel
                .setLayout(new BoxLayout(separatorPanel, BoxLayout.Y_AXIS));
        separatorPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
        separatorPanel.add(separator);
        add(separatorPanel, BorderLayout.CENTER);
    }

}
