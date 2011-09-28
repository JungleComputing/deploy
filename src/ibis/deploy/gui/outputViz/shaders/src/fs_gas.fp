
uniform vec4 AmbientMaterial;

uniform sampler2D Colormap;

uniform int scrWidth;
uniform int scrHeight;
uniform int Multicolor;

void main() {
	if (Multicolor = 1) {
		vec2 tCoord = vec2(gl_FragCoord.x/float(scrWidth), gl_FragCoord.y/float(scrHeight));	
		
		vec4 color = texture2D(Colormap, tCoord);	
		     color = mix(AmbientMaterial, color, 0.5);
		
	    gl_FragColor = vec4(color.rgb, AmbientMaterial.a);
	} else {
		gl_FragColor = AmbientMaterial;
	}
	    
}
