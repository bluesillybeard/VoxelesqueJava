#version 330

in  vec2 outTexCoord;
out vec4 fragColor;

uniform sampler2D texture_sampler;
uniform float timeSeconds; //required uniform

void main()
{
	vec4 color = texture(texture_sampler, outTexCoord);
	if(color.a < 0.1){
		discard; //I am aware that this is a lame solution for transparency, but honestly I think it's no big deal.
	}
	fragColor = color;
}
