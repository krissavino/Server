package Server;

import Server.Interfaces.IThreadController;

public final class ThreadController implements IThreadController
{
    private boolean cancellationRequested = false;

    public boolean isCancellationRequested() {
        return cancellationRequested;
    }

    public void Cancel() {
        cancellationRequested = true;
    }

    public void reset() {
        cancellationRequested = false;
    }
}
