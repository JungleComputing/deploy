package ibis.amuse.visualization.openglCommon.math;

public class MatrixFMath {

    public static double degreesToRadians = Math.PI / 180.0;

    /**
     * Helper method that creates a Orthogonal matrix
     * 
     * @param left
     *            The left clipping plane
     * @param right
     *            The right clipping plane
     * @param bottom
     *            The bottom clipping plane
     * @param top
     *            The top clipping plane
     * @param zNear
     *            The near clipping plane
     * @param zFar
     *            The far clipping plane
     * @return An orthogonal matrix
     */
    public static MatF4 ortho(float left, float right, float bottom, float top, float zNear, float zFar) {
        float dX = right - left;
        float dY = top - bottom;
        float dZ = zFar - zNear;
        float n = zNear;
        float f = zFar;
        float t = top;
        float b = bottom;
        float r = right;
        float l = left;

        MatF4 m = new MatF4(2 / dX, 0, 0, -(l + r) / dX, 0, 2 / dY, 0, -(t + b) / dY, 0, 0, -2 / (f - n), -(f + n) / dZ,
                0, 0, 0, 1);
        return m;
    }

    /**
     * Helper method to define an orthogonal matrix for 2d projections
     * 
     * @param left
     *            The left clipping plane
     * @param right
     *            The right clipping plane
     * @param bottom
     *            The bottom clipping plane
     * @param top
     *            The top clipping plane
     * @return An orthogonal matrix
     */
    public static MatF4 ortho2D(float left, float right, float bottom, float top) {
        return ortho(left, right, bottom, top, -1, 1);
    }

    /**
     * Helper method that creates a Frustum matrix
     * 
     * @param left
     *            The left clipping plane
     * @param right
     *            The right clipping plane
     * @param bottom
     *            The bottom clipping plane
     * @param top
     *            The top clipping plane
     * @param zNear
     *            The near clipping plane
     * @param zFar
     *            The far clipping plane
     * @return An frustum matrix
     */
    public static MatF4 frustum(float left, float right, float bottom, float top, float zNear, float zFar) {
        float dX = right - left;
        float dY = top - bottom;
        float dZ = zFar - zNear;
        float n = zNear;
        float f = zFar;
        float t = top;
        float b = bottom;
        float r = right;
        float l = left;

        MatF4 m = new MatF4(2 * n / dX, 0, (r + l) / dX, 0, 0, 2 * n / dY, (t + b) / dY, 0, 0, 0, -(f + n) / dZ, -2 * f
                * n / dZ, 0, 0, -1, 0);
        return m;
    }

    /**
     * Helper method that creates a perspective matrix
     * 
     * @param fovy
     *            The fov in y-direction, in degrees
     * 
     * @param aspect
     *            The aspect ratio
     * @param zNear
     *            The near clipping plane
     * @param zFar
     *            The far clipping plane
     * @return A perspective matrix
     */
    public static MatF4 perspective(float fovy, float aspect, float zNear, float zFar) {
        float t = (float) (Math.tan(fovy * degreesToRadians / 2) * zNear);
        float r = t * aspect;
        float n = zNear;
        float f = zFar;
        float dZ = zFar - zNear;

        MatF4 m = new MatF4((n / r), 0, 0, 0, 0, (n / t), 0, 0, 0, 0, -(f + n) / dZ, -2 * f * n / dZ, 0, 0, -1, 0);

        return m;
    }

    /**
     * Helper method that supplies a rotation matrix that allows us to look at
     * the indicated point
     * 
     * @param eye
     *            The coordinates of the eye (camera)
     * @param at
     *            The coordinates of the object we want to look at
     * @param up
     *            The vector indicating the up direction for the camera
     * @return A rotation matrix suitable for multiplication with the
     *         perspective matrix
     */
    public static MatF4 lookAt(VecF4 eye, VecF4 at, VecF4 up) {
        VecF4 eyeneg = eye.clone().neg();

        VecF4 n = VectorFMath.normalize(eye.sub(at));
        VecF4 u = VectorFMath.normalize(VectorFMath.cross(up, n));
        VecF4 v = VectorFMath.normalize(VectorFMath.cross(n, u));
        VecF4 t = new VecF4(0, 0, 0, 1);
        MatF4 c = new MatF4(u, v, n, t);

        return c.mul(translate(eyeneg));
    }

    /**
     * Helper method that creates a translation matrix
     * 
     * @param x
     *            The x translation
     * @param y
     *            The y translation
     * @param z
     *            The z translation
     * @return A translation matrix
     */
    public static MatF4 translate(float x, float y, float z) {
        MatF4 m = new MatF4(1, 0, 0, x, 0, 1, 0, y, 0, 0, 1, z, 0, 0, 0, 1);
        return m;
    }

    /**
     * Helper method that creates a translation matrix
     * 
     * @param vec
     *            The vector with which we want to translate
     * @return A translation matrix
     */
    public static MatF4 translate(VecF3 vec) {
        return translate(vec.v[0], vec.v[1], vec.v[2]);
    }

    /**
     * Helper method that creates a translation matrix
     * 
     * @param vec
     *            The vector with which we want to translate
     * @return A translation matrix
     */
    public static MatF4 translate(VecF4 vec) {
        return translate(vec.v[0], vec.v[1], vec.v[2]);
    }

    /**
     * Helper method that creates a scaling matrix
     * 
     * @param x
     *            The x scale
     * @param y
     *            The y scale
     * @param z
     *            The z scale
     * @return A scaling matrix
     */
    public static MatF4 scale(float x, float y, float z) {
        MatF4 m = new MatF4(x, 0, 0, 0, 0, y, 0, 0, 0, 0, z, 0, 0, 0, 0, 1);
        return m;
    }

    /**
     * Helper method that creates a scaling matrix
     * 
     * @param newScale
     *            The new uniform scale.
     * @return A scaling matrix
     */
    public static MatF4 scale(float newScale) {
        return scale(newScale, newScale, newScale);
    }

    /**
     * Helper method that creates a scaling matrix
     * 
     * @param vec
     *            The vector with which we want to scale
     * @return A scaling matrix
     */
    public static MatF4 scale(VecF3 vec) {
        return scale(vec.v[0], vec.v[1], vec.v[2]);
    }

    /**
     * Helper method that creates a scaling matrix
     * 
     * @param vec
     *            The vector with which we want to scale
     * @return A scaling matrix
     */
    public static MatF4 scale(VecF4 vec) {
        return scale(vec.v[0], vec.v[1], vec.v[2]);
    }

    /**
     * Helper method that creates a matrix describing a rotation around the
     * x-axis
     * 
     * @param angleDeg
     *            The rotation angle, in degrees
     * @return The rotation matrix
     */
    public static MatF4 rotationX(float angleDeg) {
        double angleRad = degreesToRadians * angleDeg;
        float ca = (float) Math.cos(angleRad);
        float sa = (float) Math.sin(angleRad);

        MatF4 m = new MatF4(1, 0, 0, 0, 0, ca, -sa, 0, 0, sa, ca, 0, 0, 0, 0, 1);
        return m;
    }

    /**
     * Helper method that creates a matrix describing a rotation around the
     * y-axis
     * 
     * @param angleDeg
     *            The rotation angle, in degrees
     * @return The rotation matrix
     */
    public static MatF4 rotationY(float angleDeg) {
        double angleRad = degreesToRadians * angleDeg;
        float ca = (float) Math.cos(angleRad);
        float sa = (float) Math.sin(angleRad);

        MatF4 m = new MatF4(ca, 0, sa, 0, 0, 1, 0, 0, -sa, 0, ca, 0, 0, 0, 0, 1);

        return m;
    }

    /**
     * Helper method that creates a matrix describing a rotation around the
     * z-axis
     * 
     * @param angleDeg
     *            The rotation angle, in degrees
     * @return The rotation matrix
     */
    public static MatF4 rotationZ(float angleDeg) {
        double angleRad = degreesToRadians * angleDeg;
        float ca = (float) Math.cos(angleRad);
        float sa = (float) Math.sin(angleRad);

        MatF4 m = new MatF4(ca, -sa, 0, 0, sa, ca, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1);

        return m;
    }

    /**
     * Helper method that creates a matrix describing a rotation around an
     * arbitrary axis
     * 
     * @param angleDeg
     *            The rotation angle, in degrees
     * @param x
     *            The x component of the vector that describes the axis to
     *            rotate around
     * @param y
     *            The y component of the vector that describes the axis to
     *            rotate around
     * @param z
     *            The z component of the vector that describes the axis to
     *            rotate around
     * @return The rotation matrix
     */
    public static MatF4 rotate(float angleDeg, float x, float y, float z) {
        double angleRad = degreesToRadians * angleDeg;
        float c = (float) Math.cos(angleRad);
        float s = (float) Math.sin(angleRad);
        float t = 1 - c;

        VecF3 n = VectorFMath.normalize(new VecF3(x, y, z));
        x = n.v[0];
        y = n.v[1];
        z = n.v[2];

        MatF4 R = new MatF4(t * x * x + c, t * x * y - s * z, t * x * z + s * y, 0f, t * x * y + s * z, t * y * y + c, t
                * y * z - s * x, 0f, t * x * z - s * y, t * y * z + s * x, t * z * z + c, 0f, 0f, 0f, 0f, 1f

        );

        return R;
    }

    /**
     * Helper method that creates a matrix describing a rotation around an
     * arbitrary axis
     * 
     * @param angleDeg
     *            The rotation angle, in degrees
     * @param axis
     *            The axis to rotate around
     * @return The rotation matrix
     */
    public static MatF4 rotate(float angleDeg, VecF3 axis) {
        MatF4 R = rotate(angleDeg, axis.get(0), axis.get(1), axis.get(2));

        return R;
    }

    /**
     * Helper method that creates a matrix describing a rotation around an
     * arbitrary axis
     * 
     * @param angleDeg
     *            The rotation angle, in degrees
     * @param axis
     *            The axis to rotate around
     * @return The rotation matrix
     */
    public static MatF4 rotate(float angleDeg, VecF4 axis) {
        MatF4 R = rotate(angleDeg, axis.get(0), axis.get(1), axis.get(2));

        return R;
    }

}
