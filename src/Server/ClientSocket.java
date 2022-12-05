package Server;

import Server.Interfaces.IClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public final class ClientSocket implements IClient
{
    private final Socket Socket;
    private final BufferedReader BufferedReader;
    private final PrintWriter BufferedWriter;

    ClientSocket(Socket socket)
    {
        this.Socket = socket;

        try
        {
            BufferedReader = new BufferedReader(new InputStreamReader(this.Socket.getInputStream()));
            BufferedWriter = new PrintWriter(this.Socket.getOutputStream(), true);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public Socket getSocket()
    {
        return Socket;
    }

    public boolean sendMessage(String message)
    {
        if(!Socket.isConnected())
            return false;

        BufferedWriter.println(message);

        return true;
    }

    public BufferedReader getBufferedReader() {
        return BufferedReader;
    }
}
