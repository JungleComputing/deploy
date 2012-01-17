package ibis.amuse.visualization.openglCommon.math;

import java.util.Arrays;

import com.jogamp.common.nio.Buffers;

public class Mat3 extends Matrix {
	/**
     * Creates a new 3x3 identity matrix.
     */
    public Mat3() {
    	super(9);
    	identity();
    }
    
    private void identity() {
        Arrays.fill(m, 0f);
        m[0] = m[4] = m[ 8] = 1.0f;
    }
    
    /**
     * Creates a new 3x3 matrix with all slots filled with the parameter.
     * @param in
     * 			The value to be put in all matrix fields.
     */
    public Mat3(float in) {
    	super(9);
        Arrays.fill(m, in);
    }

    /**
     * Creates a new 3x3 matrix, using the 3 vectors in order as filling.
     * @param v0
     * 			The first row of the matrix.
     * @param v1
     * 			The second row of the matrix.
     * @param v2 
     * 			The third row of the matrix.
     */
    public Mat3(Vec3 v0, Vec3 v1, Vec3 v2, Vec3 v3) {
    	super(9);
    	buf.put(v0.asBuffer());
    	buf.put(v1.asBuffer());
    	buf.put(v2.asBuffer());
    	buf.put(v3.asBuffer());
    }
    
    /**
     * Creates a new 3x3 matrix using the 9 parameters row-wise as filling.
     * @param m00
     * 			The parameter on position 0x0.
     * @param m01
     * 			The parameter on position 0x1.
     * @param m02
     * 			The parameter on position 0x2.
     * @param m10
     * 			The parameter on position 1x0.
     * @param m11
     * 			The parameter on position 1x1.
     * @param m12
     * 			The parameter on position 1x2.
     * @param m20
     * 			The parameter on position 2x0.
     * @param m21
     * 			The parameter on position 2x1.
     * @param m22
     * 			The parameter on position 2x2.
     */
    public Mat3(float m00, float m01, float m02, 
	    		float m10, float m11, float m12,
	    		float m20, float m21, float m22) {
    	super(9);
    	m[ 0] = m00; m[ 1] = m01; m[ 2] = m02;
    	m[ 3] = m10; m[ 4] = m11; m[ 5] = m12;
    	m[ 6] = m20; m[ 7] = m21; m[ 8] = m22;    	
    }
    
    /**
     * Creates a new 3x3 matrix by copying the matrix used as parameter.
     * @param n
     * 			The old matrix to be copied.
     */
    public Mat3(Mat3 n) {
    	super(9);
        buf.put(Buffers.copyFloatBuffer(n.asBuffer()));
    }
    
    /**
     * Multiplies this matrix with the given matrix, returning a new matrix. 
     * @param n
     * 			The matrix to be multiplied with the current matrix.
     * @return
     * 			The new 4x4 matrix that is the result of the multiplication.
     */
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
    
    /**
     * Multiplies this matrix with the given vector, returning a new matrix. 
     * @param v
     * 			The vector to be multiplied with the current matrix.
     * @return
     * 			The new 4x4 matrix that is the result of the multiplication.
     */
    public Vec3 mul(Vec3 v) {
    	return new Vec3(m[0 * 3 + 0]*v.v[0] + m[0 * 3 + 1]*v.v[1] + m[0 * 3 + 2]*v.v[2],
   		     			m[1 * 3 + 0]*v.v[0] + m[1 * 3 + 1]*v.v[1] + m[1 * 3 + 2]*v.v[2],
			   		    m[2 * 3 + 0]*v.v[0] + m[2 * 3 + 1]*v.v[1] + m[2 * 3 + 2]*v.v[2]);		
    }
    
    /**
     * Multiplies this matrix with the given scalar, returning a new matrix. 
     * @param n
     * 			The scalar to be multiplied with the current matrix.
     * @return
     * 			The new 4x4 matrix that is the result of the multiplication.
     */
    public Mat3 mul(Number n) {
    	float fn = n.floatValue();
		for (int i = 0; i < 9; ++i) {
			m[i] *= fn;
		}
		
		return this;
    }
    
    /**
     * Adds this matrix to the given matrix, returning a new matrix. 
     * @param n
     * 			The matrix to be added to the current matrix.
     * @return
     * 			The new 4x4 matrix that is the result of the addition.
     */
    public Mat3 add(Mat3 n) {
		for (int i = 0; i < 9; ++i) {
			m[i] += n.m[i];
		}
		
		return this;
    }
    
    /**
     * Substracts this matrix with the given matrix, returning a new matrix. 
     * @param n
     * 			The matrix to be substracted from to the current matrix.
     * @return
     * 			The new 4x4 matrix that is the result of the substraction.
     */
    public Mat3 sub(Mat3 n) {
		for (int i = 0; i < 9; ++i) {
			m[i] -= n.m[i];
		}
		
		return this;
    }
    
    /**
     * Divides the elements of this matrix with the given scalar, returning a new matrix. 
     * @param n
     * 			The scalar with which to divide the values of the current matrix.
     * @return
     * 			The new 4x4 matrix that is the result of the division.
     */
    public Mat3 div(Number n) {
    	float fn = 1f / n.floatValue();
    	
		for (int i = 0; i < 9; ++i) {
			m[i] *= fn;
		}
		
		return this;
    }
    
    public Mat3 clone() {
    	return new Mat3(this);
    }
}
