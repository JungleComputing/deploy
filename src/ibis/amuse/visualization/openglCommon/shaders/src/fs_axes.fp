#version 150

uniform vec4 AmbientMaterial;

out vec4 gl_FragColor;

void main() {
    gl_FragColor = AmbientMaterial;
}
