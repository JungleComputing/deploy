uniform sampler2D axesTexture;
uniform sampler2D gasTexture;
uniform sampler2D starTexture;

uniform int scrWidth;
uniform int scrHeight;

void main() {
	vec2 tCoord   = vec2(gl_FragCoord.x/float(scrWidth), gl_FragCoord.y/float(scrHeight));
		
	vec4 axesColor = vec4(texture2D(axesTexture, tCoord).rgb, 1.0) * 16;
  	vec4 gasColor  = vec4(texture2D(gasTexture, tCoord).rgb, 1.0) * 12;
	
	vec4 starColor = vec4(texture2D(starTexture, tCoord).rgb, 1.0);
    
    vec4 color = mix(starColor, gasColor, 0.1);
    	 color = mix(color, axesColor, 0.1);
    
    gl_FragColor = color;
}
