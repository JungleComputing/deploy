package ibis.deploy.gui.editor;

import ibis.deploy.gui.misc.Utils;
import ibis.deploy.gui.worldmap.MapUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.jdesktop.swingx.JXMapKit;
import org.jdesktop.swingx.mapviewer.GeoPosition;

public class MapEditor extends ChangeableField implements
		PropertyChangeListener {

	private static final GeoPosition DEFAULT_GEO_POSITION = new GeoPosition(0,
			0);

	private LocationPickerDialog mapDialog = null;
	private final GeoPositionTextEditor latitudeEditor;
	private final GeoPositionTextEditor longitudeEditor;

	private double initialLatitude;
	private double initialLongitude;

	/**
	 * @param form
	 *            - parent panel
	 * @param text
	 *            - label text
	 * @param latitude
	 *            - initial value for latitude
	 * @param longitude
	 *            - initial value for longitude
	 */
	public MapEditor(final JPanel tabPanel, final JPanel form, String text,
			double latitude, double longitude) {
		this.tabPanel = tabPanel;
		initialLatitude = Utils.truncate(latitude);
		initialLongitude = Utils.truncate(longitude);

		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));

		// create latitude editor and limit input to the range -90 to 90
		latitudeEditor = new GeoPositionTextEditor(container, "Latitude: ",
				initialLatitude, true);
		latitudeEditor.setMaximum(new Double(90));
		latitudeEditor.setMinimum(new Double(-90));
		latitudeEditor.addPropertyChangeListener(this);

		container.add(Box.createRigidArea(new Dimension(0, Utils.gapHeight)));

		// create latitude editor and limit input to the range -180 to 180
		longitudeEditor = new GeoPositionTextEditor(container, "Longitude: ",
				initialLongitude, true);
		longitudeEditor.setMaximum(180);
		longitudeEditor.setMinimum(-180);
		longitudeEditor.addPropertyChangeListener(this);

		container.add(Box.createRigidArea(new Dimension(0, Utils.gapHeight)));

		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());

		// label used just for spacing
		final JLabel label = new JLabel("View on map: ", JLabel.TRAILING);
		label.setPreferredSize(new Dimension(Utils.defaultLabelWidth, label
				.getPreferredSize().height));
		buttonPanel.add(label, BorderLayout.WEST);

		final JButton mapButton = Utils.createImageButton("images/map.png",
				"View location on map", null);

		mapButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (mapDialog == null) {
					mapDialog = new LocationPickerDialog((JFrame) form
							.getRootPane().getParent(), true, latitudeEditor
							.getValue(), longitudeEditor.getValue());
				} else {
					mapDialog.display(true, latitudeEditor.getValue(),
							longitudeEditor.getValue());
				}
			}
		});

		final JPanel mapButtonPanel = new JPanel(new BorderLayout());
		mapButtonPanel.add(mapButton, BorderLayout.WEST);

		buttonPanel.add(mapButtonPanel, BorderLayout.CENTER);
		container.add(buttonPanel);
		form.add(container);

	}

	/**
	 * @return - latitude value contained by the corresponding editor
	 */
	public double getLatitude() {
		return (Double) latitudeEditor.getValue();
	}

	/**
	 * @return - longitude value contained by the corresponding editor
	 */
	public double getLongitude() {
		return (Double) longitudeEditor.getValue();
	}

	/**
	 * @param latitude
	 *            - new latitude value
	 */
	public void setLatitude(double latitude) {
		latitudeEditor.setValue(Utils.truncate(latitude));
	}

	/**
	 * @param longitude
	 *            - new longitude value
	 */
	public void setLongitude(double longitude) {
		longitudeEditor.setValue(Utils.truncate(longitude));
	}

	@Override
	public void refreshInitialValue() {
		initialLatitude = (Double) latitudeEditor.getValue();
		initialLongitude = (Double) longitudeEditor.getValue();
	}

	/**
	 * @return - true if the value in any of the editors is not the initial
	 *         value
	 */
	@Override
	public boolean hasChanged() {
		return !((Double) latitudeEditor.getValue() == (initialLatitude))
				|| !((Double) longitudeEditor.getValue() == initialLongitude);
	}

	/**
	 * PropertyChange handler for the two editors
	 */
	public void propertyChange(PropertyChangeEvent event) {
		informParent();
	}

	class LocationPickerDialog extends JDialog implements ActionListener,
			PropertyChangeListener {
		/**
		 * Dialog that allows the user to select locations directly on the map
		 * or by providing values in the editors. Values in the editors reflect
		 * on the map an vice-versa
		 */
		private static final long serialVersionUID = 5221726743512199272L;

		private JPanel mainPanel = null;
		private JButton updateButton = null;
		private JButton cancelButton = null;

		private GeoPositionTextEditor dialogLatitudeEditor;
		private GeoPositionTextEditor dialogLongitudeEditor;

		private final DialogMap mapKit;
		private GeoPosition position;
		private JFrame parentFrame;

		/**
		 * @param frame
		 *            - parent frame
		 * @param modal
		 *            - created dialog will be modal / non-modal
		 * @param latitude
		 *            - initial value for latitude
		 * @param longitude
		 *            - initial value for longitude
		 */
		public LocationPickerDialog(JFrame frame, boolean modal,
				Object latitude, Object longitude) {
			super(frame, modal);

			parentFrame = frame;
			setTitle("Select the desired location on the map");

			mainPanel = new JPanel();
			setContentPane(mainPanel);
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

			if (latitude == null && longitude == null) {
				position = DEFAULT_GEO_POSITION;
			} else {
				position = new GeoPosition(Double.parseDouble(latitude
						.toString()), Double.parseDouble(longitude.toString()));
			}

			mapKit = new DialogMap(position);
			mapKit.setBorder(new EmptyBorder(10, 10, 10, 10));
			mainPanel.add(mapKit);
			mainPanel.add(new JPanel());// spacer

			JPanel longLatPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

			// initialize latitide editor
			dialogLatitudeEditor = new GeoPositionTextEditor(longLatPanel,
					"Latitude:", position.getLatitude(), false);
			dialogLatitudeEditor.setMaximum(90);
			dialogLatitudeEditor.setMinimum(-90);
			dialogLatitudeEditor.addPropertyChangeListener(this);

			// initialize longitude editor
			dialogLongitudeEditor = new GeoPositionTextEditor(longLatPanel,
					"Longitude:", position.getLongitude(), false);
			dialogLongitudeEditor.setMaximum(new Double(180));
			dialogLongitudeEditor.setMinimum(new Double(-180));
			dialogLongitudeEditor.addPropertyChangeListener(this);

			mainPanel.add(longLatPanel);

			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			updateButton = new JButton("Update");
			updateButton.addActionListener(this);
			buttonPanel.add(updateButton);

			cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(this);
			buttonPanel.add(cancelButton);

			mainPanel.add(buttonPanel);

			mapKit.getMainMap().addMouseListener(new MouseListener() {

				/**
				 * when the location on the map is updated, the values of the
				 * editors are also updated
				 */
				public void mouseClicked(MouseEvent e) {
					GeoPosition oldPosition = mapKit.getMainMap()
							.getCenterPosition();
					GeoPosition clusterLocation = mapKit.getMainMap()
							.convertPointToGeoPosition(e.getPoint());
					mapKit.getMainMap().setAddressLocation(clusterLocation);

					dialogLatitudeEditor.setValue(Utils
							.truncate(clusterLocation.getLatitude()));
					dialogLongitudeEditor.setValue(Utils
							.truncate(clusterLocation.getLongitude()));

					mapKit.getMainMap().setCenterPosition(oldPosition);
					repaint();
				}

				public void mouseEntered(MouseEvent e) {
				}

				public void mouseExited(MouseEvent e) {
				}

				public void mousePressed(MouseEvent e) {
				}

				public void mouseReleased(MouseEvent e) {
				}

			});

			mapKit.adjustMapSize();
			setResizable(false);
			pack();
			setLocationRelativeTo(parentFrame);
			setVisible(true);
		}

		/**
		 * displays the map dialog
		 * 
		 * @param visible
		 *            - whether the dialog should be visible or not
		 * @param latitude
		 *            - latitude value
		 * @param longitude
		 *            - longitude value
		 */
		public void display(boolean visible, Object latitude, Object longitude) {
			GeoPosition position = new GeoPosition(Double.parseDouble(latitude
					.toString()), Double.parseDouble(longitude.toString()));
			mapKit.getMainMap().setAddressLocation(position);

			// set zoom level to the maximum level
			mapKit.setZoom(mapKit.getMainMap().getTileFactory().getInfo()
					.getMaximumZoomLevel());

			// initialize editors
			dialogLatitudeEditor.setValue(latitude);
			dialogLongitudeEditor.setValue(longitude);

			// resize map and dialog together with it
			mapKit.adjustMapSize();
			pack();
			setLocationRelativeTo(parentFrame); // recenter dialog
			setVisible(visible);
		}

		/**
		 * Listener for the Save / Cancel buttons. On save, transfer the values
		 * to the MapEditor editors and close dialog. On cancel, just close
		 * dialog.
		 */
		public void actionPerformed(ActionEvent e) {
			if (updateButton == e.getSource()) {
				latitudeEditor.setValue(dialogLatitudeEditor.getValue());
				longitudeEditor.setValue(dialogLongitudeEditor.getValue());
			}

			setVisible(false);
		}

		/**
		 * Property change listener for the dialog editors. Changes in latitude
		 * / longitude will also be visible on the map
		 */
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getSource().equals(dialogLatitudeEditor.getTextField())) {
				position = new GeoPosition((Double) dialogLatitudeEditor
						.getValue(), position.getLongitude());
			} else {
				position = new GeoPosition(position.getLatitude(),
						(Double) dialogLongitudeEditor.getValue());
			}
			mapKit.setAddressLocation(position);
		}

	}

	class DialogMap extends JXMapKit {
		/**
    	 * 
    	 */
		private static final long serialVersionUID = -4395559209488415471L;

		private final int maxMapWidth = 800;
		private final int maxMapHeight = 600;

		public DialogMap(GeoPosition position) {
			super();

			MapUtilities.register(this);
			setTileFactory(MapUtilities.getDefaultTileFactory());
			setMiniMapVisible(false);

			getMainMap().setZoom(
					getMainMap().getTileFactory().getInfo()
							.getMaximumZoomLevel());
			getMainMap().setCenterPosition(MapUtilities.INITIAL_MAP_CENTER);
			getMainMap().setAddressLocation(position);
			getMainMap().setHorizontalWrapped(false);
		}

		/**
		 * Based on the current zoom level, it sets the maximum size for the
		 * map.
		 */
		public void adjustMapSize() {
			int zoom = getMainMap().getZoom();
			// calculate actual map size
			Dimension mapSize = getMainMap().getTileFactory().getMapSize(zoom);
			int mapWidth = (int) mapSize.getWidth()
					* getMainMap().getTileFactory().getTileSize(zoom);
			int mapHeight = (int) mapSize.getHeight()
					* getMainMap().getTileFactory().getTileSize(zoom);

			// limit map size in the dialog
			if (mapWidth > maxMapWidth)
				mapWidth = maxMapWidth;
			if (mapHeight > maxMapHeight)
				mapHeight = maxMapHeight;

			Dimension newSize = new Dimension(mapWidth + 20, mapHeight + 20);
			setPreferredSize(newSize);
			revalidate(); // revalidate to force the layout manager to recompute
			// sizes

			// the map doesn't automatically center itself
			getMainMap().setCenter(getMainMap().getCenter());
		}

		@Override
		public void paint(Graphics g) {
			adjustMapSize();
			super.paint(g);
		}
	}
}
