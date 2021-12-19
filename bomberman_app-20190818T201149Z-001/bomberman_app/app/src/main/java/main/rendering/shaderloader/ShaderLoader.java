package main.rendering.shaderloader;

import main.Logger;

import static android.opengl.GLES20.*;

public class ShaderLoader {
    private static final String TAG = "ShaderLoader";

    public static int compileVertexShader(String shaderCode) {
        return compileShader(GL_VERTEX_SHADER, shaderCode);

    }

    public static int compileFragmentShader(String shaderCode) {
        return compileShader(GL_FRAGMENT_SHADER, shaderCode);
    }


    // Compiling Shader Object
    private static int compileShader(int type, String shaderCode) {
        final int shaderObjectId = glCreateShader(type);

        if (shaderObjectId == 0) {
            Logger.log(Logger.ERROR,TAG, "Failed to create shaderObjectId");
            return 0;
        }

        glShaderSource(shaderObjectId,shaderCode);
        glCompileShader(shaderObjectId);

        final int[]compileStatus=new int[1];
        glGetShaderiv(shaderObjectId,GL_COMPILE_STATUS,compileStatus,0);
        //Print the shader info log to the Android log output
        Logger.log(Logger.DEBUG,TAG,"Results of compiling source:"+"\n"+glGetShaderInfoLog(shaderObjectId));//shaderCode+"\n:"+glGetShaderInfoLog(shaderObjectId));

        if(compileStatus[0]==0){
            glDeleteShader(shaderObjectId);
            Logger.log(Logger.ERROR,TAG,"Compilationofshaderfailed.");
            return 0;
        }

        return shaderObjectId;
    }

    public static int linkProgram(int vertexShaderId, int fragmentShaderId){
        final int programObjectId = glCreateProgram();
        if(programObjectId == 0){
            Logger.log(Logger.ERROR,TAG, "Could not create new PROGRAM");
            return 0;
        }
        glAttachShader(programObjectId, vertexShaderId);
        glAttachShader(programObjectId, fragmentShaderId);

        glLinkProgram(programObjectId);

        final int [] linkStatus = new int[1];
        glGetProgramiv(programObjectId, GL_LINK_STATUS, linkStatus, 0);

        Logger.log(Logger.DEBUG,TAG, "Results of linking PROGRAM:\n"+glGetProgramInfoLog(programObjectId));

        if(linkStatus[0] == 0){

            glDeleteProgram(programObjectId);
            return 0;
        }

        return programObjectId;
    }

    public static boolean validateProgram(int programObjectId){
        glValidateProgram(programObjectId);

        final int[] validateStatus = new int[1];
        glGetProgramiv(programObjectId, GL_VALIDATE_STATUS, validateStatus,0);

        Logger.log(Logger.DEBUG,TAG,"Results of validating PROGRAM: "+validateStatus[0]+"\nLog:"+glGetProgramInfoLog(programObjectId));

        return validateStatus[0] != 0;
    }

    public static int buildProgram(String vertexShaderSource,
                                   String fragmentShaderSource) {
        int program;

        // Compile the raw.
        int vertexShader = compileVertexShader(vertexShaderSource);
        int fragmentShader = compileFragmentShader(fragmentShaderSource);

        // Link them into a shader PROGRAM.
        program = linkProgram(vertexShader, fragmentShader);

        validateProgram(program);
        return program;
    }
}
