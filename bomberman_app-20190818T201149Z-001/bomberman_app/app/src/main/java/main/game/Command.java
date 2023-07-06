package main.game;

public class Command
{
    public byte mTick;
    public byte mPlayerInput;
    public boolean processed;

    public Command(){}

    public Command(byte tick, byte input)
    {
        mTick = tick;
        mPlayerInput = input;
    }
}
