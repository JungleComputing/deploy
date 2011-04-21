package ibis.deploy.monitoring.visualization.gridvision;

import javax.media.opengl.GL2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.deploy.monitoring.collection.Metric;
import ibis.deploy.monitoring.collection.Metric.MetricModifier;
import ibis.deploy.monitoring.collection.MetricDescription.MetricOutput;
import ibis.deploy.monitoring.collection.exceptions.OutputUnavailableException;

public class JGMetric extends JGVisualAbstract implements JGVisual {
	private static final Logger logger = LoggerFactory.getLogger("ibis.deploy.monitoring.visualization.gridvision.JGMetric");
	private final float ALPHA = 0.1f;
	
	private enum MetricDisplay { TRANSPARANCY_ENABLED, SOLIDS_ONLY };
	
	private Float[] color;
	
	private Metric metric;
	private float currentValue;
	private MetricOutput currentOutputMethod = MetricOutput.PERCENT;
	private MetricModifier myMod;
	
	private int glName;
	private int[] barPointer, selectedBarPointer, barTransparencyPointer, selectedBarTransparencyPointer;
	private int[] tubePointer, selectedTubePointer, tubeTransparencyPointer, selectedTubeTransparencyPointer;
	private MetricDisplay currentDisplay;
	
	JGMetric(JungleGoggles goggles, JGVisual parent, Metric metric, MetricModifier mod) {		
		super(goggles, parent);
		
		this.goggles = goggles;	
		this.metric = metric;
		this.myMod = mod;
		this.color = metric.getDescription().getColor();
		
		update();
		
		//getting these here is just an optimalization
		barPointer = goggles.getDisplayListPointer(DisplayListBuilder.DisplayList.BAR);
		selectedBarPointer = goggles.getDisplayListPointer(DisplayListBuilder.DisplayList.SELECTED_BAR);
		barTransparencyPointer = goggles.getDisplayListPointer(DisplayListBuilder.DisplayList.BAR_TRANSPARENCY);	
		selectedBarTransparencyPointer = goggles.getDisplayListPointer(DisplayListBuilder.DisplayList.SELECTED_BAR_TRANSPARENCY);
		
		tubePointer = goggles.getDisplayListPointer(DisplayListBuilder.DisplayList.TUBE);
		selectedTubePointer = goggles.getDisplayListPointer(DisplayListBuilder.DisplayList.SELECTED_TUBE);
		tubeTransparencyPointer = goggles.getDisplayListPointer(DisplayListBuilder.DisplayList.TUBE_TRANSPARENCY);	
		selectedTubeTransparencyPointer = goggles.getDisplayListPointer(DisplayListBuilder.DisplayList.SELECTED_TUBE_TRANSPARENCY);
		
		currentDisplay = MetricDisplay.TRANSPARANCY_ENABLED;
		mShape = MetricShape.BAR;
		
		width = 0.25f;
		height = 1f;
		
		radius = height;
		
		
		glName = goggles.registerGLName(parent, this);
	}
	
	public void drawSolids(GL2 gl, int renderMode) {
		if (renderMode == GL2.GL_SELECT) { gl.glLoadName(glName); }
		if (mShape == MetricShape.BAR) {
			if (goggles.currentlySelected(glName)) {
				drawList(gl, selectedBarPointer, currentValue, 1.0f);
			} else {
				drawList(gl, barPointer, currentValue, 1.0f);
			}
		} else if (mShape == MetricShape.TUBE) {
			if (goggles.currentlySelected(glName)) {
				drawList(gl, selectedTubePointer, currentValue, 1.0f);
			} else {
				drawList(gl, tubePointer, currentValue, 1.0f);
			}
		}		
	}
	
	public void drawTransparents(GL2 gl, int renderMode) {
		if (currentDisplay == MetricDisplay.TRANSPARANCY_ENABLED) {
			if (renderMode == GL2.GL_SELECT) { gl.glLoadName(glName); }
			if (mShape == MetricShape.BAR) {
				if (goggles.currentlySelected(glName)) {
					drawList(gl, selectedBarTransparencyPointer, currentValue, ALPHA);
				} else {
					drawList(gl, barTransparencyPointer, currentValue, ALPHA);
				}
			} else if (mShape == MetricShape.TUBE) {
				if (goggles.currentlySelected(glName)) {		
					drawList(gl, selectedTubeTransparencyPointer, currentValue, ALPHA);
				} else {
					drawList(gl, tubeTransparencyPointer, currentValue, ALPHA);
				}
			}		
		}
	}
	
	public void update() {
		try {			
			currentValue = (Float) metric.getValue(myMod, currentOutputMethod);
		} catch (OutputUnavailableException e) {
			logger.debug("OutputUnavailableException caught by visual metric for "+metric.getDescription().getName());
		}
	}
	
	protected void drawList(GL2 gl, int[] pointer, float length, float alpha) {
		//Save the current modelview matrix
		gl.glPushMatrix();
		
		//Translate to the desired coordinates and rotate if desired
		gl.glTranslatef(coordinates[0], coordinates[1], coordinates[2]);
		gl.glRotatef(rotation[0], 1.0f, 0.0f, 0.0f);
		gl.glRotatef(rotation[1], 0.0f, 1.0f, 0.0f);
		gl.glRotatef(rotation[2], 0.0f, 0.0f, 1.0f);		
				
		int whichBar = (int) Math.floor(length*pointer.length);
		if (length >= 0.95f) {
			whichBar = (pointer.length)-1;
		}

		gl.glColor4f(color[0], color[1], color[2], alpha);
		
		gl.glCallList(pointer[whichBar]);		
		
		//Restore the old modelview matrix
		gl.glPopMatrix();
	}	
}
