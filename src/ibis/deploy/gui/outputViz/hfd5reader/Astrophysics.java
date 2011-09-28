package ibis.deploy.gui.outputViz.hfd5reader;

import ibis.deploy.gui.outputViz.common.Vec3;
import ibis.deploy.gui.outputViz.common.Vec4;

public class Astrophysics {
	static double sigma = 5.67037321E-8;
	static double wien = 2.8977685E-3;
	
	static double solar_luminosity = 3.839E26;
	static double solar_radius = 6.955E8;
	static double earth_radius = 6371;
	
	static double parsec =  3.08568025E16;
	static int DISTANCE_FACTOR = 25;
	
	public static Vec3 toScreenCoord(double x, double y, double z) {
		float fx = (float) (DISTANCE_FACTOR * (x / parsec));
		float fy = (float) (DISTANCE_FACTOR * (y / parsec));
		float fz = (float) (DISTANCE_FACTOR * (z / parsec));
		
		return new Vec3(fx, fy, fz);
	}
	
	public static float starToScreenCoord(double size) {
		float fs = (float) (DISTANCE_FACTOR * (size / parsec));
		
		return fs;
	}
	
	public static float toScreenCoord(double parsecs) {
		float fx = (float) (DISTANCE_FACTOR * parsecs);
		
		return fx;
	}
	
	public static Vec4 toColor(double luminosity, double radius) {
		luminosity *= solar_luminosity;
		//radius /= parsec;
		double temperature = Math.pow((luminosity/(4 * Math.PI * (radius*radius) * sigma)), 0.25);
		
		//System.out.println(temperature);
			
		Vec4 color;
		
//		if (temperature <= 3500) {
//			color = new Vec4( 1f, 0f, 0f, 1f);
//		} else if (temperature > 3500 && temperature <= 4900) {
//			color = new Vec4( 1f,.5f, 0f, 1f);
//		} else if (temperature > 4900 && temperature <= 6000) {
//			color = new Vec4( 1f, 1f, 0f, 1f);
//		} else if (temperature > 6000 && temperature <= 7500) {
//			color = new Vec4( 1f, 1f,.5f, 1f);
//		} else if (temperature > 7500 && temperature <= 10000) {
//			color = new Vec4( 1f, 1f, 1f, 1f);
//		} else if (temperature > 10000 && temperature <= 28000) {
//			color = new Vec4(.5f, 1f, 1f, 1f);
//		} else {
//			color = new Vec4( 0f,.2f, 1f, 1f);
//		}
		
		if (temperature <= 3500) {														//M
			float intensity = (float) ((temperature - 2750)/(3500 - 2750) * 0.5);
			color = new Vec4( 0.5f+intensity, 0f, 0f, 1f);
		} else if (temperature > 3500 && temperature <= 4900) {							//K
			float intensity = (float) ((temperature - 3500)/(4900 - 3500) * 0.5);
			color = new Vec4( 1f,intensity, 0f, 1f);
		} else if (temperature > 4900 && temperature <= 6000) {							//G
			float intensity = (float) ((temperature - 4900)/(6000 - 4900) * 0.5);
			color = new Vec4( 1f, 0.5f+intensity, 0f, 1f);
		} else if (temperature > 6000 && temperature <= 7500) {							//F
			float intensity = (float) ((temperature - 6000)/(7500 - 6000) * 0.5);
			color = new Vec4( 1f, 1f,intensity, 1f);
		} else if (temperature > 7500 && temperature <= 10000) {						//A
			float intensity = (float) ((temperature - 7500)/(10000 - 7500) * 0.5);
			color = new Vec4( 1f-intensity, 1f, 0.5f+intensity, 1f);
		} else if (temperature > 10000 && temperature <= 28000) {						//B
			float intensity = (float) ((temperature - 10000)/(28000 - 10000) * 0.5);
			color = new Vec4(0.5f-intensity, 1f-intensity, 1f, 1f);
		} else {																		//O
			float intensity = (float) ((temperature - 28000)/(35000 - 28000) * 0.5);
			color = new Vec4(0.2f,0.75f-intensity, 1f, 1f);
		}
		
		
		
		//float r, g, b, SSS;
		//double wavelength = wien / temperature * 1E9;
		
				//System.out.println(luminosity + " W");
				//System.out.println(radius + " m");
				//System.out.println(wavelength + " nm");
				
				//int w = (int)wavelength;
		
		/*
		if (w >= 380 && w < 440) {	    	
			r = -(w - 440f) / (440f - 350f);
			g = 0f;
			b = 1f;
		} else if (w >= 440 && w < 490) {
			r = 0f;
			g = (w - 440f) / (490f - 440f);
			b = 1f;
		} else if (w >= 490 && w < 510) {
			r = 0f;
			g = 1f;
			b = -(w - 510f) / (510f - 490f);
		} else if (w >= 510 && w < 580) {
			r = (w - 510f) / (580f - 510f);
			g = 1f;
			b = 0f;
		} else if (w >= 580 && w < 645) {
			r = 1f;
			g = -(w - 645f) / (645f - 580f);
			b = 0f;
		} else if (w >= 645 && w <= 780) {
			r = 1f;
			g = 0f;
			b = 0f;
		} else {
			r = 1f;
			g = 1f;
			b = 1f;
		}

		if (w >= 380 && w < 420) {
			SSS = 0.3f + 0.7f*(w - 350f) / (420f - 350f);
		} else if (w >= 420f && w <= 700f) {
			SSS = 1f;
		} else if ( w > 700f && w <= 780f) {
			SSS = 0.3f + 0.7f*(780f - w) / (780f - 700f);
		} else {
			SSS = 1f;			
		}
		
		return new Vec4(SSS*r, SSS*g, SSS*b);
		*/
		
		return color;
	}
}
