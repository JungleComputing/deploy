package ibis.amuse.visualization.openglCommon.math;

import java.nio.FloatBuffer;
import java.util.List;

public class VectorMath {
	
    /**
     * Helper method to calculate the dot product of two vectors
     * @param u
     * 		The first vector.
     * @param v
     * 		The second vector.
     * @return
     * 		The dot product of the two vectors.
     */
    public static float dot(Vec2 u, Vec2 v) {
        return u.v[0] * v.v[0] + u.v[1] * v.v[1];
    }
    
    /**
     * Helper method to calculate the dot product of two vectors
     * @param u
     * 		The first vector.
     * @param v
     * 		The second vector.
     * @return
     * 		The dot product of the two vectors.
     */
    public static float dot(Vec3 u, Vec3 v) {
    	return u.v[0] * v.v[0] + u.v[1] * v.v[1] + u.v[2] * v.v[2];
    }
    
    /**
     * Helper method to calculate the dot product of two vectors
     * @param u
     * 		The first vector.
     * @param v
     * 		The second vector.
     * @return
     * 		The dot product of the two vectors.
     */
    public static float dot(Vec4 u, Vec4 v) {
    	return u.v[0] * v.v[0] + u.v[1] * v.v[1] + u.v[2] * v.v[2] + u.v[3] * v.v[3];
    }

    /**
     * Helper method to calculate the length of a vector.
     * @param u
     * 		The vector.
     * @return
     * 		The length of the vector.
     */
    public static float length(Vec2 v) {
        return (float) Math.sqrt(dot(v, v));
    }
    
    /**
     * Helper method to calculate the length of a vector.
     * @param u
     * 		The vector.
     * @return
     * 		The length of the vector.
     */
    public static float length(Vec3 v) {
        return (float) Math.sqrt(dot(v, v));
    }
    
    /**
     * Helper method to calculate the length of a vector.
     * @param u
     * 		The vector.
     * @return
     * 		The length of the vector.
     */
    public static float length(Vec4 v) {
        return (float) Math.sqrt(dot(v, v));
    }
    
    /**
     * Helper method to normalize a vector.
     * @param u
     * 		The vector.
     * @return
     * 		The normal of the vector.
     */
    public static Vec2 normalize(Vec2 v) {
        return v.div(length(v));
    }
    
    /**
     * Helper method to normalize a vector.
     * @param u
     * 		The vector.
     * @return
     * 		The normal of the vector.
     */
    public static Vec3 normalize(Vec3 v) {
        return v.div(length(v));
    }
    
    /**
     * Helper method to normalize a vector.
     * @param u
     * 		The vector.
     * @return
     * 		The normal of the vector.
     */
    public static Vec4 normalize(Vec4 v) {
        return v.div(length(v));
    }
    
    /**
     * Helper method to calculate the cross product of two vectors
     * @param u
     * 		The first vector.
     * @param v
     * 		The second vector.
     * @return
     * 		The new vector, which is the cross product of the two vectors.
     */
    public static Vec3 cross(Vec3 u, Vec3 v) {
        return new Vec3(u.v[1] * v.v[2] - u.v[2] * v.v[1], 
                        u.v[2] * v.v[0] - u.v[0] * v.v[2],
                        u.v[0] * v.v[1] - u.v[1] * v.v[0]);
    }
    
    /**
     * Helper method to calculate the cross product of two vectors
     * @param u
     * 		The first vector.
     * @param v
     * 		The second vector.
     * @return
     * 		The new vector, which is the cross product of the two vectors.
     */
    public static Vec4 cross(Vec4 u, Vec4 v) {
        return new Vec4(u.v[1] * v.v[2] - u.v[2] * v.v[1], 
                        u.v[2] * v.v[0] - u.v[0] * v.v[2],
                        u.v[0] * v.v[1] - u.v[1] * v.v[0],
        				0.0f);
    }
    
    /**
     * Helper method to create a FloatBuffer from an array of vectors. 
     * @param array
     * 		The array of vectors.
     * @return
     * 		The new FloatBuffer
     */
    public static FloatBuffer toBuffer(Vec3[] array) {
		FloatBuffer result = FloatBuffer.allocate(array.length*3);
		
		for (int i=0; i < array.length; i++) {
			result.put(array[i].asBuffer());
		}
		
		result.rewind();
		
		return result;
	}
    
    /**
     * Helper method to create a FloatBuffer from an array of vectors. 
     * @param array
     * 		The array of vectors.
     * @return
     * 		The new FloatBuffer
     */
    public static FloatBuffer toBuffer(Vec4[] array) {
		FloatBuffer result = FloatBuffer.allocate(array.length*4);
		
		for (int i=0; i < array.length; i++) {
			result.put(array[i].asBuffer());
		}
		
		result.rewind();
		
		return result;
	}
    
    /**
     * Helper method to create a FloatBuffer from an array of vectors.
     * @param array
     * 		The List of vectors.
     * @return
     * 		The new FloatBuffer
     */
    public static FloatBuffer vec2ListToBuffer(List<Vec2> list) {
		FloatBuffer result = FloatBuffer.allocate(list.size()*2);
		
		for (Vector v : list) {		
			result.put(v.asBuffer());
		}
		
		result.rewind();
		
		return result;
	}
    
    /**
     * Helper method to create a FloatBuffer from an array of vectors.
     * @param array
     * 		The List of vectors.
     * @return
     * 		The new FloatBuffer
     */
    public static FloatBuffer vec3ListToBuffer(List<Vec3> list) {
		FloatBuffer result = FloatBuffer.allocate(list.size()*3);
		
		for (Vector v : list) {		
			result.put(v.asBuffer());
		}
		
		result.rewind();
		
		return result;
	}
    
    /**
     * Helper method to create a FloatBuffer from an array of vectors.
     * @param array
     * 		The List of vectors.
     * @return
     * 		The new FloatBuffer
     */
    public static FloatBuffer vec4ListToBuffer(List<Vec4> list) {
		FloatBuffer result = FloatBuffer.allocate(list.size()*4);
		
		for (Vector v : list) {		
			result.put(v.asBuffer());
		}
		
		result.rewind();
		
		return result;
	}
}
