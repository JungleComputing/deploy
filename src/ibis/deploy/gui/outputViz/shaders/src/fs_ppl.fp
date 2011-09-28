varying in  vec3 EyespaceNormal;

uniform vec3 LightPos;
uniform vec4 AmbientMaterial;
uniform vec4 SpecularMaterial;
uniform vec4 DiffuseMaterial;
uniform float Shininess;

uniform int Mode;

void main() {
	if (Mode == 0) {
	    vec3 N = normalize(EyespaceNormal);
	    vec3 L = normalize(LightPos);
	    vec3 E = vec3(0, 0, 1);
	    vec3 H = normalize(L + E);
	
	    float df = max(0.0, dot(N, L));
	    float sf = max(0.0, dot(N, H));
	    sf = pow(sf, Shininess);
	
	    vec4 color = AmbientMaterial + df * DiffuseMaterial + sf * SpecularMaterial;
	    gl_FragColor = color;
	} else {
		gl_FragColor = AmbientMaterial;
	}
}
