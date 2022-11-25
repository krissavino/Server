package Server.Commands.Models;

import Server.Commands.Interfaces.ISimpleCommand;

public class SimpleCommandModel implements ISimpleCommand
{
    protected String Name = "None";

    public String getName() {
        return Name;
    }
}


