uniform sampler3D DensityTex;

//uniform vec3 LightPos;
//uniform vec3 LightIntensity;
//uniform vec3 EyePos;
//uniform float Absorption;

float Absorption = 0.5;
vec3 EyePos = vec3(0,0,0);
vec3 LightPos = vec3(200,200,200);
vec3 LightIntensity = vec3(1,1,1);

void main()
{
	// diagonal of the cube
	const float maxDist = sqrt(400.0);

	const int numSamples = 256;
	const float scale = maxDist/float(numSamples);

	const int numLightSamples = 32;
	const float lscale = maxDist / float(numLightSamples);

	// assume all coordinates are in texture space
	vec3 pos = gl_TexCoord[0].xyz;
	vec3 eyeDir = normalize(pos-EyePos)*scale;

	// transmittance
	float T = 1.0;
	// in-scattered radiance
	vec3 Lo = vec3(0.0);

	for (int i=0; i < numSamples; ++i)
	{
		// sample density
		float density = texture3D(DensityTex, pos).x;

		// skip empty space
		if (density > 0.0)
		{
			// attenuate ray-throughput
			T *= 1.0-density*scale*Absorption;
			if (T <= 0.01)
				break;

			// point light dir in texture space
			vec3 lightDir = normalize(LightPos-pos)*lscale;

			// sample light
			float Tl = 1.0;	// transmittance along light ray
			vec3 lpos = pos + lightDir;

			for (int s=0; s < numLightSamples; ++s)
			{
				float ld = texture3D(DensityTex, lpos).x;
				Tl *= 1.0-Absorption*lscale*ld;

				if (Tl <= 0.01)
					break;

				lpos += lightDir;
			}

			vec3 Li = LightIntensity*Tl;

			Lo += Li*T*density*scale;
		}

		pos += eyeDir;
	}

	gl_FragColor.xyz = Lo;
	gl_FragColor.w = 1.0-T;
}
