//
// Fragment shader for refraction
//
// Author: Randi Rost
//
// Copyright (c) 2003-2006: 3Dlabs, Inc.
//
// See 3Dlabs-License.txt for license information
//

uniform samplerCube Cubemap;

varying in vec3  Reflect;
varying in vec3  Refract;
varying in float Ratio;

void main()
{
    vec3 refractColor = vec3(texture(Cubemap, Refract));
    vec3 reflectColor = vec3(texture(Cubemap, Reflect));

    vec3 color   = mix(refractColor, reflectColor, Ratio);

    gl_FragColor = vec4(color, 1.0);
}