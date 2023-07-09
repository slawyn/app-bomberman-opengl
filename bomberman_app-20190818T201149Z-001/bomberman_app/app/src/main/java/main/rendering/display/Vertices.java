package main.rendering.display;

import static main.game.SceneManager.*;
import static main.nativeclasses.GameElement.*;

import android.util.SparseArray;

import main.rendering.VertexArray;

public class Vertices {
    private SparseArray<VertexArray> mVertexData;
    public Vertices(float scalefactor)
    {

        float vertices78x78[] = {
                78f, 0f, 1f, 0f,
                0, 0f, 0f, 0f,
                0, 78f, 0f, 1f,
                78f, 0f, 1f, 0f,
                0f, 78f, 0f, 1f,
                78f, 78f, 1f, 1f};


        float vertices136x136[] = {// Triangle Fan
                136, 0f, 1f, 0f,
                0, 0f, 0f, 0f,
                0, 136f, 0f, 1f,
                136f, 0f, 1f, 0f,
                0f, 136f, 0f, 1f,
                136f, 136f, 1f, 1f};

        float vertices150x100[] = {// Triangle Fan
                150f, 0f, 1f, 0f,
                0, 0f, 0f, 0f,
                0, 100f, 0f, 1f,
                150f, 0f, 1f, 0f,
                0f, 100f, 0f, 1f,
                150f, 100f, 1f, 1f};

        // Here we define surfaces for objects
        // these get recalculated on load
        float vertices180x180[] = {// Triangle Fan
                180f, 0f, 1f, 0f,
                0, 0f, 0f, 0f,
                0, 180f, 0f, 1f,
                180f, 0f, 1f, 0f,
                0f, 180f, 0f, 1f,
                180f, 180f, 1f, 1f};


        float vertices300x300[] = {// Triangle Fan
                300f, 0f, 1f, 0f,
                0, 0f, 0f, 0f,
                0, 300f, 0f, 1f,
                300f, 0f, 1f, 0f,
                0f, 300f, 0f, 1f,
                300f, 300f, 1f, 1f};

        float vertices600x600[] = {// Triangle Fan
                600f, 0f, 1f, 0f,
                0, 0f, 0f, 0f,
                0, 600f, 0f, 1f,
                600f, 0f, 1f, 0f,
                0f, 600f, 0f, 1f,
                600f, 600f, 1f, 1f};


        float vertices524x200[] = {// Triangle Fan
                524f, 0f, 1f, 0f,
                0, 0f, 0f, 0f,
                0, 200f, 0f, 1f,
                524f, 0f, 1f, 0f,
                0f, 200f, 0f, 1f,
                524f, 200f, 1f, 1f};

        float[] verticesFullscreen = {
                // Order of coordinates: X, Y, S, T
                1920f, 0f, 1f, 0f,
                0, 0f, 0f, 0f,
                0, 1080f, 0f, 1f,
                1920f, 0f, 1f, 0f,
                0f, 1080f, 0f, 1f,
                1920f, 1080f, 1f, 1f};


        /*Rescale vertices*/
        vertices78x78[0] = vertices78x78[0] * scalefactor;
        vertices78x78[9] = vertices78x78[9] * scalefactor;
        vertices78x78[12] = vertices78x78[12] * scalefactor;
        vertices78x78[17] = vertices78x78[17] * scalefactor;
        vertices78x78[20] = vertices78x78[20] * scalefactor;
        vertices78x78[21] = vertices78x78[21] * scalefactor;

        vertices136x136[0] = vertices136x136[0] * scalefactor;
        vertices136x136[9] = vertices136x136[9] * scalefactor;
        vertices136x136[12] = vertices136x136[12] * scalefactor;
        vertices136x136[17] = vertices136x136[17] * scalefactor;
        vertices136x136[20] = vertices136x136[20] * scalefactor;
        vertices136x136[21] = vertices136x136[21] * scalefactor;

        vertices150x100[0] = vertices150x100[0] * scalefactor;
        vertices150x100[9] = vertices150x100[9] * scalefactor;
        vertices150x100[12] = vertices150x100[12] * scalefactor;
        vertices150x100[17] = vertices150x100[17] * scalefactor;
        vertices150x100[20] = vertices150x100[20] * scalefactor;
        vertices150x100[21] = vertices150x100[21] * scalefactor;

        vertices180x180[0] = vertices180x180[0] * scalefactor;
        vertices180x180[9] = vertices180x180[9] * scalefactor;
        vertices180x180[12] = vertices180x180[12] * scalefactor;
        vertices180x180[17] = vertices180x180[17] * scalefactor;
        vertices180x180[20] = vertices180x180[20] * scalefactor;
        vertices180x180[21] = vertices180x180[21] * scalefactor;

        vertices300x300[0] = vertices300x300[0] * scalefactor;
        vertices300x300[9] = vertices300x300[9] * scalefactor;
        vertices300x300[12] = vertices300x300[12] * scalefactor;
        vertices300x300[17] = vertices300x300[17] * scalefactor;
        vertices300x300[20] = vertices300x300[20] * scalefactor;
        vertices300x300[21] = vertices300x300[21] * scalefactor;

        vertices600x600[0] = vertices600x600[0] * scalefactor;
        vertices600x600[9] = vertices600x600[9] * scalefactor;
        vertices600x600[12] = vertices600x600[12] * scalefactor;
        vertices600x600[17] = vertices600x600[17] * scalefactor;
        vertices600x600[20] = vertices600x600[20] * scalefactor;
        vertices600x600[21] = vertices600x600[21] * scalefactor;

        vertices524x200[0] = vertices524x200[0] * scalefactor;
        vertices524x200[9] = vertices524x200[9] * scalefactor;
        vertices524x200[12] = vertices524x200[12] * scalefactor;
        vertices524x200[17] = vertices524x200[17] * scalefactor;
        vertices524x200[20] = vertices524x200[20] * scalefactor;
        vertices524x200[21] = vertices524x200[21] * scalefactor;

        verticesFullscreen[0] = verticesFullscreen[0] * scalefactor;
        verticesFullscreen[9] = verticesFullscreen[9] * scalefactor;
        verticesFullscreen[12] = verticesFullscreen[12] * scalefactor;
        verticesFullscreen[17] = verticesFullscreen[17] * scalefactor;
        verticesFullscreen[20] = verticesFullscreen[20] * scalefactor;
        verticesFullscreen[21] = verticesFullscreen[21] * scalefactor;


        /* Load into float buffers */
        VertexArray vert78x78 = new VertexArray(vertices78x78);
        VertexArray vert136x136 = new VertexArray(vertices136x136);
        VertexArray vert150x100 = new VertexArray(vertices150x100);
        VertexArray vert180x180 = new VertexArray(vertices180x180);
        VertexArray vert300x300 = new VertexArray(vertices300x300);
        VertexArray vert600x600 = new VertexArray(vertices600x600);
        VertexArray vert524x200 = new VertexArray(vertices524x200);
        VertexArray vertFullscreen = new VertexArray(verticesFullscreen);

        /* Assign objects */
        mVertexData = new SparseArray<>();
        mVertexData.put(OBJ_CRATE, vert136x136);
        mVertexData.put(OBJ_BLOCK, vert136x136);
        mVertexData.put(OBJ_ITEM, vert136x136);
        mVertexData.put(OBJ_BOMB, vert136x136);
        mVertexData.put(OBJ_EXPLN, vert150x100);
        mVertexData.put(OBJ_PLAYR, vert180x180);
        mVertexData.put(SOBJ_TOUCH, vert300x300);
        mVertexData.put(SOBJ_BUTTON, vert524x200);
        mVertexData.put(SOBJ_BACKGROUND, vertFullscreen);
        mVertexData.put(SOBJ_BACKGROUND_LOADING, vert600x600);
        mVertexData.put(SOBJ_FPS_COUNTER, vert78x78);
        mVertexData.put(SOBJ_TIMER, vert78x78);
    }
    public VertexArray getQuad(int key)
    {
        return mVertexData.get(key);
    }
}
