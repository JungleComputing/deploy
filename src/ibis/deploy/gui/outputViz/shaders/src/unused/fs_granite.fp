varying in float LightIntensity; 
varying in vec3  MCposition;

uniform sampler3D Noise;
uniform float NoiseScale;  // 1.2

void main() {	
    vec4 noisevec = texture(Noise, NoiseScale * MCposition);

    float intensity = min(1.0, noisevec[3] * 18);

    vec3 color   = intensity * LightIntensity * 1.5;    
   
	gl_FragColor = vec4(color, 1.0);
}