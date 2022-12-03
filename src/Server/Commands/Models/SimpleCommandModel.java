package Server.Commands.Models;

import Server.Commands.Interfaces.ISimpleCommand;

public class SimpleCommandModel implements ISimpleCommand
{
    protected String Name = "None";

    public String getCommandName() {
        return Name;
    }
}


