package ibis.deploy.gui.editor;

import ibis.deploy.gui.misc.Utils;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

public class Spacer extends JPanel {
    private static final long serialVersionUID = 2750127182956648302L;

    public Spacer(String labelText) {
        super();

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        JLabel label = new JLabel(labelText);
        Font font = label.getFont();
        label.setFont(font.deriveFont(Font.BOLD));
        add(label);
        revalidate();

        JPanel separatorPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
        separator.setPreferredSize(new Dimension(2 * Utils.defaultLabelWidth
                - label.getPreferredSize().width,
                separator.getPreferredSize().height));
        separatorPanel.add(separator);

        add(separatorPanel);
    }

}
