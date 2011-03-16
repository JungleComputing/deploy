package ibis.deploy.monitoring.visualization.gridvision;

public class FloatMatrixMath {
	public static float max(float[] input) {
		float result = -Float.MAX_VALUE;
		
		for (float in : input) {
			if (in > result) result = in;
		}
		
		return result;	
	}
	
	public static float min(float[] input) {
		float result = Float.MAX_VALUE;
		
		for (float in : input) {
			if (in < result) result = in;
		}
		
		return result;	
	}
	
	public static float[] mul(Number[] m1, Number[] m2) {		
		if (m1.length != m2.length) {
			System.out.println("tried to multiply 2 matrices of different lengths.");
			System.exit(0);
		}
		
		float[] result = new float[m1.length];
		for (int i=0;i<m1.length;i++) {
			result[i] = (Float)m1[i]*(Float)m2[i];
		}
		
		return result;	
	}
	
	public static float[] mul(float[] m1, float[] m2) {		
		if (m1.length != m2.length) {
			System.out.println("tried to multiply 2 matrices of different lengths.");
			System.exit(0);
		}
		
		float[] result = new float[m1.length];
		for (int i=0;i<m1.length;i++) {
			result[i] = m1[i]*m2[i];
		}
		
		return result;	
	}
	
	public static float[] mul(float[] m1, float num) {
		float[] result = new float[m1.length];
		for (int i=0;i<m1.length;i++) {
			result[i] = m1[i]*num;
		}
		
		return result;	
	}
	
	public static float[] div(float[] m1, float[] m2) {		
		if (m1.length != m2.length) {
			System.out.println("tried to divide 2 matrices of different lengths.");
			System.exit(0);
		}
		
		float[] result = new float[m1.length];
		for (int i=0;i<m1.length;i++) {
			result[i] = m1[i]/m2[i];
		}
		
		return result;	
	}
	
	public static float[] div(float[] m1, float num) {
		float[] result = new float[m1.length];
		for (int i=0;i<m1.length;i++) {
			result[i] = m1[i]/num;
		}
		
		return result;	
	}
	
	public static float[] add(float[] m1, float[] m2) {		
		if (m1.length != m2.length) {
			System.out.println("tried to add 2 matrices of different lengths.");
			System.exit(0);
		}
		
		float[] result = new float[m1.length];
		for (int i=0;i<m1.length;i++) {
			result[i] = m1[i]+m2[i];
		}
		
		return result;	
	}
	
	public static float[] add(float[] m1, float num) {
		float[] result = new float[m1.length];
		for (int i=0;i<m1.length;i++) {
			result[i] = m1[i]+num;
		}
		
		return result;	
	}
	
	public static float[] sub(float[] m1, float[] m2) {		
		if (m1.length != m2.length) {
			System.out.println("tried to substract 2 matrices of different lengths.");
			System.exit(0);
		}
		
		float[] result = new float[m1.length];
		for (int i=0;i<m1.length;i++) {
			result[i] = m1[i]-m2[i];
		}
		
		return result;	
	}
	
	public static float[] sub(float[] m1, float num) {
		float[] result = new float[m1.length];
		for (int i=0;i<m1.length;i++) {
			result[i] = m1[i]-num;
		}
		
		return result;	
	}
}
