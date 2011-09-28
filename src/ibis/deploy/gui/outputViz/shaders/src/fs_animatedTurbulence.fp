varying in float LightIntensity; 
varying in vec3  MCposition;

uniform sampler3D Noise;
//uniform vec3 Color1;     
uniform vec4 AmbientMaterial;     
uniform float NoiseScale;
uniform float Offset;

vec3 Color1 = vec3(0,0,0);
vec3 Color2 = AmbientMaterial.rgb;

void main() {	 
    vec4 noisevecX 		= texture(Noise,      NoiseScale * .33 * MCposition + vec3(Offset, 0, 0));
    vec4 noisevecXglow 	= texture(Noise, .5 * NoiseScale * .33 * MCposition + vec3(Offset, 0, 0));
    vec4 noisevecY 		= texture(Noise,      NoiseScale * .5  * MCposition + vec3(0, Offset*2, 0));
    vec4 noisevecYglow 	= texture(Noise, .5 * NoiseScale * .33 * MCposition + vec3(Offset, 0, 0));
    vec4 noisevecZ 		= texture(Noise,      NoiseScale *       MCposition + vec3(0, 0, Offset*3));
    vec4 noisevecZglow 	= texture(Noise, .5 * NoiseScale * .33 * MCposition + vec3(Offset, 0, 0));

    float intensity = ((noisevecX[0] +
                        noisevecX[1] +
                        noisevecX[2] +
                        noisevecX[3]) * 0.33) +
                      ((noisevecY[0] +
                        noisevecY[1] +
                        noisevecY[2] +
                        noisevecY[3]) * 0.33) +
                      ((noisevecZ[0] +
                        noisevecZ[1] +
                        noisevecZ[2] +
                        noisevecZ[3]) * 0.33);
                        
	float intensityGlow = ((noisevecXglow[0] +
	                        noisevecXglow[1] +
	                        noisevecXglow[2] +
	                        noisevecXglow[3]) * 0.33) +
	                      ((noisevecYglow[0] +
	                        noisevecYglow[1] +
	                        noisevecYglow[2] +
	                        noisevecYglow[3]) * 0.33) +
	                      ((noisevecZglow[0] +
	                        noisevecZglow[1] +
	                        noisevecZglow[2] +
	                        noisevecZglow[3]) * 0.33);

    vec3 color   	= mix(Color1, Color2, intensity) * 25;
    vec3 colorGlow  = mix(Color1, Color2, intensityGlow) * 25;
    
    vec3 glowing = mix(color, colorGlow, 0.5) ;
    
	gl_FragColor = vec4(glowing, 1.0);
}
