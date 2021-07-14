package engine.graph;

import java.util.HashMap;
import java.util.Map;
import org.joml.Matrix4f;
import static org.lwjgl.opengl.GL20.*;
import org.lwjgl.system.MemoryStack;

public class ShaderProgram {

    private final int programId;

    private int vertexShaderId;

    private int fragmentShaderId;

    private int viewMatrixUniform, modelViewMatrixUniform, projectionMatrixUniform, textureSamplerUniform;

    public ShaderProgram(String vertexShaderCode, String fragmentShaderCode) throws Exception {


        programId = glCreateProgram();
        if (programId == 0) {
            throw new Exception("Could not create Shader");
        }

        vertexShaderId = createShader(vertexShaderCode, GL_VERTEX_SHADER);
        fragmentShaderId = createShader(fragmentShaderCode, GL_FRAGMENT_SHADER);
        link();

        modelViewMatrixUniform = glGetUniformLocation(programId, "modelViewMatrix");
        projectionMatrixUniform = glGetUniformLocation(programId, "projectionMatrix");
        textureSamplerUniform = glGetUniformLocation(programId, "texture_sampler");
        viewMatrixUniform = glGetUniformLocation(programId, "viewMatrix");
    }

    public void setModelViewMatrix(Matrix4f value){
        // Dump the matrix into a float buffer
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glUniformMatrix4fv(modelViewMatrixUniform, false,
                    value.get(stack.mallocFloat(16)));
        }
    }
    public void setProjectionMatrix(Matrix4f value){
        // Dump the matrix into a float buffer
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glUniformMatrix4fv(projectionMatrixUniform, false,
                    value.get(stack.mallocFloat(16)));
        }
    }
    public void setViewMatrix(Matrix4f value){
        // Dump the matrix into a float buffer
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glUniformMatrix4fv(viewMatrixUniform, false,
                    value.get(stack.mallocFloat(16)));
        }
    }
    public void setTextureSamplerUniform(int value) {
        glUniform1i(textureSamplerUniform, value);
    }

    protected int createShader(String shaderCode, int shaderType) throws Exception {
        int shaderId = glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new Exception("Error creating shader. Type: " + shaderType);
        }

        glShaderSource(shaderId, shaderCode);
        glCompileShader(shaderId);

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
            throw new Exception("Error compiling Shader code: " + glGetShaderInfoLog(shaderId, 1024));
        }

        glAttachShader(programId, shaderId);

        return shaderId;
    }

    public void link() throws Exception {
        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            throw new Exception("Error linking Shader code: " + glGetProgramInfoLog(programId, 1024));
        }

        if (vertexShaderId != 0) {
            glDetachShader(programId, vertexShaderId);
        }
        if (fragmentShaderId != 0) {
            glDetachShader(programId, fragmentShaderId);
        }

        glValidateProgram(programId);
        if (glGetProgrami(programId, GL_VALIDATE_STATUS) == 0) {
            System.err.println("Warning validating Shader code: " + glGetProgramInfoLog(programId, 1024));
        }
    }

    public void bind() {
        glUseProgram(programId);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void cleanup() {
        unbind();
        if (programId != 0) {
            glDeleteProgram(programId);
        }
    }
}