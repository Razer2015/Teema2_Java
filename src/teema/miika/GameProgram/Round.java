package teema.miika.GameProgram;

import java.util.ArrayList;
import java.util.List;

public class Round {
    public List<Match> Matches;
    public RoundPenalty RoundPenalties;

    public Round(int teamCount) {
        this.Matches = new ArrayList<>();
        this.RoundPenalties = new RoundPenalty(teamCount);
    }

    /// <summary>
    ///     Reset and count round penalties
    /// </summary>
    public void countPenalties() {
        RoundPenalties.resetPenalties();
        RoundPenalties.countRoundMatchPenalties(Matches);
    }

    /// <summary>
    ///     Get total penalties count for a round
    /// </summary>
    /// <returns></returns>
    public int getTotalPenalties() {
        return RoundPenalties.getTotalPenalties();
    }

    /// <summary>
    ///     Get total match penalties
    /// </summary>
    public int getMatchPenalties(int home, int visitor) {
        return RoundPenalties.getMatchPenalties(home, visitor);
    }
}
