package main.rendering.color;
/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material,
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose.
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
 ***/


import android.content.Context;
import android.content.res.Resources;

import com.game.bomber.R;

import main.rendering.VertexArray;
import main.rendering.shaderloader.ShaderProgram;

import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform4fv;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glVertexAttribPointer;

public class ColorShaderProgram extends ShaderProgram
{
    // Uniform locations
    private final int U_MATRIX_LOCATION;
    // Attribute locations
    private final int A_POSITION_LOCATION;
    private final int A_COLOR_LOCATION;

    private float[] mHitboxColor = {
                0f, 0f, 1f, 0.5f, // vertex 0
                0f, 0f, 1f, 0.5f, // vertex 1
                0f, 0f, 1f, 0.5f, // vertex 2
                0f, 0f, 1f, 0.5f, // vertex 3
                0f, 0f, 1f, 0.5f, // vertex 4
                0f, 0f, 1f, 0.5f, // vertex 5
    };

    private VertexArray mHitboxColorArray;

    public ColorShaderProgram(Resources resources) {
        super(resources, R.raw.simple_vertex_shader,
                R.raw.simple_fragment_shader);

        // Retrieve uniform locations for the shader PROGRAM.
        U_MATRIX_LOCATION = glGetUniformLocation(PROGRAM, U_MATRIX);

        // Retrieve attribute locations for the shader PROGRAM.
        A_POSITION_LOCATION = glGetAttribLocation(PROGRAM, A_POSITION);
        A_COLOR_LOCATION = glGetAttribLocation(PROGRAM, A_COLOR);

        mHitboxColorArray = new VertexArray(mHitboxColor);
    }


    public void setUniforms(final float[] matrix) {
        // Pass the matrix into the shader PROGRAM.
       glUniformMatrix4fv(U_MATRIX_LOCATION, 1, false, matrix, 0);
       mHitboxColorArray.setVertexAttribPointer(0, A_COLOR_LOCATION,4,0);

    }


    public int getPositionAttributeLocation() {
        return A_POSITION_LOCATION;
    }
}
