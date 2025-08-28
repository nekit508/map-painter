uniform sampler2D u_texture;

varying vec2 v_texCoords;
varying float v_height;

void main() {
	vec4 tex_color = texture2D(u_texture, v_texCoords);
	tex_color.r = v_height;
	tex_color.g = v_height;
	tex_color.b = v_height;
	gl_FragColor = tex_color;
}
