package main.rendering.animation;

import java.util.Vector;

import main.game.Hitbox;
import main.game.GameObject;
import main.rendering.color.ColorShaderProgram;
import main.rendering.VertexArray;

public class DebugObject extends RenderObject
{

    private Vector<VertexArray> vertexData;
    DebugObject(GameObject gameObject, float scale)
    {
        super();
        super.init(gameObject.mObjectType);
        this.vertexData = new Vector<>();

        for(int idx = 0;idx<gameObject.mBoxes.length;idx++){

            Hitbox box1 = gameObject.mBoxes[idx];
            float[] data0 = new float[12];
            data0[0] = (box1.offsetX + box1.sizeX) * scale;
            data0[1] = box1.offsetY * scale;

            data0[2] = box1.offsetX * scale;
            data0[3] = box1.offsetY * scale;

            data0[4] = box1.offsetX * scale;
            data0[5] = (box1.offsetY + box1.sizeY) * scale;

            // second triangle
            data0[6] = (box1.offsetX + box1.sizeX) * scale;
            data0[7] = box1.offsetY * scale;

            data0[8] = box1.offsetX * scale;
            data0[9] = (box1.offsetY + box1.sizeY) * scale;

            data0[10] = (box1.offsetX + box1.sizeX) * scale;
            data0[11] = (box1.offsetY + box1.sizeY) * scale;
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
