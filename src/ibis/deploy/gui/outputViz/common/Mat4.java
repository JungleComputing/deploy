package ibis.deploy.gui.outputViz.common;

import java.nio.FloatBuffer;
import java.util.Arrays;

import com.jogamp.common.nio.Buffers;

public class Mat4 {
	public float[] m;
	private FloatBuffer buf;

    public Mat4() {
    	identity();
    }
    
    public Mat4(float in) {
    	m = new float[16];
    	buf = FloatBuffer.wrap(m);
        Arrays.fill(m, in);
    }
    
    private void identity() {
    	m = new float[16];
    	buf = FloatBuffer.wrap(m);
        Arrays.fill(m, 0f);
        m[0] = m[5] = m[10] = m[15] = 1.0f;
    }

    public Mat4(Vec4 v0, Vec4 v1, Vec4 v2, Vec4 v3) {
    	identity();
    	buf.rewind();
    	buf.put(v0.asBuffer());
    	buf.put(v1.asBuffer());
    	buf.put(v2.asBuffer());
    	buf.put(v3.asBuffer());
    }
    
    public Mat4(float m00, float m01, float m02, float m03, 
	    		float m10, float m11, float m12, float m13,
	    		float m20, float m21, float m22, float m23,
	    		float m30, float m31, float m32, float m33) {
    	identity();
    	m[ 0] = m00; m[ 1] = m01; m[ 2] = m02; m[ 3] = m03;
    	m[ 4] = m10; m[ 5] = m11; m[ 6] = m12; m[ 7] = m13;
    	m[ 8] = m20; m[ 9] = m21; m[10] = m22; m[11] = m23;
    	m[12] = m30; m[13] = m31; m[14] = m32; m[15] = m33;    	
    }
    
    public Mat4(Mat4 n) {    	
    	identity();
    	buf.rewind();
        buf.put(Buffers.copyFloatBuffer(n.asBuffer()));
    }
    
    public Mat4 mul(Mat4 n) {
    	Mat4 a = new Mat4(0);
    	   	
		for (int i = 0; i < 4; ++i) {
			for (int j = 0; j < 4; ++j) {
				for (int k = 0; k < 4; ++k) {
					a.m[i * 4 + j] += m[i * 4 + k] * n.m[k * 4 + j];
				}
			}
		}
		
		return a;
    }
    
    public Vec4 mul(Vec4 v) {
    	return new Vec4(m[0 * 4 + 0]*v.v[0] + m[0 * 4 + 1]*v.v[1] + m[0 * 4 + 2]*v.v[2] + m[0 * 4 + 3]*v.v[3],
   		     			m[1 * 4 + 0]*v.v[0] + m[1 * 4 + 1]*v.v[1] + m[1 * 4 + 2]*v.v[2] + m[1 * 4 + 3]*v.v[3],
			   		    m[2 * 4 + 0]*v.v[0] + m[2 * 4 + 1]*v.v[1] + m[2 * 4 + 2]*v.v[2] + m[2 * 4 + 3]*v.v[3],
			   		    m[3 * 4 + 0]*v.v[0] + m[3 * 4 + 1]*v.v[1] + m[3 * 4 + 2]*v.v[2] + m[3 * 4 + 3]*v.v[3]);		
    }
    
    public Mat4 mul(Number n) {
    	float fn = n.floatValue();
		for (int i = 0; i < 16; ++i) {
			m[i] *= fn;
		}
		
		return this;
    }
    
    public Mat4 add(Mat4 n) {
		for (int i = 0; i < 16; ++i) {
			m[i] += n.m[i];
		}
		
		return this;
    }
    
    public Mat4 sub(Mat4 n) {
		for (int i = 0; i < 16; ++i) {
			m[i] -= n.m[i];
		}
		
		return this;
    }
    
    public Mat4 div(Number n) {
    	float fn = 1f / n.floatValue();
    	
		for (int i = 0; i < 16; ++i) {
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
        return m[i * 4 + j];
    }
    
    public float get(int i) {
        return m[i];
    }
    
    public void set(int i, int j, float f) {        
        m[i * 4 + j] = f;
    }
    
    public void set(int i, float f) {        
        m[i] = f;
    }
    
    public Mat4 clone() {
    	return new Mat4(this);
    }
    
    public void printReadable() {		
		for (int i=0; i<16; i++) {			
			if (i % 4 == 0) {
				System.out.println();
			}
			System.out.print(m[i]+" ");
		}
		System.out.println();
		System.out.println();
	}
}
