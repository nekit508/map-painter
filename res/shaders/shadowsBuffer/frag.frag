uniform sampler2D u_texture;

varying vec2 v_texCoords;

void main() {
	vec4 texture_coolor = texture2D(u_texture, v_texCoords);
	gl_FragColor = vec4(0.0, 0.0, 0.0, 0.7 * texture_coolor.r) * texture_coolor.a;
}
