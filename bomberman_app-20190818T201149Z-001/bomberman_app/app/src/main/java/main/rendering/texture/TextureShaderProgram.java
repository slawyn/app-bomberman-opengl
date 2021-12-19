package main.rendering.texture;
/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material,
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose.
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
 ***/


import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import android.content.Context;
import android.content.res.Resources;

import com.game.bomber.R;

import main.rendering.VertexArray;
import main.rendering.shaderloader.ShaderProgram;

public class TextureShaderProgram extends ShaderProgram
{
    // Uniform locations
    private final int U_MATRIX_LOCATION;
    private final int U_TEXTURE_LOCATION;

    // Attribute locations
    private final int A_POSITION_LOCATION;
    private final int A_TEXTURE_COORDINATES_LOCATION;

    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT + TEXTURE_COORDINATES_COMPONENT_COUNT) * 4;


    public TextureShaderProgram(Resources resources) {
        super(resources, R.raw.texture_vertex_shader,
                R.raw.texture_fragment_shader);

        // Retrieve uniform locations for the shader PROGRAM.
        U_MATRIX_LOCATION = glGetUniformLocation(PROGRAM, U_MATRIX);
        U_TEXTURE_LOCATION = glGetUniformLocation(PROGRAM, U_TEXTURE_UNIT);

        // Retrieve attribute locations for the shader PROGRAM.
        A_POSITION_LOCATION = glGetAttribLocation(PROGRAM, A_POSITION);
        A_TEXTURE_COORDINATES_LOCATION = glGetAttribLocation(PROGRAM, A_TEXTURE_COORDINATES);

    }

    public void setUniformsFast(final float[] matrix){
        glUniformMatrix4fv(U_MATRIX_LOCATION, 1, false, matrix, 0);
    }

    public void setUniforms(final float[] matrix, final int textureId, final int textureUnit) {
        // Pass the matrix into the shader PROGRAM.
        glUniformMatrix4fv(U_MATRIX_LOCATION, 1, false, matrix, 0);

        // Set the active texture unit to texture unit 0.
        glActiveTexture(textureUnit);

        // Bind the texture to this unit.
        glBindTexture(GL_TEXTURE_2D, textureId);

        // Tell the texture uniform sampler to use this texture in the shader by
        // telling it to read from texture unit
        glUniform1i(U_TEXTURE_LOCATION, textureUnit-GL_TEXTURE0);

    }

    public void bindData(VertexArray array)
    {
        array.setVertexAttribPointer(
                0,
                A_POSITION_LOCATION,
                POSITION_COMPONENT_COUNT,
                STRIDE);

        array.setVertexAttribPointer(
                POSITION_COMPONENT_COUNT,
                A_TEXTURE_COORDINATES_LOCATION,
                TEXTURE_COORDINATES_COMPONENT_COUNT,
                STRIDE);
    }



    public int getPositionAttributeLocation() {
        return A_POSITION_LOCATION;
    }

    public int getTextureCoordinatesAttributeLocation() {
        return A_TEXTURE_COORDINATES_LOCATION;
    }


}
