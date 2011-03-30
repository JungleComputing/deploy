package ibis.deploy.vizFramework.globeViz.viz;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.FrameFactory;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.PatternFactory;
import gov.nasa.worldwind.render.Polyline;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.util.ArrayList;

import javax.swing.JFrame;


public class GlobeVisualization {
	private GlobeAnnotation tooltipAnnotation;
	private CircleAnnotation lastSelectedDot;
	private WorldWindowGLCanvas worldWindCanvas;
	private RenderableLayer annotationLayer;

	public GlobeVisualization() {

		// create a WorldWind main object
		worldWindCanvas = new WorldWindowGLCanvas();
		worldWindCanvas.setModel(new BasicModel());

		// build Java swing interface
		JFrame frame = new JFrame("World Wind");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(worldWindCanvas);
		frame.setSize(800, 600);
		frame.setVisible(true);

		annotationLayer = buildDotLayer();
		worldWindCanvas.getModel().getLayers().add(annotationLayer);

		createTooltip();

		// create a listener for displaying the tooltip
		worldWindCanvas.addSelectListener(new SelectListener() {
			public void selected(SelectEvent event) {
				if (event.getEventAction().equals(SelectEvent.ROLLOVER))
					highlight(event.getTopObject());
			}
		});

		// //temporarily disable some layers for debugging - TODO - remove
		// for (Layer layer : worldWindCanvas.getModel().getLayers()) {
		// if (layer.getName()
		// .equals(
		// "NASA Blue Marble Image")
		// || layer.getName().equals(
		// "Blue Marble (WMS) 2004")) {
		// layer.setEnabled(false);
		// }
		// }

	}

	// tooltip initialization
	private void createTooltip() {

		// Initialize tooltip annotation
		tooltipAnnotation = new GlobeAnnotation("", Position.fromDegrees(0, 0,
				0));

		// Create attributes for the tooltip
		AnnotationAttributes tooltipAttributes = new AnnotationAttributes();
		tooltipAttributes.setCornerRadius(10);
		tooltipAttributes.setInsets(new Insets(8, 8, 8, 8));
		tooltipAttributes.setBackgroundColor(new Color(0f, 0f, 0f, .5f));
		tooltipAttributes.setDrawOffset(new Point(25, 25));
		tooltipAttributes.setDistanceMinScale(.5);
		tooltipAttributes.setDistanceMaxScale(2);
		tooltipAttributes.setDistanceMinOpacity(.5);
		tooltipAttributes.setLeaderGapWidth(14);
		tooltipAttributes.setDrawOffset(new Point(20, 40));
		tooltipAttributes.setFont(Font.decode("Arial-PLAIN-12"));
		tooltipAttributes.setTextColor(Color.BLACK);

		tooltipAnnotation = new GlobeAnnotation("", Position.fromDegrees(10,
				100, 0), tooltipAttributes);
		tooltipAnnotation.getAttributes().setImageSource(
				PatternFactory.createPattern(PatternFactory.GRADIENT_VLINEAR,
						new Dimension(32, 128), 1f, Color.WHITE, new Color(0f,
								0f, 0f, 0f))); // White to transparent gradient
		tooltipAnnotation.getAttributes().setSize(new Dimension(150, 0));
		tooltipAnnotation.getAttributes().setVisible(false);
		tooltipAnnotation.setAlwaysOnTop(true);

		annotationLayer.addRenderable(tooltipAnnotation);
	}

	// displays the name of a location on mouse over
	private void highlight(Object o) {
		if (lastSelectedDot == o)
			return; // same thing selected

		if (lastSelectedDot != null) {
			lastSelectedDot.getAttributes().setHighlighted(false);
			lastSelectedDot = null;
			tooltipAnnotation.getAttributes().setVisible(false);
		}

		if (o != null && o instanceof CircleAnnotation) {
			lastSelectedDot = (CircleAnnotation) o;
			lastSelectedDot.getAttributes().setHighlighted(true);
			tooltipAnnotation.setText("<p>" + lastSelectedDot.getLocationName()
					+ "</p>");
			tooltipAnnotation.setPosition(lastSelectedDot.getPosition());
			tooltipAnnotation.getAttributes().setVisible(true);
			worldWindCanvas.repaint();
		}
	}

	public RenderableLayer buildDotLayer() {
		Position pos;
		CircleAnnotation annotation;
		int i;
		RenderableLayer layer = new RenderableLayer();
		layer.setName("Locations");

		AnnotationAttributes dotAttributes = new AnnotationAttributes();
		dotAttributes.setLeader(FrameFactory.LEADER_NONE);
		dotAttributes.setDrawOffset(new Point(0, -16));
		dotAttributes.setSize(new Dimension(15, 15));
		dotAttributes.setBorderWidth(0);
		dotAttributes.setCornerRadius(0);
		dotAttributes.setBackgroundColor(new Color(0, 0, 0, 0));

		ArrayList<Position> positionList = generatePositionList(UIConstants.NPOSITIONS);

		generateRandomConnections(positionList, layer);

		for (i = 0; i < UIConstants.NPOSITIONS; i++) {
			pos = positionList.get(i);

			annotation = new CircleAnnotation(pos, dotAttributes, "Location "
					+ i);
			annotation.getAttributes().setImageSource(
					UIConstants.LOCATIONS_SHAPE_LIST[i % 7]);
			annotation.getAttributes().setTextColor(
					UIConstants.LOCATION_COLOR_LIST[i % 7]);

			layer.addRenderable(annotation);
		}

		return layer;
	}

	// generates a random list of connections between the locations in the
	// positionList
	private void generateRandomConnections(ArrayList<Position> positionList,
			RenderableLayer layer) {

		int i, j;

		Position pos1, pos2;

		for (i = 0; i < positionList.size(); i++) {
			for (j = i + 1; j < positionList.size(); j++) {
				if (Math.random() > 0.5) {
					pos1 = positionList.get(i);
					pos2 = positionList.get(j);
					layer.addRenderable(createArcBetween(pos1, pos2));
				}
			}
		}
	}

	// computes a polyline between the two locations
	private Polyline createArcBetween(Position pos1, Position pos2) {
		Polyline polyline;
		ArrayList<Position> polylineList = new ArrayList<Position>();

		// add the control points to the list
		polylineList.addAll(doTheSplits(pos1, pos2, 3, true));

		// add the points of the BSpline created using the control points.

		polylineList = BSpline.computePolyline(worldWindCanvas.getModel()
				.getGlobe(), polylineList);

		// The BSpline doesn't pass through the control points, so to force the
		// polyline to pass through the two locations we have to add them
		// separately to the list
		polylineList.add(0, pos1);
		polylineList.add(pos2);

		polyline = new Polyline(polylineList);
		polyline.setColor(new Color(0, 255, 0, 150));
		polyline.setLineWidth(3.0);

		return polyline;
	}

	// calculates the interpolation point for pos1 and pos2
	private Position getMidPoint(Position pos1, Position pos2,
			boolean adjustHeight) {

		Position pos3 = Position.interpolateGreatCircle(0.5, pos1, pos2);
		if (adjustHeight) {
			double newHeight = LatLon.greatCircleDistance(pos1, pos2).degrees
					* UIConstants.ARC_HEIGHT;
			pos3 = new Position(pos3.latitude, pos3.longitude, newHeight);
		}

		return pos3;
	}

	private ArrayList<Position> doTheSplits(Position pos1, Position pos2,
			int depth, boolean adjustHeight) {

		ArrayList<Position> l1, l2, list = new ArrayList<Position>();

		if (depth == 0) {
			list.add(pos1);
			list.add(pos2);
			return list;
		}

		Position pos3 = getMidPoint(pos1, pos2, adjustHeight);

		l1 = doTheSplits(pos1, pos3, depth - 1, false);
		l2 = doTheSplits(pos3, pos2, depth - 1, false);

		l1.remove(l1.size() - 1); // remove the last element, otherwise we'll
		// have the midpoint two times in the final
		// list
		list.addAll(l1);
		list.addAll(l2);

		return list;
	}

	// generates a list with nPoints random locations
	private ArrayList<Position> generatePositionList(int nPoints) {
		ArrayList<Position> positionList = new ArrayList<Position>();
		for (int i = 0; i < nPoints; i++) {
			positionList.add(generateRandomPosition());
		}

		return positionList;
	}

	// generates one random geographic location
	private Position generateRandomPosition() {
		double lat, longit;
		lat = (Math.random() * 1000) % 180 - 90;
		longit = (Math.random() * 1000) % 360 - 180;
		LatLon pos1 = LatLon.fromDegrees(lat, longit);

		return new Position(pos1, 0);
	}
}
