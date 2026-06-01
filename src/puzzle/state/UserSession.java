package puzzle.state;

// State pattern context - holds the current DifficultyState and delegates to it
public class UserSession {

    private DifficultyState currentState;
    private int totalPuzzlesSolved = 0;
    private int totalScore = 0;
    private String lastTransitionMessage = "";

    public UserSession() { this.currentState = DifficultyState.create(); }

    public DifficultyState getCurrentState()   { return currentState; }
    public int getTotalPuzzlesSolved()         { return totalPuzzlesSolved; }
    public int getTotalScore()                 { return totalScore; }
    public String getLastTransitionMessage()   { return lastTransitionMessage; }
    public int getHintCount()                  { return currentState.getHintCount(); }
    public int getScoreMultiplier()            { return currentState.getScoreMultiplier(); }

    // Evaluates performance based on time taken (seconds) rather than a countdown
    public boolean evaluateAndTransition(int score, int secondsElapsed) {
        totalPuzzlesSolved++;
        totalScore += score;

        int event = toEvent(secondsElapsed);
        DifficultyState previous = currentState;
        currentState = currentState.processEvent(event);

        boolean transitioned = !previous.getName().equals(currentState.getName());
        lastTransitionMessage = transitioned
                ? previous.getName() + " -> " + currentState.getName()
                : "Stayed at " + currentState.getName();
        return transitioned;
    }

    // Maps elapsed time to an event using the current state's thresholds
    private int toEvent(int secondsElapsed) {
        int fast   = currentState.getFastThreshold();
        int normal = currentState.getNormalThreshold();
        if (secondsElapsed <= fast)   return DifficultyState.PERFECT;
        if (secondsElapsed <= normal) return DifficultyState.SOLVED_FAST;
        if (secondsElapsed <= normal * 2) return DifficultyState.SOLVED_NORMAL;
        return DifficultyState.SOLVED_SLOW;
    }
}