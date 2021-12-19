package main.rendering.shaderloader;

import android.content.Context;
import android.content.res.Resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import main.rendering.shaderloader.ShaderLoader;

import static android.opengl.GLES20.*;
public abstract class ShaderProgram {
    // Uniform constants
    protected static final String U_MATRIX = "u_Matrix";
    protected static final String U_TEXTURE_UNIT = "u_TextureUnit";
    protected static final String A_COLOR = "a_Color";        // for hitboxes

    // Attribute constants
    protected static final String A_POSITION = "a_Position";
    protected static final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";

    // Shader PROGRAM
    protected final int PROGRAM;
    protected ShaderProgram(Resources resources, int vertexShaderResourceId,
                            int fragmentShaderResourceId) {
        // Compile the raw and link the PROGRAM.
        PROGRAM = ShaderLoader.buildProgram(
                TextResourceReader.readTextFileFromResource(
                        resources, vertexShaderResourceId),
                TextResourceReader.readTextFileFromResource(
                        resources, fragmentShaderResourceId));
    }

    public void useProgram() {
        glUseProgram(PROGRAM);
    }


    /* Load shader configuration from files */
    public static class TextResourceReader {

        public static String readTextFileFromResource(Resources resources, int resourceId) {
            StringBuilder body = new StringBuilder();
            try {
                InputStream inputStream = resources.openRawResource(resourceId);
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String nextLine;
                while ((nextLine = bufferedReader.readLine()) != null) {
                    body.append(nextLine);
                    body.append('\n');
                }
            } catch (IOException e) {
                throw new RuntimeException("Could not open resource:" + resourceId, e);
            } catch (Resources.NotFoundException nfe) {
                throw new RuntimeException("Resource not found:" + resourceId, nfe);
            } return body.toString();
        }
    }
}