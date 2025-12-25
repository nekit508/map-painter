uniform sampler2D u_texture;
uniform float u_sunAzimuth;
uniform float u_sunElevation;

varying vec2 v_texCoords;

void main() {
	vec4 normalMap = texture2D(u_texture, v_texCoords);

	vec3 normal = normalize(normalMap.rgb * 2.0 - vec3(1.0));

	float fact = 1.0 - max(0.0, dot(normal, vec3(
		cos(u_sunAzimuth) * cos(u_sunElevation),
		sin(u_sunAzimuth) * cos(u_sunElevation),
		sin(u_sunElevation))
	));

	fact = pow(fact, 1);

	gl_FragColor = vec4(0.0, 0.0, 0.0, 0.7 * fact * normalMap.a);
}
