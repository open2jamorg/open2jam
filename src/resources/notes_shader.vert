attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;
uniform mat4 u_proj;
uniform mat4 u_trans;
uniform mat4 u_projTrans;
varying vec4 v_color;
varying vec2 v_texCoords;
varying vec4 v_position;
varying float v_alpha;

void main()
{
   v_color = a_color;
   v_texCoords = a_texCoord0;
   gl_Position =  u_projTrans * a_position;
   v_position = u_proj * a_position;
   v_alpha = 1.0;
}