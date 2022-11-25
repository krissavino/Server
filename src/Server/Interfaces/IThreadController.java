package Server.Interfaces;

public interface IThreadController
{
    boolean isCancellationRequested();

    void Cancel();

    void reset();
}
