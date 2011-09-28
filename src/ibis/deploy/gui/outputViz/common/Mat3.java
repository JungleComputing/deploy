package ibis.deploy.gui.outputViz.common;

import java.nio.FloatBuffer;
import java.util.Arrays;

import com.jogamp.common.nio.Buffers;

public class Mat3 {
	public float[] m;
	private FloatBuffer buf;

    public Mat3() {
    	identity();
    }
    
    public Mat3(float in) {
    	m = new float[9];
    	buf = FloatBuffer.wrap(m);
        Arrays.fill(m, in);
    }
    
    private void identity() {
    	m = new float[9];
    	buf = FloatBuffer.wrap(m);
        Arrays.fill(m, 0f);
        m[0] = m[4] = m[ 8] = 1.0f;
    }

    public Mat3(Vec3 v0, Vec3 v1, Vec3 v2, Vec3 v3) {
    	identity();
    	buf.rewind();
    	buf.put(v0.asBuffer());
    	buf.put(v1.asBuffer());
    	buf.put(v2.asBuffer());
    	buf.put(v3.asBuffer());
    }
    
    public Mat3(float m00, float m01, float m02, 
	    		float m10, float m11, float m12,
	    		float m20, float m21, float m22) {
    	identity();
    	m[ 0] = m00; m[ 1] = m01; m[ 2] = m02;
    	m[ 3] = m10; m[ 4] = m11; m[ 5] = m12;
    	m[ 6] = m20; m[ 7] = m21; m[ 8] = m22;    	
    }
    
    public Mat3(Mat3 n) {    	
    	identity();
    	buf.rewind();
        buf.put(Buffers.copyFloatBuffer(n.asBuffer()));
    }
    
    public Mat3 mul(Mat3 n) {
    	Mat3 a = new Mat3(0);
    	   	
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 3; ++j) {
				for (int k = 0; k < 3; ++k) {
					a.m[i * 3 + j] += m[i * 3 + k] * n.m[k * 3 + j];
				}
			}
		}
		
		return a;
    }
    
    public Vec3 mul(Vec3 v) {
    	return new Vec3(m[0 * 3 + 0]*v.v[0] + m[0 * 3 + 1]*v.v[1] + m[0 * 3 + 2]*v.v[2],
   		     			m[1 * 3 + 0]*v.v[0] + m[1 * 3 + 1]*v.v[1] + m[1 * 3 + 2]*v.v[2],
			   		    m[2 * 3 + 0]*v.v[0] + m[2 * 3 + 1]*v.v[1] + m[2 * 3 + 2]*v.v[2]);		
    }
    
    public Mat3 mul(Number n) {
    	float fn = n.floatValue();
		for (int i = 0; i < 9; ++i) {
			m[i] *= fn;
		}
		
		return this;
    }
    
    public Mat3 add(Mat3 n) {
		for (int i = 0; i < 9; ++i) {
			m[i] += n.m[i];
		}
		
		return this;
    }
    
    public Mat3 sub(Mat3 n) {
		for (int i = 0; i < 9; ++i) {
			m[i] -= n.m[i];
		}
		
		return this;
    }
    
    public Mat3 div(Number n) {
    	float fn = 1f / n.floatValue();
    	
		for (int i = 0; i < 9; ++i) {
			m[i] *= fn;
		}
		
		return this;
    }
	
	public float[] asArray() {
        return m;
    }
    
    public FloatBuffer asBuffer() {
    	buf.rewind();
    	return buf;
    }
    
    public float get(int i, int j) {
        return m[i * 3 + j];
    }
    
    public float get(int i) {
        return m[i];
    }
    
    public void set(int i, int j, float f) {        
        m[i * 3 + j] = f;
    }
    
    public void set(int i, float f) {        
        m[i] = f;
    }
    
    public Mat3 clone() {
    	return new Mat3(this);
    }
    
    public void printReadable() {		
		for (int i=0; i<9; i++) {			
			if (i % 3 == 0) {
				System.out.println();
			}
			System.out.print(m[i]+" ");
		}
		System.out.println();
		System.out.println();
	}
}
