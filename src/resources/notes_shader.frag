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
uniform sampler2D u_texture;

uniform int u_visibility;
uniform vec4 u_v_points;
uniform vec2 u_cut_points;

float getAlpha(float start, float finish, vec2 points);


void main()
{    
    float v_alpha = 1.0;

    if(u_visibility == 1){ //p1
	v_alpha = getAlpha(1.0, 0.0, vec2(u_v_points.x, u_v_points.y));
    }
    else if (u_visibility == 2){
	v_alpha = getAlpha(0.0, 1.0, vec2(u_v_points.z, u_v_points.w));
    }
    else if (u_visibility == 3){
	v_alpha = getAlpha(0.0, 1.0, vec2(u_v_points.x, u_v_points.y));
	if(v_position.y < u_v_points.z)
	{
	    v_alpha = getAlpha(1.0, 0.0, vec2(u_v_points.z, u_v_points.w));
	}
    }

    if(v_position.y < u_cut_points.y || v_position.y > u_cut_points.x){ v_alpha = 0.0; }

    vec4 color = v_color * texture2D(u_texture, v_texCoords);
    gl_FragColor = vec4(color.r, color.g, color.b, color.a*v_alpha);
}

float getAlpha(float start, float finish, vec2 points)
{
    if(v_position.y > points.x){
	return(start);
    }
    else {
	float m = (finish - start) / (points.y - points.x);
	float b = -(m*points.y) + start;
	return(m * v_position.y + b);
    }
}