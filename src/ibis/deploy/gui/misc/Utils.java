package ibis.deploy.gui.misc;

import java.awt.Color;
import java.awt.Insets;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class Utils {
	
	public static final double localClusterLatitude = 52.332933;
	public static final double localClusterLongitude = 4.866064;
	
	public static final Color selectedClusterFillColor = new Color(255, 100, 100, 200);
    public static final Color selectedClusterBorderColor = new Color(255, 100, 100, 255);
    
    public static final Color defaultClusterBorderColor = new Color(100, 100, 255, 255);
    public static final Color defaultClusterFillColor = new Color(100, 100, 255, 150);

    /** Returns an JLabel, or null if the path was invalid. */
    public static JLabel createImageLabel(String path, String description) {
        JLabel result = new JLabel(createImageIcon(path, description));
        result.setToolTipText(description);
        return result;
    }

    public static Color getColor(String colorString) {
        if (colorString == null || colorString.equals("")) {
            return null;
        }
        return Color.decode(colorString);
    }

    public static Color getLightColor(String colorString) {
        Color color = getColor(colorString);

        if (color == null) {
            return null;
        }
        return new Color(color.getRed()  , color.getGreen(), color.getBlue(), 135);
    }

    /**
     * Returns an JButton, or null if the path was invalid.
     * 
     * @param buttonText
     */
    public static JButton createImageButton(Action action, String path,
            String description, String buttonText) {
        JButton result = new JButton(action);
        result.setText(buttonText);
        result.setIcon(createImageIcon(path, description));
        result.setHorizontalAlignment(SwingConstants.LEFT);
        result.setMargin(new Insets(2, 2, 2, 2));
        result.setVerticalTextPosition(AbstractButton.CENTER);
        result.setHorizontalTextPosition(AbstractButton.TRAILING);
        result.setToolTipText(description);
        return result;
    }

    /**
     * Returns an JButton, or null if the path was invalid.
     * 
     * @param buttonText
     */
    public static JButton createImageButton(String path, String description,
            String buttonText) {
        JButton result = new JButton(buttonText, createImageIcon(path,
                description));
        result.setHorizontalAlignment(SwingConstants.LEFT);
        result.setMargin(new Insets(2, 2, 2, 2));
        result.setVerticalTextPosition(AbstractButton.CENTER);
        result.setHorizontalTextPosition(AbstractButton.TRAILING);
        result.setToolTipText(description);
        return result;
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    public static ImageIcon createImageIcon(String path, String description) {
        java.net.URL imgURL = Utils.class.getResource(path);
        // URL imgURL = null;
        // try {
        // imgURL = new URL("file:" + path);
        // } catch (MalformedURLException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            return null;
        }
    }

}
