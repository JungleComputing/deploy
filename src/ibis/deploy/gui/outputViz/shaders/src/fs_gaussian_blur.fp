uniform sampler2D Texture;

uniform int scrWidth;
uniform int scrHeight;

uniform int blurDirection;
uniform int blurType;

const float pi = 3.14159265;

const vec2 vertical = vec2(0.0, 1.0);
const vec2 horizontal = vec2(1.0, 0.0);

vec4 gaussianBlur(sampler2D tex, vec2 tCoord, vec2 multiplyVec, float blurSize, float numPixelsPerSide, float sigma) {
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
  	
  	for (float i = 1.0; i <= numPixelsPerSide; i++) {
  		vec2 offset = vec2((i * blurSize * multiplyVec/float(scrWidth)));
  		if (
  		tCoord.x - offset.x < 0.0 || tCoord.x + offset.x > 1.0 ||
  			tCoord.y - offset.y < 0.0 || tCoord.y + offset.y > 1.0) {
  			avgValue += 2 * texture2D(tex, tCoord) * incrementalGaussian.x;  			
  		} else {
  			avgValue += texture2D(tex, tCoord - offset) * incrementalGaussian.x;  		         
	    	avgValue += texture2D(tex, tCoord + offset) * incrementalGaussian.x;
	    }
	             
	    coefficientSum += 2.0 * incrementalGaussian.x;
	    incrementalGaussian.xy *= incrementalGaussian.yz;
  	}
  	
  	return vec4(avgValue / coefficientSum);
}

void main() {
	vec2 tCoord   = vec2(gl_FragCoord.x/float(scrWidth), gl_FragCoord.y/float(scrHeight));
	
	float blurSize;
	float sigma;
	float numPixelsPerSide;
	
	if (blurType == 2) {
		blurSize = 4.0;
		sigma = 2.0;
		numPixelsPerSide = 2.0;
	} else if(blurType == 4) {
		blurSize = 4.0;
		sigma = 4.0;
		numPixelsPerSide = 4.0;
	} else {
		blurSize = 8.0;
		sigma = 4.0;
		numPixelsPerSide = 8.0;
	}
	
	vec2 direction;
	if (blurDirection == 0) {
		direction = horizontal;
	} else {
		direction = vertical;
	}
	
  	gl_FragColor = gaussianBlur(Texture, tCoord, direction, blurSize, numPixelsPerSide, sigma);
}