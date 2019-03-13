package teema.miika.GameProgram;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameProgram {
    private final String _scheduleFile = "gameSchedule.txt";
    static Random rand = new Random();
    int _teamCount;
    Round[] _rounds;
    TabuList _tabuList;
    int _totalMoves = 0;

    public GameProgram(int teamCount) {
        _teamCount = teamCount;

        int giveUp = 10;
        while (giveUp-- > 0) {
            initGames();

            int totalPenalty = getTotalPenalty();

            System.out.println("Start penalties: " + totalPenalty);

            if (totalPenalty == 0) {
                return;
            }

            Match firstMove = getFirstMove();
            int round = moveToBestRound(firstMove);
            int moves = 1;
            while (moves <= 1000) {
                Match worstMatch = getWorstMatch(round);
                if (worstMatch == null) break;
                round = moveToBestRound(worstMatch);
                moves++;

                if (moves % 10 == 0)
                    System.out.println("Penalties after " + moves + " moves: " + getTotalPenalty());

                if (getTotalPenalty() <= 0) break;
            }

            if (getTotalPenalty() <= 0) {
                System.out.println();
                System.out.println("Success with " + _totalMoves + " moves");
                saveGame();
                printGame();
                break;
            }
            System.out.println(getTotalPenalty());
            System.out.println("Attempt failed");
        }
    }

    /// <summary>
    ///     Initialize the games list by setting the games in random rounds
    ///     Also counts the penalties after that
    /// </summary>S
    private void initGames() {
        _totalMoves = 0;
        _tabuList = new TabuList();
        _rounds = new Round[_teamCount % 2 == 0 ? (_teamCount - 1) * 2 : _teamCount * 2];
        for (int i = 0; i < _rounds.length; i++) {
            _rounds[i] = new Round(_teamCount);
        }

        for (int i = 0; i <= _teamCount; i++) {
            for (int y = i + 1; y <= _teamCount - 1; y++) {
                // Home game
                List<Round> unfilledRounds = getUnfilledRounds();
                int random = rand.nextInt(unfilledRounds.size());
                Round round = unfilledRounds.get(random);
                round.Matches.add(new Match(i, y));

                // Visitor game
                unfilledRounds = getUnfilledRounds();
                random = rand.nextInt(unfilledRounds.size());
                round = unfilledRounds.get(random);
                round.Matches.add(new Match(y, i));
/*
                int random = rand.nextInt(_rounds.length);
                Round round = _rounds[random];
                round.Matches.add(new Match(i, y));
                random = rand.nextInt(_rounds.length);
                round = _rounds[random];
                round.Matches.add(new Match(y, i));*/
            }
        }

        // Count penalties
        for (int i = 0; i < _rounds.length; i++)
            _rounds[i].countPenalties();
    }

    /// <summary>
    ///     Get the first game move by random
    /// </summary>
    private Match getFirstMove() {
        while (true) {
            int roundCandidate = rand.nextInt(_rounds.length);
            Round round = _rounds[roundCandidate];
            List<Match> matches = getAvailableMatches(round.Matches);
            if (matches.size() >= 1) {
                return matches.get(rand.nextInt(matches.size()));
            }
        }
    }

    /// <summary>
    ///     Select the worst match of the chosen round (exclude previous move)
    /// </summary>
    private Match getWorstMatch(int roundId) {
        Round round = _rounds[roundId];
        List<Match> matches = getAvailableMatches(round.Matches);
        // <Match, Penalties>
        List<Pair<Match, Integer>> matchList = new ArrayList<>();
        for (int i = 0; i < matches.size(); i++){
            Match match = matches.get(i);
            matchList.add(new Pair(match, round.getMatchPenalties(match.Home, match.Visitor)));
        }

        if (matchList.size() <= 0) {
            return null;
            //return GetFirstMove();
        }

        List<Pair<Match, Integer>> maxs = getMaximumMatchPenalty(matchList);
        return maxs.get(rand.nextInt(maxs.size())).getLeft();
    }

    /// <summary>
    ///     Get total penalties of the game (all rounds combined)
    /// </summary>
    /// <returns></returns>
    private int getTotalPenalty() {
        int penaltyCount = 0;
        for (int i = 0; i < _rounds.length; i++) {
            penaltyCount += _rounds[i].RoundPenalties.getTotalPenalties();
        }
        return penaltyCount;
    }

    /// <summary>
    ///     Move the chosen match to the round that gives the lowest total penalty count
    /// </summary>
    private int moveToBestRound(Match match) {
        int totalPenaltyBefore = getTotalPenalty();

        // Remove the round about to be moved and count the penalties again
        int roundId = -1;
        for (int i = 0; i < _rounds.length; i++) {
            if (_rounds[i].Matches.contains(match)) {
                roundId = i;
                _rounds[i].Matches.remove(match);
                break;
            }
        }
        _rounds[roundId].countPenalties();

        List<Pair<Integer, Integer>> penaltys = new ArrayList<>();
        for (int i = 0; i < _rounds.length; i++) {
            if (i == roundId) {
                continue;
            }

            Round testRound = _rounds[i];
            testRound.Matches.add(match);
            testRound.countPenalties();

            penaltys.add(new Pair(i, getTotalPenalty()));

            testRound.Matches.remove(match);
            testRound.countPenalties();
        }

        // <Round, Penalties>
        List<Pair<Integer, Integer>> mins = getMinimumRoundPenalty(penaltys);
        int bestRound = mins.get(rand.nextInt( mins.size())).getLeft();

        moveRound(roundId, bestRound, match);

        return bestRound;
    }

    /// <summary>
    ///     Move round from round to another
    /// </summary>
    private Round moveRound(int prevRound, int newRound, Match match) {
        // Remove from old (if exists)
        _rounds[prevRound].Matches.remove(match);
        _rounds[prevRound].countPenalties();

        // Add to new
        _rounds[newRound].Matches.add(match);
        _rounds[newRound].countPenalties();

        // Add to tabulist
        _tabuList.add(match);

        _totalMoves++;

        return _rounds[newRound];
    }

    /// <summary>
    ///     Print the game result
    /// </summary>
    private void printGame() {
        //System.out.println("0 0 0");
        //System.out.println("0 0 0");

        for (int i = 0; i < _rounds.length; i++) {
            List<Match> matches = _rounds[i].Matches;
            for (int y = 0; y < matches.size(); y++) {
                Match match = matches.get(y);

                System.out.println((i + 1) + " " + (match.Home + 1) + " " + (match.Visitor + 1));
            }
        }
    }

    /// <summary>
    ///     Save the game results to a file
    /// </summary>
    private void saveGame() {
        try {
            PrintWriter bw = new PrintWriter(new FileWriter(_scheduleFile));

            // Kaikki virheet | Hard virheet | Soft virheet
            bw.print("0 0 0");
            bw.println();
            // Pelatut virheet | Kotiesto virheet | Vierasesto virheet
            bw.print("0 0 0");
            bw.println();

            for (int i = 0; i < _rounds.length; i++) {
                List<Match> matches = _rounds[i].Matches;
                for (int y = 0; y < matches.size(); y++) {
                    Match match = matches.get(y);

                    // Kierros | Koti joukkue | Vieras joukkue
                    bw.print((i + 1));
                    bw.print(" ");
                    bw.print((match.Home + 1));
                    bw.print(" ");
                    bw.print((match.Visitor + 1));
                    bw.println();
                }
            }

            bw.close();

            System.out.println("GameSchedule written to: " + _scheduleFile);
        }
        catch (IOException ioException) {
            System.out.println("Error writing the gameSchedule.txt");
            System.out.println(ioException.getMessage());
        }
    }


    private List<Round> getUnfilledRounds(){
        List<Round> rounds = new ArrayList<>();
        for (int i = 0; i < _rounds.length; i++) {
            if (_rounds[i].Matches.size() < _teamCount / 2)
                rounds.add(_rounds[i]);
        }
        return rounds;
    }

    private List<Match> getAvailableMatches(List<Match> matches){
        List<Match> availableMatches = new ArrayList<>();
        for (int i = 0; i < matches.size(); i++) {
            if (!_tabuList.contains(matches.get(i)))
                availableMatches.add(matches.get(i));
        }
        return availableMatches;
    }

    private List<Pair<Integer, Integer>> getMinimumRoundPenalty(List<Pair<Integer, Integer>> input){
        int minimum = 9999;
        for (int i = 0; i < input.size(); i++) {
            int penaltyCount = input.get(i).getRight();
            if (penaltyCount < minimum) minimum = penaltyCount;
        }
        List<Pair<Integer, Integer>> pairs = new ArrayList<>();
        for (int i = 0; i < input.size(); i++) {
            if (input.get(i).getRight() == minimum)
                pairs.add(input.get(i));
        }
        return pairs;
    }

    private List<Pair<Match, Integer>> getMaximumMatchPenalty(List<Pair<Match, Integer>> input){
        int maximum = -1;
        for (int i = 0; i < input.size(); i++) {
            int penaltyCount = input.get(i).getRight();
            if (penaltyCount > maximum) maximum = penaltyCount;
        }
        List<Pair<Match, Integer>> pairs = new ArrayList<>();
        for (int i = 0; i < input.size(); i++) {
            if (input.get(i).getRight() == maximum)
                pairs.add(input.get(i));
        }
        return pairs;
    }
}
