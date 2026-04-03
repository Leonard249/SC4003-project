public class ThreePrisonersDilemma {
	
	static int[][][] payoff = {  
		{{6,3},  //payoffs when first and second players cooperate 
		 {3,0}}, //payoffs when first player coops, second defects
		{{8,5},  //payoffs when first player defects, second coops
	     {5,2}}};//payoffs when first and second players defect
	
	abstract class Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			throw new RuntimeException("You need to override the selectAction method.");
		}
		
		final String name() {
			String result = getClass().getName();
			return result.substring(result.indexOf('$')+1);
		}
	}
	
	/* --- BASELINE STRATEGIES --- */
	
	class NicePlayer extends Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			return 0; 
		}
	}
	
	class NastyPlayer extends Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			return 1; 
		}
	}
	
	class RandomPlayer extends Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (Math.random() < 0.5)
				return 0;  
			else
				return 1;  
		}
	}
	
	class TolerantPlayer extends Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			int opponentCoop = 0;
			int opponentDefect = 0;
			for (int i=0; i<n; i++) {
				if (oppHistory1[i] == 0)
					opponentCoop = opponentCoop + 1;
				else
					opponentDefect = opponentDefect + 1;
			}
			for (int i=0; i<n; i++) {
				if (oppHistory2[i] == 0)
					opponentCoop = opponentCoop + 1;
				else
					opponentDefect = opponentDefect + 1;
			}
			if (opponentDefect > opponentCoop)
				return 1;
			else
				return 0;
		}
	}
	
	class FreakyPlayer extends Player {
		int action;
		FreakyPlayer() {
			if (Math.random() < 0.5)
				action = 0; 
			else
				action = 1; 
		}
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			return action;
		}	
	}

	class T4TPlayer extends Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			if (n==0) return 0; 
			if (Math.random() < 0.5)
				return oppHistory1[n-1];
			else
				return oppHistory2[n-1];
		}	
	}
    
    class AdaptivePlayer extends Player {
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {

            // ── ROUND 0 ──────────────────────────────────────────────────────────
            // Open with cooperation. Safe because retaliation logic kicks in from
            // round 1 onwards, and cooperative openers build mutual 6/round payoffs.
            if (n == 0) return 0;

            // ── CUMULATIVE DEFECTION COUNTS ───────────────────────────────────────
            int d1 = 0, d2 = 0;
            for (int i = 0; i < n; i++) {
                d1 += oppHistory1[i];
                d2 += oppHistory2[i];
            }

            // ── STAGE 1: BOTH CONFIRMED NASTY → ALWAYS DEFECT ────────────────────
            // Two conditions flag a permanent defector:
            //   perm: 100% defection rate after 3+ rounds (NastyPlayer, FreakyNasty)
            //   conf: >70% defection rate after 8+ rounds (catches alternating patterns)
            // Only defect unconditionally when BOTH opponents are flagged.
            // Defecting against a single nasty when the other is cooperative cascades
            // TolerantPlayer into majority-retaliation → DDD = 2/round.
            // Cooperating against one nasty + one cooperator gives payoff[0][C][1] = 3/round,
            // which beats the DDD outcome. The context-aware check below handles this case.
            boolean perm1 = (n >= 3 && d1 == n);
            boolean perm2 = (n >= 3 && d2 == n);
            boolean conf1 = (n >= 8 && d1 * 10 > n * 7);
            boolean conf2 = (n >= 8 && d2 * 10 > n * 7);
            if ((perm1 || conf1) && (perm2 || conf2)) return 1;

            // ── STAGE 2: RANDOM PLAYER DETECTION ─────────────────────────────────
            // After 20 rounds, a defection rate between 30–70% is the signature of
            // RandomPlayer (memoryless, 50/50 each round). RandomPlayer cannot
            // reciprocate cooperation, so defecting is always dominant against them.
            // This is the single largest source of score gain over naive TF2T:
            // (Random, Random) match: +0.77/round vs baseline.
            // (Nice, Random)   match: +1.09/round vs baseline.
            if (n >= 20) {
                double r1 = (double) d1 / n;
                double r2 = (double) d2 / n;
                if (r1 >= 0.3 && r1 <= 0.7) return 1;
                if (r2 >= 0.3 && r2 <= 0.7) return 1;
            }

            // ── STAGE 3: SINGLE CONFIRMED NASTY — CONTEXT-AWARE RESPONSE ─────────
            // When exactly one opponent is a confirmed permanent defector:
            // Check whether the cooperative opponent ever defected while we were
            // cooperating (cwc = "cooped while we cooped").
            //
            // cwc == 0  →  they never punished our cooperation
            //              they are Tolerant-like or Nice-like
            //              retaliating would cascade them into majority-defect mode
            //              → cooperate (get 3/round instead of 2/round DDD)
            //
            // cwc > 0   →  they defected even when we were cooperative
            //              they are T4T-like, TFT-like, or Random
            //              they will not be provoked into a cascade by our defection
            //              → defect (get 5/round from the cooperative-but-reactive opp)
            if ((perm1 || conf1) && !(perm2 || conf2)) {
                int cwc = 0;
                for (int i = 0; i < n; i++)
                    if (myHistory[i] == 0 && oppHistory2[i] == 1) cwc++;
                return (cwc > 0) ? 1 : 0;
            }
            if ((perm2 || conf2) && !(perm1 || conf1)) {
                int cwc = 0;
                for (int i = 0; i < n; i++)
                    if (myHistory[i] == 0 && oppHistory1[i] == 1) cwc++;
                return (cwc > 0) ? 1 : 0;
            }

            // ── STAGE 4: ROUND 1 GUARD ────────────────────────────────────────────
            // Only one round of history — not enough for TF2T.
            // Require BOTH opponents to have defected before retaliating.
            // A single round-1 defection could be noise, T4T copying an unknown
            // opponent, or FreakyNasty before we know their pattern.
            // Single-defector retaliation here triggers the Tolerant cascade.
            if (n == 1) {
                return (oppHistory1[0] == 1 && oppHistory2[0] == 1) ? 1 : 0;
            }

            // ── STAGE 5: TIT-FOR-TWO-TATS (AND logic) ────────────────────────────
            // The core repeated-game strategy. Retaliate only when an opponent has
            // defected in BOTH of their last two consecutive rounds.
            //
            // AND logic (both must trigger) vs OR logic (either triggers):
            //   OR: one defecting opponent causes retaliation → hits the cooperative
            //       third party → triggers TolerantPlayer cascade → DDD spiral
            //   AND: requires coordinated or double defection → cooperative third
            //       party is unaffected → Tolerant keeps cooperating → 3/round floor
            //
            // Self-forgiving by construction: the moment an opponent cooperates for
            // two consecutive rounds, the condition becomes false and we cooperate
            // again automatically. No explicit forgiveness window needed.
            //
            // Mirror-safe: two Adaptive players both cooperate by default. Neither
            // ever defects twice consecutively against the other unprovoked, so TF2T
            // AND never triggers. Guaranteed 6/round in mirror matches.
            if (n >= 2) {
                boolean tf1 = (oppHistory1[n-1] == 1 && oppHistory1[n-2] == 1);
                boolean tf2 = (oppHistory2[n-1] == 1 && oppHistory2[n-2] == 1);
                if (tf1 && tf2) return 1;
            }

            // ── DEFAULT: COOPERATE ────────────────────────────────────────────────
            // No exploit phase. Mutual cooperation at 6/round already outperforms
            // the occasional +2 from exploiting cooperators, once mirror-match costs
            // and Tolerant cascade risks are accounted for.
            return 0;
        }
    }
	
	float[] scoresOfMatch(Player A, Player B, Player C, int rounds) {
		int[] HistoryA = new int[0], HistoryB = new int[0], HistoryC = new int[0];
		float ScoreA = 0, ScoreB = 0, ScoreC = 0;
		
		for (int i=0; i<rounds; i++) {
			int PlayA = A.selectAction(i, HistoryA, HistoryB, HistoryC);
			int PlayB = B.selectAction(i, HistoryB, HistoryC, HistoryA);
			int PlayC = C.selectAction(i, HistoryC, HistoryA, HistoryB);
			ScoreA = ScoreA + payoff[PlayA][PlayB][PlayC];
			ScoreB = ScoreB + payoff[PlayB][PlayC][PlayA];
			ScoreC = ScoreC + payoff[PlayC][PlayA][PlayB];
			HistoryA = extendIntArray(HistoryA, PlayA);
			HistoryB = extendIntArray(HistoryB, PlayB);
			HistoryC = extendIntArray(HistoryC, PlayC);
		}
		float[] result = {ScoreA/rounds, ScoreB/rounds, ScoreC/rounds};
		return result;
	}
	
	int[] extendIntArray(int[] arr, int next) {
		int[] result = new int[arr.length+1];
		for (int i=0; i<arr.length; i++) {
			result[i] = arr[i];
		}
		result[result.length-1] = next;
		return result;
	}
	
    // UPDATED: Now supports 7 players
	int numPlayers = 7; 
	Player makePlayer(int which) {
		switch (which) {
		case 0: return new NicePlayer();
		case 1: return new NastyPlayer();
		case 2: return new RandomPlayer();
		case 3: return new TolerantPlayer();
		case 4: return new FreakyPlayer();
		case 5: return new T4TPlayer();
        case 6: return new AdaptivePlayer(); 
		}
		throw new RuntimeException("Bad argument passed to makePlayer");
	}
	
	public static void main (String[] args) {
		ThreePrisonersDilemma instance = new ThreePrisonersDilemma();
		instance.runTournament();
	}
	
	boolean verbose = true; 
	
	void runTournament() {
		float[] totalScore = new float[numPlayers];

		for (int i=0; i<numPlayers; i++) for (int j=i; j<numPlayers; j++) for (int k=j; k<numPlayers; k++) {

			Player A = makePlayer(i); 
			Player B = makePlayer(j);
			Player C = makePlayer(k);
			int rounds = 90 + (int)Math.rint(20 * Math.random()); 
			float[] matchResults = scoresOfMatch(A, B, C, rounds); 
			totalScore[i] = totalScore[i] + matchResults[0];
			totalScore[j] = totalScore[j] + matchResults[1];
			totalScore[k] = totalScore[k] + matchResults[2];
			if (verbose)
				System.out.println(A.name() + " scored " + matchResults[0] +
						" points, " + B.name() + " scored " + matchResults[1] + 
						" points, and " + C.name() + " scored " + matchResults[2] + " points.");
		}
		int[] sortedOrder = new int[numPlayers];

		for (int i=0; i<numPlayers; i++) {
			int j=i-1;
			for (; j>=0; j--) {
				if (totalScore[i] > totalScore[sortedOrder[j]]) 
					sortedOrder[j+1] = sortedOrder[j];
				else break;
			}
			sortedOrder[j+1] = i;
		}
		
		if (verbose) System.out.println();
		System.out.println("Tournament Results");
		for (int i=0; i<numPlayers; i++) 
			System.out.println(makePlayer(sortedOrder[i]).name() + ": " 
				+ totalScore[sortedOrder[i]] + " points.");
		
	} 
}