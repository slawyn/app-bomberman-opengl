package main.rendering;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glVertexAttribPointer;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class VertexArray
{
    private final FloatBuffer FLOAT_BUFFER;
    public VertexArray(float[] vertexData)
    {
        FLOAT_BUFFER = ByteBuffer
                .allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);

    }

    public void setVertexAttribPointer(int dataOffset, int attributeLocation, int componentCount, int stride)
    {
        FLOAT_BUFFER.position(dataOffset);
        glVertexAttribPointer(attributeLocation, componentCount, GL_FLOAT,
                false, stride, FLOAT_BUFFER);
        glEnableVertexAttribArray(attributeLocation);

        FLOAT_BUFFER.position(0);
    }
}
