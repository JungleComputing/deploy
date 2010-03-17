package ibis.deploy.gui.editor;

public interface ChangeableField 
{
	public boolean hasChanged();
	public void refreshInitialValue();
}
