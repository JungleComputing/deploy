package ibis.amuse.visualization.openglCommon.models;


import ibis.amuse.visualization.amuseAdaptor.Astrophysics;
import ibis.amuse.visualization.openglCommon.Material;
import ibis.amuse.visualization.openglCommon.exceptions.UninitializedException;
import ibis.amuse.visualization.openglCommon.math.MatF4;
import ibis.amuse.visualization.openglCommon.math.VecF3;
import ibis.amuse.visualization.openglCommon.math.VecF4;
import ibis.amuse.visualization.openglCommon.models.base.Sphere;
import ibis.amuse.visualization.openglCommon.shaders.Program;

import javax.media.opengl.GL3;


public class StarModel extends Sphere {
    float radius;

    public StarModel(Program program, Material material, int ndiv, float radius, VecF3 center) {
        super(program, material, ndiv, radius, center);
        this.radius = radius;
    }

    public StarModel(Material material, int ndiv, float radius, VecF3 center) {
        super(material, ndiv, radius, center);
        this.radius = radius;
    }

    @Override
    public void draw(GL3 gl, Program program, MatF4 MVMatrix) {

        program.setUniformVector("DiffuseMaterial", material.diffuse);
        program.setUniformVector("AmbientMaterial", material.ambient);
        program.setUniformVector("SpecularMaterial", material.specular);

        VecF4 haloColor = new VecF4(material.ambient);
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
