package Server.Interfaces;

import java.io.BufferedReader;
import java.net.Socket;

public interface IClient
{
    Socket getSocket();
    boolean sendMessage(String message);
    BufferedReader getBufferedReader();
}
