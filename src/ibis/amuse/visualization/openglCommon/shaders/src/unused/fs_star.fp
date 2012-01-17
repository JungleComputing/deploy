varying in vec2 pointCenter;                       
varying in vec4 color;      
varying in float brightness;  
                       
uniform float sigma2;                           
uniform float glareFalloff;                     
uniform float glareBrightness;                  
uniform float diffSpikeBrightness;              
uniform float exposure;     

vec3 linearToSRGB(vec3 c)                       
{                                               
    vec3 linear = 12.92 * c;                    
    vec3 nonlinear = (1.0 + 0.055) * pow(c, vec3(1.0 / 2.4)) - vec3(0.055);
    return mix(linear, nonlinear, step(vec3(0.0031308), c));
}

void main()                                     
{                                               
    vec2 offset = gl_FragCoord.xy - pointCenter;                
    float r2 = dot(offset, offset);                             
    float b = exp(-r2 / (2.0 * sigma2));                        
    float spikes = (max(0.0, 1.0 - abs(offset.x + offset.y)) + max(0.0, 1.0 - abs(offset.x - offset.y))) * diffSpikeBrightness;
    b += glareBrightness / (glareFalloff * pow(r2, 1.5) + 1.0) * (spikes + 0.5);     
    gl_FragColor = vec4(linearToSRGB(b * exposure * color.rgb * brightness), 1.0);   
}

   