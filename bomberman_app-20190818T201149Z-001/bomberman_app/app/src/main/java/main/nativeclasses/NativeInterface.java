package main.nativeclasses;

public class NativeInterface
{
    static {
        System.loadLibrary("NativeInterface");
    }


    public native static long initFreeType();
}
