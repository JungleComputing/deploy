package ibis.amuse.visualization;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeListener;

public class GoggleSwing {

    public GoggleSwing() {
    }

    public static Box titleBox(String label, ItemListener listener) {
        ArrayList<Component> vcomponents = new ArrayList<Component>();

        vcomponents.add(verticalStrut(5));
        Box hrzOuterBox = Box.createHorizontalBox();
        hrzOuterBox.add(horizontalStrut(5));

        Box hrzInnerBox = Box.createHorizontalBox();
        JLabel panelTitle = new JLabel(label);
        hrzInnerBox.add(panelTitle);

        hrzInnerBox.add(Box.createHorizontalGlue());

        Icon exitIcon = new ColorIcon(0, 0, 0);
        JCheckBox exit = new JCheckBox(exitIcon);
        exit.addItemListener(listener);
        hrzInnerBox.add(exit);
        hrzOuterBox.add(hrzInnerBox);

        hrzOuterBox.add(horizontalStrut(5));
        vcomponents.add(hrzOuterBox);

        vcomponents.add(verticalStrut(5));

        return vBoxedComponents(vcomponents, false);
    }

    public static Box checkboxBox(String name, String[] labels, boolean[] selections, ItemListener[] listeners) {
        ArrayList<Component> vcomponents = new ArrayList<Component>();
        vcomponents.add(new JLabel(name));
        vcomponents.add(Box.createHorizontalGlue());

        for (int i = 0; i < labels.length; i++) {
            ArrayList<Component> hcomponents = new ArrayList<Component>();

            JCheckBox btn = new JCheckBox();
            btn.setSelected(selections[i]);
            btn.addItemListener(listeners[i]);
            hcomponents.add(btn);

            JLabel label = new JLabel(labels[i]);
            label.setFont(new Font("Arial", Font.PLAIN, 10));
            hcomponents.add(label);

            vcomponents.add(hBoxedComponents(hcomponents));
        }
        return vBoxedComponents(vcomponents, true);
    }

    public static Box legendBox(String name, String[] labels, Float[][] colors, boolean[] selections,
            ItemListener[] listeners) {
        ArrayList<Component> vcomponents = new ArrayList<Component>();
        vcomponents.add(new JLabel(name));
        vcomponents.add(Box.createHorizontalGlue());

        for (int i = 0; i < labels.length; i++) {
            ArrayList<Component> hcomponents = new ArrayList<Component>();

            JCheckBox btn = new JCheckBox();
            btn.setSelected(selections[i]);
            btn.addItemListener(listeners[i]);
            hcomponents.add(btn);

            JCheckBox icon = new JCheckBox(new ColorIcon(colors[i]));
            hcomponents.add(icon);

            JLabel label = new JLabel(labels[i]);
            label.setFont(new Font("Arial", Font.PLAIN, 10));
            hcomponents.add(label);

            vcomponents.add(hBoxedComponents(hcomponents));
        }
        return vBoxedComponents(vcomponents, true);
    }

    public static Box radioBox(String name, String[] labels, ActionListener[] actions) {
        ArrayList<Component> vcomponents = new ArrayList<Component>();
        ButtonGroup group = new ButtonGroup();

        vcomponents.add(new JLabel(name));
        vcomponents.add(Box.createHorizontalGlue());

        for (int i = 0; i < labels.length; i++) {
            JRadioButton btn = new JRadioButton(labels[i]);
            group.add(btn);
            if (i == 0)
                btn.setSelected(true);
            btn.addActionListener(actions[i]);
            vcomponents.add(btn);
        }
        return vBoxedComponents(vcomponents, true);
    }

    public static Box sliderBox(String label, ChangeListener listener, int min, int max, int spacing, int norm,
            JLabel dynamicLabel) {
        ArrayList<Component> components = new ArrayList<Component>();
        JLabel thresholdlabel = new JLabel(label);
        components.add(thresholdlabel);
        components.add(Box.createHorizontalGlue());

        JSlider slider = new JSlider();
        slider.setMinimum(min);
        slider.setMaximum(max);
        slider.setMinorTickSpacing(spacing);
        slider.setPaintTicks(true);
        slider.setSnapToTicks(true);
        slider.setValue(norm);
        slider.addChangeListener(listener);
        components.add(slider);

        components.add(dynamicLabel);
        return vBoxedComponents(components, true);
    }

    public static Box buttonBox(String name, String[] labels, ActionListener[] actions) {
        ArrayList<Component> vcomponents = new ArrayList<Component>();

        vcomponents.add(new JLabel(name));
        vcomponents.add(Box.createHorizontalGlue());

        for (int i = 0; i < labels.length; i++) {
            ArrayList<Component> hcomponents = new ArrayList<Component>();
            hcomponents.add(Box.createHorizontalGlue());

            JButton btn = new JButton(labels[i]);
            btn.addActionListener(actions[i]);
            btn.setPreferredSize(new Dimension(150, 10));
            hcomponents.add(btn);

            hcomponents.add(Box.createHorizontalGlue());
            vcomponents.add(hBoxedComponents(hcomponents));
        }

        vcomponents.add(verticalStrut(5));

        return vBoxedComponents(vcomponents, true);
    }

    private static Box vBoxedComponents(ArrayList<Component> components, boolean bordered) {
        Box hrzBox = Box.createHorizontalBox();
        hrzBox.add(horizontalStrut(2));

        Box vrtBox = Box.createVerticalBox();
        if (bordered)
            vrtBox.setBorder(new BevelBorder(BevelBorder.RAISED));
        vrtBox.setAlignmentY(Component.TOP_ALIGNMENT);

        vrtBox.add(verticalStrut(5));

        for (int i = 0; i < components.size(); i++) {
            Component current = components.get(i);
            vrtBox.add(current);
        }
        hrzBox.add(vrtBox);

        hrzBox.add(horizontalStrut(2));
        return hrzBox;
    }

    private static Box hBoxedComponents(ArrayList<Component> components) {
        Box hrzOuterBox = Box.createHorizontalBox();
        hrzOuterBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        hrzOuterBox.add(horizontalStrut(2));

        Box hrzInnerBox = Box.createHorizontalBox();
        hrzInnerBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (int i = 0; i < components.size(); i++) {
            Component current = components.get(i);
            hrzInnerBox.add(current);
        }
        hrzOuterBox.add(hrzInnerBox);

        hrzOuterBox.add(horizontalStrut(2));

        return hrzOuterBox;
    }

    public static Component verticalStrut(int size) {
        Component verticalStrut = Box.createRigidArea(new Dimension(0, size));
        return verticalStrut;
    }

    public static Component horizontalStrut(int size) {
        Component verticalStrut = Box.createRigidArea(new Dimension(size, 0));
        return verticalStrut;
    }

    /**
     * Returns an JButton, or null if the path was invalid.
     * 
     * @param buttonText
     */
    public static JButton createImageButton(String path, String description, String buttonText) {
        ImageIcon icon = createImageIcon(path, description);
        if (icon == null) {
            System.out.println("Icon bogus");
        }
        JButton result = new JButton(buttonText, icon);
        result.setHorizontalAlignment(SwingConstants.CENTER);
        result.setMargin(new Insets(2, 2, 2, 2));
        result.setVerticalTextPosition(AbstractButton.CENTER);
        result.setHorizontalTextPosition(AbstractButton.TRAILING);
        result.setToolTipText(description);
        result.setFocusPainted(false);
        return result;
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    public static ImageIcon createImageIcon(String path, String description) {
        URL imgURL = null;

        if (path != null) {
            imgURL = ClassLoader.getSystemClassLoader().getResource(path);
        }

        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            return null;
        }
    }
}
