package ibis.deploy.gui;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

public class GUIUtils {

    /** Returns an JLabel, or null if the path was invalid. */
    protected static JLabel createImageLabel(String path, String description) {
        JLabel result = new JLabel(createImageIcon(path, description));
        result.setToolTipText(description);
        return result;
    }

    /**
     * Returns an JButton, or null if the path was invalid.
     * 
     * @param buttonText
     */
    protected static JButton createImageButton(String path, String description,
            String buttonText) {
        JButton result = new JButton(buttonText, createImageIcon(path,
                description));
        result.setVerticalTextPosition(AbstractButton.CENTER);
        result.setHorizontalTextPosition(AbstractButton.TRAILING);
        result.setToolTipText(description);
        return result;
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path, String description) {
        // java.net.URL imgURL = GUIUtils.class.getResource(path);
        URL imgURL = null;
        try {
            imgURL = new URL("file:" + path);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            return null;
        }
    }

}