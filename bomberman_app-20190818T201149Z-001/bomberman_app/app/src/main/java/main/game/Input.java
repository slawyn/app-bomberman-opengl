package main.game;

public class Input
{
    public byte mTick;
    public byte mPlayerInput;
    public boolean processed;

    public Input(){}

    public Input(byte tick, byte input)
    {
        mTick = tick;
        mPlayerInput = input;
    }
}
