#extension GL_EXT_geometry_shader4 : enable

varying in vec3 vPosition[3];
varying in vec3 vNormal[3];

varying out vec2 gPosition;
varying out vec2 gEndpoints[2];

uniform float Radius;
uniform mat4 MVMatrix;
uniform mat4 PMatrix;

vec4 obb[8];
mat4 MVPMatrix = MVMatrix * PMatrix;

vec2 to_screen(vec3 v)
{
    v = (MVMatrix * vec4(v, 1)).xyz;
    vec4 u = PMatrix * vec4(v, 1.0);
    return u.xy / u.w;
}

void emit()
{
    gPosition = gl_Position.xy / gl_Position.w;
    EmitVertex();
}

void emit(int a, int b, int c, int d)
{
    gl_Position = obb[a]; emit();
    gl_Position = obb[b]; emit();
    gl_Position = obb[c]; emit();
    gl_Position = obb[d]; emit();
    EndPrimitive();
}

void main()
{
    // Pass raytracing inputs to fragment shader:
    gEndpoints[0] = to_screen(vPosition[1]);
    gEndpoints[1] = to_screen(vPosition[2]);
    
    // Compute orientation vectors for the connecting faces:
    vec3 p0, p1, p2, p3;
    p0 = vPosition[0]; p1 = vPosition[1];
    p2 = vPosition[2]; p3 = vPosition[3];
    vec3 n0 = normalize(p1-p0);
    vec3 n1 = normalize(p2-p1);
    vec3 n2 = normalize(p3-p2);
    vec3 u = normalize(n0+n1);
    vec3 v = normalize(n1+n2);

    // Compute the eight corners:

    vec3 i,j,k; float r = Radius;

    j = u; i = vNormal[1]; k = cross(i, j);
    obb[0] = MVPMatrix*vec4(p1 + i*r + k*r,1);
    obb[1] = MVPMatrix*vec4(p1 + i*r - k*r,1);
    obb[2] = MVPMatrix*vec4(p1 - i*r - k*r,1);
    obb[3] = MVPMatrix*vec4(p1 - i*r + k*r,1);

    j = v; i = vNormal[2]; k = cross(i, j);
    obb[4] = MVPMatrix*vec4(p2 + i*r + k*r,1);
    obb[5] = MVPMatrix*vec4(p2 + i*r - k*r,1);
    obb[6] = MVPMatrix*vec4(p2 - i*r - k*r,1);
    obb[7] = MVPMatrix*vec4(p2 - i*r + k*r,1);

    // Emit the six faces of the prismoid:
    emit(0,1,3,2);
    emit(5,4,6,7);
    emit(4,5,0,1);
    emit(3,2,7,6);
    emit(0,3,4,7);
    emit(2,1,6,5);
}