package teema.miika.GameProgram;

import java.util.List;

public class RoundPenalty {
    final int _teamCount;
    Penalty[] _teamPenalties;

    public RoundPenalty(int teamCount) {
        this._teamCount = teamCount;
        this._teamPenalties = new Penalty[0];
    }

    /// <summary>
    ///     Reset round penalties
    /// </summary>
    public void resetPenalties() {
        _teamPenalties = new Penalty[_teamCount];
        for (int i = 0; i < _teamPenalties.length; i++) {
            _teamPenalties[i] = new Penalty(i, 0);
        }
    }

    /// <summary>
    ///     Count penalties for a round
    /// </summary>
    public void countRoundMatchPenalties(List<Match> matches) {
        for (int i = 0; i < _teamPenalties.length; i++) {
            Penalty penalty = _teamPenalties[i];

            int occurences = getMatchesCount(matches, i);
            if (occurences != 1) {
                if (occurences < 1) {
                    penalty.Penalties++;
                }
                else {
                    penalty.Penalties += occurences - 1;
                }
                _teamPenalties[i] = penalty;
            }
        }
    }

    /// <summary>
    ///     Count how many times teamId is found in the matches list (either home or visitor)
    /// </summary>
    private int getMatchesCount(List<Match> matches, int teamId){
        int occurences = 0;
        for (int i = 0; i < matches.size(); i++) {
            Match match = matches.get(i);
            if (match.Home == teamId || match.Visitor == teamId)
                occurences++;
        }
        return occurences;
    }

    /// <summary>
    ///     Get total penalties of a round
    /// </summary>
    public int getTotalPenalties() {
        int penalties = 0;
        for (int i = 0; i < _teamPenalties.length; i++) {
            penalties += _teamPenalties[i].Penalties;
        }
        return penalties;
    }

    /// <summary>
    ///     Get match penalties
    /// </summary>
    public int getMatchPenalties(int home, int visitor) {
        return _teamPenalties[home].Penalties + _teamPenalties[visitor].Penalties;
    }
}
