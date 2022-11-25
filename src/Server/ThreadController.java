package Server;

import Server.Interfaces.IThreadController;

public class ThreadController implements IThreadController
{
    private boolean cancelationRequested = false;

    public boolean isCancellationRequested() {
        return cancelationRequested;
    }

    public void Cancel() {
        cancelationRequested = true;
    }

    public void reset() {
        cancelationRequested = false;
    }
}
