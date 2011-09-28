package ibis.deploy.monitoring.visualization.gridvision.common;

public class Color4 extends Vec4{
	public static Color4 black 	= new Color4(0.0f, 0.0f, 0.0f, 1.0f);
	public static Color4 red 	= new Color4(1.0f, 0.0f, 0.0f, 1.0f);
	public static Color4 yellow = new Color4(1.0f, 1.0f, 0.0f, 1.0f);
	public static Color4 green 	= new Color4(0.0f, 1.0f, 0.0f, 1.0f);
	public static Color4 blue 	= new Color4(0.0f, 0.0f, 1.0f, 1.0f);	
	public static Color4 magenta= new Color4(1.0f, 0.0f, 1.0f, 1.0f);
	public static Color4 white 	= new Color4(1.0f, 1.0f, 1.0f, 1.0f);
	public static Color4 cyan 	= new Color4(0.0f, 1.0f, 1.0f, 1.0f);
	
	public static Color4 t_black 	= new Color4(0.0f, 0.0f, 0.0f, 0.2f);
	public static Color4 t_red 		= new Color4(1.0f, 0.0f, 0.0f, 0.2f);
	public static Color4 t_yellow 	= new Color4(1.0f, 1.0f, 0.0f, 0.2f);
	public static Color4 t_green 	= new Color4(0.0f, 1.0f, 0.0f, 0.2f);
	public static Color4 t_blue 	= new Color4(0.0f, 0.0f, 1.0f, 0.2f);	
	public static Color4 t_magenta	= new Color4(1.0f, 0.0f, 1.0f, 0.2f);
	public static Color4 t_white 	= new Color4(1.0f, 1.0f, 1.0f, 0.2f);
	public static Color4 t_cyan 	= new Color4(0.0f, 1.0f, 1.0f, 0.2f);
	
	public Color4() {
		super();
	}
	
	public Color4(Vec4 v) {
		super(v);
	}
	
	public Color4(float x, float y, float z, float w) {
		super(x, y, z, w);
	}
}
