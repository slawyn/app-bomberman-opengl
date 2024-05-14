package main.rendering.elements;

import java.util.Vector;

import main.game.events.EventObject;
import main.nativeclasses.GameElement;
import main.rendering.color.ColorShaderProgram;
import main.rendering.VertexArray;

public class DebugElement extends RenderElement
{

    private Vector<VertexArray> vertexData;
    public DebugElement(EventObject go, float scale)
    {
        super();
        super.init(go.getType(), go.getSubtype(), go.getState(), go.getId());
        this.vertexData = new Vector<>();

        int[][] hitboxes = go.getHitboxes();
        for(int idx = 0;idx<hitboxes.length;idx++)
        {
            int[] box1 = hitboxes[idx];
            float[] data0 = new float[12];


            /* first triangle
            // top right
            data0[0] = (0 + box1[2]) * scale;
            data0[1] = 0 * scale;

            // top left
            data0[2] = 0 * scale;
            data0[3] = 0 * scale;

            // bottom left
            data0[4] = 0 * scale;
            data0[5] = (0 + box1[3]) * scale;

            // second triangle
            data0[6] = (0 + box1[2]) * scale;
            data0[7] = 0 * scale;

            data0[8] = 0 * scale;
            data0[9] = (0 + box1[3]) * scale;

            data0[10] = (0 + box1[2]) * scale;
            data0[11] = (0 + box1[3]) * scale;
            */

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
