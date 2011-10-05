uniform sampler2D axesTexture;
uniform sampler2D gasTexture;
uniform sampler2D starTexture;

uniform int scrWidth;
uniform int scrHeight;

const float sigma = 4.0;
const float pi = 3.14159265;
const float numBlurPixelsPerSide = 4.0;

const vec2 vertical = vec2(0.0, 1.0);
const vec2 horizontal = vec2(1.0, 0.0);

vec4 gaussianBlur(sampler2D tex, vec2 tCoord, vec2 multiplyVec, float blurSize) {
	// Incremental Gaussian Coefficent Calculation (See GPU Gems 3 pp. 877 - 889)
	vec3 incrementalGaussian;
  	incrementalGaussian.x = 1.0 / (sqrt(2.0 * pi) * sigma);
  	incrementalGaussian.y = exp(-0.5 / (sigma * sigma));
  	incrementalGaussian.z = incrementalGaussian.y * incrementalGaussian.y;

  	vec4 avgValue = vec4(0.0, 0.0, 0.0, 0.0);
  	float coefficientSum = 0.0;

  	// Take the central sample first...
  	avgValue += texture2D(tex, tCoord) * incrementalGaussian.x;
  	coefficientSum += incrementalGaussian.x;
  	incrementalGaussian.xy *= incrementalGaussian.yz;

  	// Go through the remaining 8 vertical samples (4 on each side of the center)
  	for (float i = 1.0; i <= numBlurPixelsPerSide; i++) { 
	    avgValue += texture2D(tex, tCoord - (i * blurSize * multiplyVec/float(scrWidth))) * incrementalGaussian.x;         
	    avgValue += texture2D(tex, tCoord + (i * blurSize * multiplyVec/float(scrWidth))) * incrementalGaussian.x;         
	    coefficientSum += 2.0 * incrementalGaussian.x;
	    incrementalGaussian.xy *= incrementalGaussian.yz;
  	}
  	
  	return vec4(avgValue / coefficientSum);
}

vec4 bloom(sampler2D tex, vec2 tCoord) {
	int i, j;
	vec4 sum = vec4(0);
	
	for( i= -4 ;i < 4; i++) {
        for (j = -3; j < 3; j++) {
            sum += texture2D(tex, tCoord + vec2(j, i)*0.004) * 0.25;
        }
	}
		
	if (texture(tex, tCoord).r < 0.3) {
    	return sum*sum*0.012 + texture2D(tex, tCoord);
	} else {
        if (texture(tex, tCoord).r < 0.5) {
            return sum*sum*0.009 + texture2D(tex, tCoord);
        } else {
            return sum*sum*0.0075 + texture2D(tex, tCoord);
        }
    }
}

void main() {
	//sampler2D intermediateTexture = sampler2D(scrWidth, scrHeight);
	vec2 tCoord   = vec2(gl_FragCoord.x/float(scrWidth), gl_FragCoord.y/float(scrHeight));
		
	vec4 axesColor = texture2D(axesTexture, tCoord) * 10;
  	vec4 gasColor = texture2D(gasTexture, tCoord) * 10;
	
	vec4 starColor = texture2D(starTexture, tCoord) * 10;
	
	//float blurSize = 2.0;
	//vec4 starColor = gaussianBlur(starTexture, tCoord, horizontal, blurSize) * 10;
	//starColor = mix(texture2D(starTexture, tCoord), starColor, 0.1);
    
    vec4 color = mix(starColor, gasColor, 0.1);
    	 color = mix(color, axesColor, 0.1);
    
    gl_FragColor = color;
}
