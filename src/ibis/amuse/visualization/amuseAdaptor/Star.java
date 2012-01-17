package ibis.amuse.visualization.amuseAdaptor;

import ibis.amuse.visualization.openglCommon.exceptions.UninitializedException;
import ibis.amuse.visualization.openglCommon.math.Mat4;
import ibis.amuse.visualization.openglCommon.math.MatrixMath;
import ibis.amuse.visualization.openglCommon.math.Vec3;
import ibis.amuse.visualization.openglCommon.math.Vec4;
import ibis.amuse.visualization.openglCommon.models.Model;
import ibis.amuse.visualization.openglCommon.shaders.Program;

import javax.media.opengl.GL3;


public class Star {
    private final Vec4 color;
    private final Vec3 location;
    private final float radius;
    private final Model model;

    public Star(Model baseModel, Vec3 location, double radius, double luminosity) {
        this.model = baseModel;
        this.location = location;

        this.color = Astrophysics.starColor(luminosity, radius);
        this.radius = Astrophysics.starToScreenRadius(radius);
    }

    public void init(GL3 gl) {
        model.init(gl);
    }

    public void draw(GL3 gl, Program program, Mat4 MVMatrix) {
        program.setUniformVector("DiffuseMaterial", color);
        program.setUniformVector("AmbientMaterial", color);
        program.setUniformVector("SpecularMaterial", color);

        Vec4 haloColor = new Vec4(color);
        float haloAlpha = (float) (0.5f - (radius / Astrophysics.STAR_RADIUS_AT_1000_SOLAR_RADII));
        haloColor.set(3, haloAlpha);

        program.setUniformVector("HaloColor", haloColor);

        MVMatrix = MVMatrix.mul(MatrixMath.translate(location));
        MVMatrix = MVMatrix.mul(MatrixMath.scale(radius));
        program.setUniformMatrix("MVMatrix", MVMatrix);

        try {
            program.use(gl);
        } catch (UninitializedException e) {
            e.printStackTrace();
        }

        model.vbo.bind(gl);

        program.linkAttribs(gl, model.vbo.getAttribs());

        gl.glDrawArrays(GL3.GL_TRIANGLES, 0, model.numVertices);
    }

    public float getRadius() {
        return radius;
    }
}
