package main.rendering.elements;

import java.util.Vector;

import main.nativeclasses.GameElement;
import main.rendering.color.ColorShaderProgram;
import main.rendering.VertexArray;

public class DebugElement extends RenderElement
{

    private Vector<VertexArray> vertexData;
    public DebugElement(GameElement go, float scale)
    {
        super();
        super.init(go.mObjectType, go.mObjectSubtype, go.getState(), go.getUniqeueID());
        this.vertexData = new Vector<>();

        int[][] hitboxes = go.getHitBoxes();
        for(int idx = 0;idx<hitboxes.length;idx++)
        {
            int[] box1 = hitboxes[idx];
            float[] data0 = new float[12];

            /*
            jiHitboxes[0] = xHitbox.i16OffsetX;
            jiHitboxes[1] = xHitbox.i16OffsetY;
            jiHitboxes[2] = xHitbox.i16HalfSizeX;
            jiHitboxes[3] = xHitbox.i16HalfSizeY;
            jiHitboxes[4] = xHitbox.i16Left;
            jiHitboxes[5] = xHitbox.i16Bottom;
            jiHitboxes[6] = xHitbox.i16Right;
            jiHitboxes[7] = xHitbox.i16Top;
            */

            /*
            // first triangle
            data0[0] = (box1[0] + box1[2]) * scale;
            data0[1] = box1[1] * scale;

            data0[2] = box1[0] * scale;
            data0[3] = box1[1] * scale;

            data0[4] = box1[0] * scale;
            data0[5] = (box1[1] + box1[3]) * scale;

            // second triangle
            data0[6] = (box1[0] + box1[2]) * scale;
            data0[7] = box1[1] * scale;

            data0[8] = box1[0] * scale;
            data0[9] = (box1[1] + box1[3]) * scale;

            data0[10] = (box1[0] + box1[2]) * scale;
            data0[11] = (box1[1] + box1[3]) * scale;
            */

            /* first triangle */
            // top right
            data0[0] = (0 + box1[2]) * scale;
            data0[1] = 0 * scale;

            // top left
            data0[2] = 0 * scale;
            data0[3] = 0 * scale;

            // bottom left
            data0[4] = 0 * scale;
            data0[5] = (0 + box1[3]) * scale;

            /* second triangle */
            data0[6] = (0 + box1[2]) * scale;
            data0[7] = 0 * scale;

            data0[8] = 0 * scale;
            data0[9] = (0 + box1[3]) * scale;

            data0[10] = (0 + box1[2]) * scale;
            data0[11] = (0 + box1[3]) * scale;

            /* first triangle */
            // top right
            data0[0] = (box1[2]) * scale;
            data0[1] = -box1[3] * scale;

            // top left
            data0[2] = -box1[2] * scale;
            data0[3] = -box1[3] * scale;

            // bottom left
            data0[4] = -box1[2] * scale;
            data0[5] = (box1[3]) * scale;

            /* second triangle */
            // top right
            data0[6] = (box1[2]) * scale;
            data0[7] = -box1[3] * scale;

            // bottom left
            data0[8] = -box1[2] * scale;
            data0[9] = (box1[3]) * scale;

            // bottom right
            data0[10] = (box1[2]) * scale;
            data0[11] = (box1[3]) * scale;


            vertexData.add(new VertexArray(data0));
        }
    }

    public int getNumberOfObjects()
    {
        return vertexData.size();
    }

    public void bindDebugData(int nm, ColorShaderProgram debugprogram)
    {
        vertexData.get(nm).setVertexAttribPointer(
                0,
                debugprogram.getPositionAttributeLocation(),
                2,
                8);
    }
}
