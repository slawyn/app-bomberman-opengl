package main.game.events;

public abstract class EventObject
{
    public abstract int getState();
    public abstract int getId();

    public abstract int getSubtype();
    public abstract int getType();
}
