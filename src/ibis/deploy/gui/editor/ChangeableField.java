package ibis.deploy.gui.editor;

import javax.swing.JPanel;

import ibis.deploy.gui.applications.ApplicationEditorTabPanel;
import ibis.deploy.gui.resources.ResourceEditorTabPanel;

public class ChangeableField {

    protected JPanel tabPanel;

    /**
     * @return - true when the content of the field has changed. To be
     *         overridden in the subclasses
     */
    public boolean hasChanged() {
        return false;
    }

    /**
     * Updates the initial value of the field. Useful in the apply / discard
     * mechanism implemented in the subclasses
     */
    public void refreshInitialValue() {

    }

    /**
     * Informs the parent panel to check for changes
     */
    protected void informParent() {
        // the editor's configuration has changed, the parent panel must check
        // for changes and update itself accordingly
        if (tabPanel instanceof ResourceEditorTabPanel)
            ((ResourceEditorTabPanel) tabPanel).checkForChanges();

        if (tabPanel instanceof ApplicationEditorTabPanel)
            ((ApplicationEditorTabPanel) tabPanel).checkForChanges();
    }
}
