//varying in float LightIntensity; 
varying in vec3  MCposition;

uniform sampler3D Noise;

uniform vec4 AmbientMaterial;
uniform float Offset;

vec4 Color1 = vec4(0,0,0,0);
float noiseScale = .1;


void main() {	
    vec4 noisevec = texture(Noise, MCposition * noiseScale + 128 * MCposition.x);// * Offset);

    float intensity = (noisevec[0] +
                       noisevec[1] +
                       noisevec[2] +
                       noisevec[3] + 0.03125) * 1.5;

    vec3 color   = mix(Color1, AmbientMaterial.rgb, intensity);    
   
	gl_FragColor = vec4(color, AmbientMaterial.a);
}