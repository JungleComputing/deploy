varying in float LightIntensity;
varying in vec4 color;

void main()
{
    gl_FragColor = color;// * LightIntensity;
}