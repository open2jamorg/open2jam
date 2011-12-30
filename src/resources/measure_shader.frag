#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif
attribute vec4 a_position;
varying LOWP vec4 v_color;
varying vec2 v_texCoords;
varying vec4 v_position;
varying float v_alpha;
uniform sampler2D u_texture;

uniform int u_hidden;
uniform int u_sudden;
uniform int u_dark;

void main()
{
    if(v_position.y < -1 || v_position.y > 0.6){ v_alpha= 0.0; }

    vec4 color = v_color * texture2D(u_texture, v_texCoords);
    gl_FragColor = vec4(color.r, color.g, color.b, color.a*v_alpha);
}