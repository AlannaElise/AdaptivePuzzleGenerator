package puzzle.scheduler;

import puzzle.core.Puzzle;
import puzzle.factory.PuzzleFactory;

import java.util.ArrayList;
import java.util.function.Consumer;

// Scheduler pattern - controls the order in which threads access the puzzle generator.
// Uses enter() and done() to manage token-based access to the shared resource.
public class PuzzleScheduler {

    private final PuzzleFactory factory = new PuzzleFactory();
    private Thread runningThread = null;
    private final ArrayList<Thread> waitingThreads    = new ArrayList<>();
    private final ArrayList<ScheduleOrdering> waitingRequests = new ArrayList<>();
    private Consumer<String> statusListener;

    public void setStatusListener(Consumer<String> listener) {
        this.statusListener = listener;
    }

    public void start() { /* No dedicated thread needed - threads spawn per request */ }

    public void stop() {
        synchronized (this) {
            for (Thread t : waitingThreads) t.interrupt();
            waitingThreads.clear();
            waitingRequests.clear();
            runningThread = null;
        }
    }

    public void schedule(String puzzleType, PuzzleFactory.Difficulty difficulty,
                         GenerationRequest.Priority priority, String description,
                         Consumer<Puzzle> callback) {
        GenerationRequest request = new GenerationRequest(
                puzzleType, difficulty, priority, description, callback);
        notifyStatus("Queued: " + description);

        Thread t = new Thread(() -> {
            try {
                enter(request);
                try {
                    notifyStatus("Generating: " + description);
                    Thread.sleep(priority == GenerationRequest.Priority.LOW ? 600 : 200);
                    Puzzle puzzle = factory.createPuzzle(puzzleType, difficulty);
                    notifyStatus("Ready: " + description);
                    javax.swing.SwingUtilities.invokeLater(() -> callback.accept(puzzle));
                } finally {
                    done();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Scheduler-" + description);
        t.setDaemon(true);
        t.start();
    }

    private void enter(ScheduleOrdering request) throws InterruptedException {
        Thread thisThread = Thread.currentThread();
        synchronized (this) {
            if (runningThread == null) {
                runningThread = thisThread;
                return;
            }
            waitingThreads.add(thisThread);
            waitingRequests.add(request);
        }
        synchronized (thisThread) {
            while (thisThread != runningThread)
                thisThread.wait();
        }
        synchronized (this) {
            int i = waitingThreads.indexOf(thisThread);
            if (i >= 0) {
                waitingThreads.remove(i);
                waitingRequests.remove(i);
            }
        }
    }

    private synchronized void done() {
        int n = waitingThreads.size();
        if (n <= 0) {
            runningThread = null;
        } else if (n == 1) {
            runningThread = waitingThreads.get(0);
            synchronized (runningThread) { runningThread.notifyAll(); }
        } else {
            int next = 0;
            ScheduleOrdering best = waitingRequests.get(0);
            for (int i = 1; i < n; i++) {
                if (waitingRequests.get(i).scheduleBefore(best)) {
                    next = i;
                    best = waitingRequests.get(i);
                }
            }
            runningThread = waitingThreads.get(next);
            synchronized (runningThread) { runningThread.notifyAll(); }
        }
    }

    private void notifyStatus(String msg) {
        if (statusListener != null)
            javax.swing.SwingUtilities.invokeLater(() -> statusListener.accept(msg));
    }
}