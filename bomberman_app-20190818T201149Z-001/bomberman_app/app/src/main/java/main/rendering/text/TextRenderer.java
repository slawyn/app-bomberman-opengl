package main.rendering.text;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.opengl.GLES20;

import main.rendering.VertexArray;
import main.rendering.texture.TextureLoader;
import main.rendering.texture.TextureShaderProgram;

import static android.opengl.GLES10.glActiveTexture;
import static android.opengl.GLES10.glBindTexture;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glUniform1i;
//https://learnopengl.com/In-Practice/Text-Rendering
public class TextRenderer
{

    private Bitmap bitmap;
    private Canvas canvas;
    private Paint textPaint;
    private VertexArray mStatusAreaArray;


    public int[] textures;
    public int textIndex;
    public static float mStatusBarVertices[] = {// Triangle Fan
            136, 0f, 1f, 0f,
            0, 0f, 0f, 0f,
            0, 136f, 0f, 1f,
            136f, 0f, 1f, 0f,
            0f, 136f, 0f, 1f,
            136f, 136f, 1f, 1f
    };

    public TextRenderer()
    {
        // Create an empty, mutable bitmap
        bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
        textPaint = new Paint();
        textPaint.setColor(Color.GREEN);
        textPaint.setTextSize(32);
        canvas = new Canvas(bitmap);

        mStatusAreaArray = new VertexArray(mStatusBarVertices);
        textures = new int[1000];
        textIndex = 0;
        // textPaint.setAntiAlias(true);
    }

    public int texturizeText(String text){
        bitmap.eraseColor(0);
        canvas.drawText(text, 16,16, textPaint);

        int tex = TextureLoader.loadTexture(bitmap);
        textures[textIndex] = tex;
        ++textIndex;
        return tex;
    }

    public void loadGlyphs(){

    }

    public void draw(TextureShaderProgram program){
        mStatusAreaArray.setVertexAttribPointer(
                0,
                program.getPositionAttributeLocation(),
                2,
                16);

        mStatusAreaArray.setVertexAttribPointer(
                2,
                program.getTextureCoordinatesAttributeLocation(),
                2,
                16);


        glDrawArrays(GL_TRIANGLE_FAN, 0, 6);

        // TODO this approach sucks ass, creating and remove textures lags the game
        if(textIndex==1000){
            glDeleteTextures(1000,textures,0);
            textIndex = 0;
        }


    }
}
