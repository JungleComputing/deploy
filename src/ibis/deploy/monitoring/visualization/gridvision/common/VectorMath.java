package ibis.deploy.monitoring.visualization.gridvision.common;

public class VectorMath {
    public static float dot(Vec2 u, Vec2 v) {
        return u.v[0] * v.v[0] + u.v[1] * v.v[1];
    }
    
    public static float dot(Vec3 u, Vec3 v) {
    	return u.v[0] * v.v[0] + u.v[1] * v.v[1] + u.v[2] * v.v[2];
    }
    
    public static float dot(Vec4 u, Vec4 v) {
    	return u.v[0] * v.v[0] + u.v[1] * v.v[1] + u.v[2] * v.v[2] + u.v[3] * v.v[3];
    }

    public static float length(Vec2 v) {
        return (float) Math.sqrt(dot(v, v));
    }
    
    public static float length(Vec3 v) {
        return (float) Math.sqrt(dot(v, v));
    }
    
    public static float length(Vec4 v) {
        return (float) Math.sqrt(dot(v, v));
    }
    
    public static Vec2 normalize(Vec2 v) {
        return v.div(length(v));
    }
    
    public static Vec3 normalize(Vec3 v) {
        return v.div(length(v));
    }
    
    public static Vec4 normalize(Vec4 v) {
        return v.div(length(v));
    }
    
    public static Vec3 cross(Vec3 u, Vec3 v) {
        return new Vec3(u.v[1] * v.v[2] - u.v[2] * v.v[1], 
                        u.v[2] * v.v[0] - u.v[0] * v.v[2],
                        u.v[0] * v.v[1] - u.v[1] * v.v[0]);
    }
    
    public static Vec4 cross(Vec4 u, Vec4 v) {
        return new Vec4(u.v[1] * v.v[2] - u.v[2] * v.v[1], 
                        u.v[2] * v.v[0] - u.v[0] * v.v[2],
                        u.v[0] * v.v[1] - u.v[1] * v.v[0],
        				0.0f);
    }
}
