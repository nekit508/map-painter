uniform mat4 u_projTrans;

attribute vec4 a_position;
attribute vec2 a_texCoord0;
attribute float a_height;

varying float v_height;
varying vec2 v_texCoords;

uniform vec2 u_viewportInverse;

void main(){
    v_height = a_height;
    v_texCoords = a_texCoord0;
    gl_Position = u_projTrans * a_position;
}
