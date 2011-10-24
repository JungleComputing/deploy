package ibis.deploy.gui.outputViz.models;

import ibis.deploy.gui.outputViz.amuse.Astrophysics;
import ibis.deploy.gui.outputViz.common.Material;
import ibis.deploy.gui.outputViz.common.math.Mat4;
import ibis.deploy.gui.outputViz.common.math.Vec3;
import ibis.deploy.gui.outputViz.common.math.Vec4;
import ibis.deploy.gui.outputViz.exceptions.UninitializedException;
import ibis.deploy.gui.outputViz.models.base.Sphere;
import ibis.deploy.gui.outputViz.shaders.Program;

import javax.media.opengl.GL3;

public class StarModel extends Sphere {
    float radius;

    public StarModel(Program program, Material material, int ndiv, float radius, Vec3 center) {
        super(program, material, ndiv, radius, center);
        this.radius = radius;
    }

    public StarModel(Material material, int ndiv, float radius, Vec3 center) {
        super(material, ndiv, radius, center);
        this.radius = radius;
    }

    @Override
    public void draw(GL3 gl, Program program, Mat4 MVMatrix) {

        program.setUniformVector("DiffuseMaterial", material.diffuse);
        program.setUniformVector("AmbientMaterial", material.ambient);
        program.setUniformVector("SpecularMaterial", material.specular);

        Vec4 haloColor = new Vec4(material.ambient);
        float haloAlpha = (float) (0.5f - (radius / Astrophysics.STAR_RADIUS_AT_1000_SOLAR_RADII));
        haloColor.set(3, haloAlpha);

        program.setUniformVector("HaloColor", haloColor);

        program.setUniformMatrix("MVMatrix", MVMatrix);

        try {
            program.use(gl);
        } catch (UninitializedException e) {
            e.printStackTrace();
        }

        vbo.bind(gl);

        program.linkAttribs(gl, vbo.getAttribs());

        if (format == vertex_format.TRIANGLES) {
            gl.glDrawArrays(GL3.GL_TRIANGLES, 0, numVertices);
        } else if (format == vertex_format.POINTS) {
            gl.glDrawArrays(GL3.GL_POINTS, 0, numVertices);
        } else if (format == vertex_format.LINES) {
            gl.glDrawArrays(GL3.GL_LINES, 0, numVertices);
        }
    }
}