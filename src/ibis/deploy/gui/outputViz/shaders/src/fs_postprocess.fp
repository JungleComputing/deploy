uniform sampler2D axesTexture;
uniform sampler2D gasTexture;
uniform sampler2D starTexture;

uniform int scrWidth;
uniform int scrHeight;

void main() {
	vec2 tCoord   = vec2(gl_FragCoord.x/float(scrWidth), gl_FragCoord.y/float(scrHeight));
		
	vec4 axesColor = texture2D(axesTexture, tCoord) * 10;
  	vec4 gasColor = texture2D(gasTexture, tCoord) * 10;
	
	vec4 starColor = texture2D(starTexture, tCoord) * 3;
    
    vec4 color = mix(starColor, gasColor, 0.1);
    	 color = mix(color, axesColor, 0.1);
    
    gl_FragColor = color;
}
