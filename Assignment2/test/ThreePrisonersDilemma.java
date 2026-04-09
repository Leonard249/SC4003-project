public class ThreePrisonersDilemma {

	static int[][][] payoff = {
			{ { 6, 3 }, // payoffs when first and second players cooperate
					{ 3, 0 } }, // payoffs when first player coops, second defects
			{ { 8, 5 }, // payoffs when first player defects, second coops
					{ 5, 2 } } };// payoffs when first and second players defect

	abstract class Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
			throw new RuntimeException("You need to override the selectAction method.");
		}

		final String name() {
			String result = getClass().getName();
			return result.substring(result.indexOf('$') + 1);
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
			for (int i = 0; i < n; i++) {
				if (oppHistory1[i] == 0)
					opponentCoop = opponentCoop + 1;
				else
					opponentDefect = opponentDefect + 1;
			}
			for (int i = 0; i < n; i++) {
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
			if (n == 0)
				return 0;
			if (Math.random() < 0.5)
				return oppHistory1[n - 1];
			else
				return oppHistory2[n - 1];
		}
	}

	class AdaptivePlayer extends Player {
		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {

			// ── ROUND 0 ──────────────────────────────────────────────────────────
			// Open with cooperation. Safe because retaliation logic kicks in from
			// round 1 onwards, and cooperative openers build mutual 6/round payoffs.
			if (n == 0)
				return 0;

			// ── CUMULATIVE DEFECTION COUNTS ───────────────────────────────────────
			int d1 = 0, d2 = 0;
			for (int i = 0; i < n; i++) {
				d1 += oppHistory1[i];
				d2 += oppHistory2[i];
			}

			// ── STAGE 1: BOTH CONFIRMED NASTY → ALWAYS DEFECT ────────────────────
			// Two conditions flag a permanent defector:
			// perm: 100% defection rate after 3+ rounds (NastyPlayer, FreakyNasty)
			// conf: >70% defection rate after 8+ rounds (catches alternating patterns)
			// Only defect unconditionally when BOTH opponents are flagged.
			// Defecting against a single nasty when the other is cooperative cascades
			// TolerantPlayer into majority-retaliation → DDD = 2/round.
			// Cooperating against one nasty + one cooperator gives payoff[0][C][1] =
			// 3/round,
			// which beats the DDD outcome. The context-aware check below handles this case.
			boolean perm1 = (n >= 3 && d1 == n);
			boolean perm2 = (n >= 3 && d2 == n);
			boolean conf1 = (n >= 8 && d1 * 10 > n * 7);
			boolean conf2 = (n >= 8 && d2 * 10 > n * 7);
			if ((perm1 || conf1) && (perm2 || conf2))
				return 1;

			// ── STAGE 2: RANDOM PLAYER DETECTION ─────────────────────────────────
			// After 20 rounds, a defection rate between 30–70% is the signature of
			// RandomPlayer (memoryless, 50/50 each round). RandomPlayer cannot
			// reciprocate cooperation, so defecting is always dominant against them.
			// This is the single largest source of score gain over naive TF2T:
			// (Random, Random) match: +0.77/round vs baseline.
			// (Nice, Random) match: +1.09/round vs baseline.
			if (n >= 20) {
				double r1 = (double) d1 / n;
				double r2 = (double) d2 / n;
				if (r1 >= 0.3 && r1 <= 0.7)
					return 1;
				if (r2 >= 0.3 && r2 <= 0.7)
					return 1;
			}

			// ── STAGE 3: SINGLE CONFIRMED NASTY — CONTEXT-AWARE RESPONSE ─────────
			// When exactly one opponent is a confirmed permanent defector:
			// Check whether the cooperative opponent ever defected while we were
			// cooperating (cwc = "cooped while we cooped").
			//
			// cwc == 0 → they never punished our cooperation
			// they are Tolerant-like or Nice-like
			// retaliating would cascade them into majority-defect mode
			// → cooperate (get 3/round instead of 2/round DDD)
			//
			// cwc > 0 → they defected even when we were cooperative
			// they are T4T-like, TFT-like, or Random
			// they will not be provoked into a cascade by our defection
			// → defect (get 5/round from the cooperative-but-reactive opp)
			if ((perm1 || conf1) && !(perm2 || conf2)) {
				int cwc = 0;
				for (int i = 0; i < n; i++)
					if (myHistory[i] == 0 && oppHistory2[i] == 1)
						cwc++;
				return (cwc > 0) ? 1 : 0;
			}
			if ((perm2 || conf2) && !(perm1 || conf1)) {
				int cwc = 0;
				for (int i = 0; i < n; i++)
					if (myHistory[i] == 0 && oppHistory1[i] == 1)
						cwc++;
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
			// OR: one defecting opponent causes retaliation → hits the cooperative
			// third party → triggers TolerantPlayer cascade → DDD spiral
			// AND: requires coordinated or double defection → cooperative third
			// party is unaffected → Tolerant keeps cooperating → 3/round floor
			//
			// Self-forgiving by construction: the moment an opponent cooperates for
			// two consecutive rounds, the condition becomes false and we cooperate
			// again automatically. No explicit forgiveness window needed.
			//
			// Mirror-safe: two Adaptive players both cooperate by default. Neither
			// ever defects twice consecutively against the other unprovoked, so TF2T
			// AND never triggers. Guaranteed 6/round in mirror matches.
			if (n >= 2) {
				boolean tf1 = (oppHistory1[n - 1] == 1 && oppHistory1[n - 2] == 1);
				boolean tf2 = (oppHistory2[n - 1] == 1 && oppHistory2[n - 2] == 1);
				if (tf1 && tf2)
					return 1;
			}

			// ── DEFAULT: COOPERATE ────────────────────────────────────────────────
			// No exploit phase. Mutual cooperation at 6/round already outperforms
			// the occasional +2 from exploiting cooperators, once mirror-match costs
			// and Tolerant cascade risks are accounted for.
			return 0;
		}
	}

	class Old_Class extends Player {
		// State variable to track if our exploit backfired
		boolean exploitBusted = false;

		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {

			// ── ROUND 0 ──────────────────────────────────────────────────────────
			// Open with cooperation to establish trust with forgiving strategies.
			if (n == 0)
				return 0;

			// ── CUMULATIVE DEFECTION COUNTS ───────────────────────────────────────
			int d1 = 0, d2 = 0;
			for (int i = 0; i < n; i++) {
				d1 += oppHistory1[i];
				d2 += oppHistory2[i];
			}

			// ── STAGE 1: BOTH CONFIRMED NASTY → ALWAYS DEFECT ────────────────────
			// Flag permanent defectors (100% after 3 rounds, or >70% after 8 rounds)
			boolean perm1 = (n >= 3 && d1 == n);
			boolean perm2 = (n >= 3 && d2 == n);
			boolean conf1 = (n >= 8 && d1 * 10 > n * 7);
			boolean conf2 = (n >= 8 && d2 * 10 > n * 7);
			if ((perm1 || conf1) && (perm2 || conf2))
				return 1;

			// ── STAGE 2: RANDOM PLAYER DETECTION ─────────────────────────────────
			// After 20 rounds, a defection rate between 30–70% indicates a memoryless
			// RandomPlayer. Defecting is the mathematically dominant strategy here.
			if (n >= 20) {
				double r1 = (double) d1 / n;
				double r2 = (double) d2 / n;
				if (r1 >= 0.3 && r1 <= 0.7)
					return 1;
				if (r2 >= 0.3 && r2 <= 0.7)
					return 1;
			}

			// ── STAGE 2.5: THE PREDATORY EXPLOIT ─────────────────────────────────
			// If we haven't been caught yet, and the game has gone on for 5 rounds...
			if (!exploitBusted && n >= 5) {

				// Check if BOTH opponents have been perfectly peaceful so far
				if (d1 == 0 && d2 == 0) {
					// If they are perfectly peaceful, defect exactly every 3rd round.
					// Round 6 (n=5) will be the first "Probe" defection.
					if (n % 3 == 2) {
						return 1; // Steal 8 points
					}
				}

				// THE SAFETY VALVE: Check if they retaliated to our probe.
				// If we defected in the previous round, and they immediately defected back,
				// they are reactive (like T4T), not passive. Shut down the exploit forever.
				if (n >= 2 && myHistory[n - 1] == 1) {
					if (oppHistory1[n - 1] == 1 || oppHistory2[n - 1] == 1) {
						exploitBusted = true;
						// Let the code fall through to Stage 5 (TF2T) to repair the relationship
					}
				}
			}

			// ── STAGE 3: SINGLE CONFIRMED NASTY — CONTEXT-AWARE RESPONSE ─────────
			// Check whether the cooperative opponent ever defected while we were
			// cooperating.
			// cwc == 0 -> Tolerant/Nice -> Play the Martyr (return 0) to prevent a 2-point
			// cascade.
			// cwc > 0 -> T4T/Random -> They won't cascade, so protect ourselves (return 1).
			if ((perm1 || conf1) && !(perm2 || conf2)) {
				int cwc = 0;
				for (int i = 0; i < n; i++)
					if (myHistory[i] == 0 && oppHistory2[i] == 1)
						cwc++;
				return (cwc > 0) ? 1 : 0;
			}
			if ((perm2 || conf2) && !(perm1 || conf1)) {
				int cwc = 0;
				for (int i = 0; i < n; i++)
					if (myHistory[i] == 0 && oppHistory1[i] == 1)
						cwc++;
				return (cwc > 0) ? 1 : 0;
			}

			// ── STAGE 4: ROUND 1 GUARD ────────────────────────────────────────────
			// Require BOTH opponents to have defected before retaliating in round 1.
			// Prevents triggering an accidental cascade against a T4T player.
			if (n == 1) {
				return (oppHistory1[0] == 1 && oppHistory2[0] == 1) ? 1 : 0;
			}

			// ── STAGE 5: TIT-FOR-TWO-TATS (AND logic) ────────────────────────────
			// Retaliate only when BOTH opponents have defected in BOTH of their last two
			// rounds.
			if (n >= 2) {
				boolean tf1 = (oppHistory1[n - 1] == 1 && oppHistory1[n - 2] == 1);
				boolean tf2 = (oppHistory2[n - 1] == 1 && oppHistory2[n - 2] == 1);
				if (tf1 && tf2)
					return 1;
			}

			// ── DEFAULT: COOPERATE ────────────────────────────────────────────────
			// Fallback to mutual cooperation for 6 points a round.
			return 0;
		}
	}

	class Old_player1 extends Player {
		// Cooldown timer for our point-farming exploit
		int exploitCooldown = 0;

		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {

			// ── ROUND 0 ──────────────────────────────────────────────────────────
			// Always open with cooperation to establish trust with forgiving strategies.
			if (n == 0)
				return 0;

			// ── THE HORIZON EXPLOIT (End-Game Betrayal) ──────────────────────────
			// Matches end randomly between 90 and 110 rounds. At round 88, the
			// "shadow of the future" is too short for opponents to punish us effectively.
			if (n >= 88)
				return 1;

			int d1 = 0, d2 = 0;
			for (int i = 0; i < n; i++) {
				d1 += oppHistory1[i];
				d2 += oppHistory2[i];
			}

			// ── STAGE 1: CONFIRMED NASTY FLAGS ───────────────────────────────────
			boolean perm1 = (n >= 3 && d1 == n);
			boolean perm2 = (n >= 3 && d2 == n);
			boolean conf1 = (n >= 8 && d1 * 10 > n * 7);
			boolean conf2 = (n >= 8 && d2 * 10 > n * 7);

			// If both are confirmed hostile early on, lock down to protect our score.
			if ((perm1 || conf1) && (perm2 || conf2))
				return 1;

			// ── STAGE 2: RANDOM PLAYER DETECTION ─────────────────────────────────
			// A defection rate between 30% and 70% after 20 rounds implies RandomPlayer.
			// They have no memory, so defecting is the mathematically dominant strategy.
			if (n >= 20) {
				double r1 = (double) d1 / n;
				double r2 = (double) d2 / n;
				if (r1 >= 0.3 && r1 <= 0.7)
					return 1;
				if (r2 >= 0.3 && r2 <= 0.7)
					return 1;
			}

			// ── STAGE 3: THE RESILIENT PREDATORY EXPLOIT ─────────────────────────
			if (exploitCooldown > 0) {
				exploitCooldown--;
			}
			if (exploitCooldown == 0 && n >= 5) {
				// If the board is highly peaceful (allowing for up to 2 accidental defects)
				if (d1 <= 2 && d2 <= 2) {
					if (n % 3 == 2)
						return 1; // Farm 8 points exactly every 3rd round
				}
				// Safety Valve: If they retaliated to our probe in the last round
				if (n >= 2 && myHistory[n - 1] == 1) {
					if (oppHistory1[n - 1] == 1 || oppHistory2[n - 1] == 1) {
						exploitCooldown = 10; // Pause exploit for 10 rounds to restore peace
					}
				}
			}

			// ── STAGE 4: CAPPED MARTYR (Loss Cutting) ────────────────────────────
			// Protect TolerantPlayer for the first 30 rounds by absorbing a 3-point score
			// to prevent a 2-point cascade. After round 30, the global point cost is too
			// high.
			if (n < 30) {
				if ((perm1 || conf1) && !(perm2 || conf2)) {
					int cwc = 0;
					for (int i = 0; i < n; i++)
						if (myHistory[i] == 0 && oppHistory2[i] == 1)
							cwc++;
					return (cwc > 0) ? 1 : 0;
				}
				if ((perm2 || conf2) && !(perm1 || conf1)) {
					int cwc = 0;
					for (int i = 0; i < n; i++)
						if (myHistory[i] == 0 && oppHistory1[i] == 1)
							cwc++;
					return (cwc > 0) ? 1 : 0;
				}
			} else {
				// Past round 30, if ANYONE is still confirmed Nasty, stop playing the martyr.
				// We cut our losses and defect to stop feeding them 8 points a round.
				if (perm1 || conf1 || perm2 || conf2)
					return 1;
			}

			// ── STAGE 5: THE APOLOGY (Death Spiral Recovery) ─────────────────────
			// If the last 5 rounds have been pure mutual defection (D-D-D),
			// offer a single 'Cooperate' to try and reset Tit-For-Tat opponents.
			if (n >= 5) {
				boolean deathSpiral = true;
				for (int i = n - 5; i < n; i++) {
					if (myHistory[i] == 0 || oppHistory1[i] == 0 || oppHistory2[i] == 0) {
						deathSpiral = false;
					}
				}
				if (deathSpiral)
					return 0; // Offer the olive branch
			}

			// ── STAGE 6: SUDDEN BETRAYAL GUARD ───────────────────────────────────
			// If the game was peaceful but someone suddenly backstabs twice in a row,
			// retaliate immediately, overriding the standard AND logic below.
			if (n > 10 && (d1 < 3 || d2 < 3)) {
				boolean suddenStab1 = (oppHistory1[n - 1] == 1 && oppHistory1[n - 2] == 1);
				boolean suddenStab2 = (oppHistory2[n - 1] == 1 && oppHistory2[n - 2] == 1);
				if (suddenStab1 || suddenStab2)
					return 1;
			}

			// ── STAGE 7: ROUND 1 GUARD ───────────────────────────────────────────
			// Requires BOTH opponents to have defected before retaliating in round 1.
			// Prevents triggering an accidental cascade against a T4T player.
			if (n == 1) {
				return (oppHistory1[0] == 1 && oppHistory2[0] == 1) ? 1 : 0;
			}

			// ── STAGE 8: TIT-FOR-TWO-TATS (AND Logic) ────────────────────────────
			// The default mid-game defense. Retaliates only when BOTH opponents
			// defect in BOTH of their last two consecutive rounds.
			if (n >= 2) {
				boolean tf1 = (oppHistory1[n - 1] == 1 && oppHistory1[n - 2] == 1);
				boolean tf2 = (oppHistory2[n - 1] == 1 && oppHistory2[n - 2] == 1);
				if (tf1 && tf2)
					return 1;
			}

			// ── DEFAULT: COOPERATE ───────────────────────────────────────────────
			return 0;
		}
	}

	class Ong_Leonard_Player extends Player {

		// Per-instance random exploit offset.
		// Three copies of this strategy playing each other will have different offsets
		// (0, 1, or 2), so they do not all defect on the same round. This prevents
		// the mutual-punishment tax that occurs when all three fire simultaneously.
		int exploitOffset = (int) (Math.random() * 3);

		// Cooldown counter. After an opponent retaliates against our exploit defection,
		// we pause farming for 10 rounds to allow trust to rebuild before resuming.
		int exploitCooldown = 0;

		int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {

			// ── ROUND 0 ──────────────────────────────────────────────────────────
			// Always open with cooperation. Cooperative openers build the mutual
			// 6/round baseline that all subsequent logic tries to exceed.
			if (n == 0)
				return 0;

			// ── CUMULATIVE DEFECTION COUNTS ───────────────────────────────────────
			int d1 = 0, d2 = 0;
			for (int i = 0; i < n; i++) {
				d1 += oppHistory1[i];
				d2 += oppHistory2[i];
			}

			// ── HORIZON EXPLOIT ───────────────────────────────────────────────────
			// Matches end randomly between rounds 90 and 110. From round 85, the
			// expected remaining rounds (~10) are too few for opponents to inflict
			// more punishment than we gain by defecting every round.
			//
			// Conditional (Fix 1): only activate against passive opponents
			// (defection rate < 15%). Against reactive opponents like T4T who have
			// been copying our exploit defections, their accumulated rate exceeds 15%,
			// so the horizon correctly does NOT fire. Without this condition, firing
			// unconditionally against T4T locks both into DDD from round 86 onward,
			// losing ~54 points over the endgame vs the +20 points we intended to gain.
			if (n >= 85 && d1 < n * 0.15 && d2 < n * 0.15)
				return 1;

			// ── STAGE 1: BOTH CONFIRMED NASTY → ALWAYS DEFECT ────────────────────
			// Two conditions identify a permanent defector:
			// perm: 100% defection rate after 3+ rounds (NastyPlayer, Freaky-Nasty)
			// conf: >70% defection rate after 8+ rounds (frequent defectors)
			// We defect only when BOTH are flagged. A single confirmed-nasty opponent
			// is handled by Stage 4 (martyr), which carefully checks whether the
			// cooperative third party is Tolerant-like before deciding to retaliate.
			boolean perm1 = (n >= 3 && d1 == n);
			boolean perm2 = (n >= 3 && d2 == n);
			boolean conf1 = (n >= 8 && d1 * 10 > n * 7);
			boolean conf2 = (n >= 8 && d2 * 10 > n * 7);
			if ((perm1 || conf1) && (perm2 || conf2))
				return 1;

			// ── STAGE 2: RANDOM PLAYER DETECTION ─────────────────────────────────
			// After 20 rounds, a 30–70% defection rate is the fingerprint of
			// RandomPlayer: memoryless, 50/50 each round, cannot be trained into
			// cooperation. Defecting is the dominant strategy against them.
			//
			// Unprovoked guard (Fix 2): require > 5 unprovoked defections (defections
			// while we were cooperating) before classifying as Random. This prevents
			// misclassifying a reactive strategy (T4T, TFT, student bots) that had a
			// mid-match war and ended up with a 35% rate but has since reformed.
			// RandomPlayer defects regardless of our action so its unprovoked count
			// climbs quickly; reformed reactive strategies keep theirs low.
			if (n >= 20) {
				double r1 = (double) d1 / n;
				double r2 = (double) d2 / n;
				int unprovoked1 = 0, unprovoked2 = 0;
				for (int i = 0; i < n; i++) {
					if (myHistory[i] == 0 && oppHistory1[i] == 1)
						unprovoked1++;
					if (myHistory[i] == 0 && oppHistory2[i] == 1)
						unprovoked2++;
				}
				if (r1 >= 0.3 && r1 <= 0.7 && unprovoked1 > 5)
					return 1;
				if (r2 >= 0.3 && r2 <= 0.7 && unprovoked2 > 5)
					return 1;
			}

			// ── STAGE 3: RESILIENT PREDATORY EXPLOIT ─────────────────────────────
			// When the match is peaceful (both opponents have at most 2 total
			// defections — allowing for T4T copying one exploit defection or random
			// noise), defect every 3rd round to earn +2 points above mutual cooperation.
			//
			// The staggered exploitOffset ensures mirror matches are desynchronised:
			// three copies of this strategy will have offset 0, 1, or 2, so they
			// cannot all defect on the same round and trigger mutual punishment.
			//
			// TolerantPlayer is safely farmable: 1 defection per 3 rounds = 33% of our
			// actions. Combined with two cooperative opponents, defections never exceed
			// cooperations in TolerantPlayer's count, so it never retaliates.
			//
			// Safety valve: if an opponent retaliates immediately after our defection
			// (they are reactive, not passive), pause for 10 rounds so trust rebuilds
			// before resuming. This prevents a single T4T retaliation from permanently
			// destroying the farming relationship.
			//
			// Note: the exploit's own condition (d1 <= 2 && d2 <= 2) naturally prevents
			// it from firing during single-nasty scenarios because confirmed-nasty
			// opponents accumulate > 2 defections within 3 rounds, defeating the check.
			if (exploitCooldown > 0) {
				exploitCooldown--;
			}
			if (exploitCooldown == 0 && n >= 5) {
				if (d1 <= 2 && d2 <= 2) {
					if (n % 3 == exploitOffset)
						return 1;
				}
				if (n >= 2 && myHistory[n - 1] == 1) {
					if (oppHistory1[n - 1] == 1 || oppHistory2[n - 1] == 1) {
						exploitCooldown = 10;
					}
				}
			}

			// ── STAGE 4: CAPPED MARTYR (Single-Nasty Tolerance) ──────────────────
			// When exactly one opponent is a confirmed defector and the other is
			// cooperative, we use the cwc (cooped-while-we-cooped) test to decide:
			//
			// cwc == 0 → cooperative opponent never defected while we cooperated.
			// They are Tolerant-like or Nice-like. If we retaliate against
			// the nasty, TolerantPlayer sees (Nasty defects + we defect)
			// as a majority and cascades: DDD = 2/round.
			// Cooperating absorbs the nasty's hits but scores 3/round,
			// which beats the 2/round DDD outcome. → COOPERATE
			//
			// cwc > 0 → cooperative opponent defected even when we were cooperating.
			// They are T4T-like or random-reactive. They react to us, not
			// to the third party, so defecting does not cascade them.
			// → DEFECT (collect 5/round from their reactive cooperation)
			//
			// Capped at round 50: beyond this point, the remaining match length
			// (~35–60 rounds) is short enough that cascade losses are bounded, while
			// continuing to feed a confirmed defector 8/round is increasingly costly.
			if (n < 50) {
				if ((perm1 || conf1) && !(perm2 || conf2)) {
					int cwc = 0;
					for (int i = 0; i < n; i++)
						if (myHistory[i] == 0 && oppHistory2[i] == 1)
							cwc++;
					return (cwc > 0) ? 1 : 0;
				}
				if ((perm2 || conf2) && !(perm1 || conf1)) {
					int cwc = 0;
					for (int i = 0; i < n; i++)
						if (myHistory[i] == 0 && oppHistory1[i] == 1)
							cwc++;
					return (cwc > 0) ? 1 : 0;
				}
			} else {
				if (perm1 || conf1 || perm2 || conf2)
					return 1;
			}

			// ── STAGE 5: DEATH SPIRAL RECOVERY (Apology) ─────────────────────────
			// If all three players have defected for 5 consecutive rounds but Stage 1
			// has not flagged both opponents (they are below the 70% threshold), both
			// are likely reactive strategies caught in a punishment loop from earlier
			// noise or the exploit. Offer one cooperative round as an olive branch.
			//
			// Gap 4 fix: guard against misfiring when opponents are semi-hostile.
			// If either opponent's cumulative defection rate exceeds 40%, they are
			// not recoverable by a single olive branch — the apology would just give
			// them a free 8-point round. Only offer the apology when both opponents
			// have been mostly cooperative overall (< 40% defection rate).
			if (n >= 5) {
				if (d1 < n * 0.4 && d2 < n * 0.4) {
					boolean deathSpiral = true;
					for (int i = n - 5; i < n; i++) {
						if (myHistory[i] == 0 || oppHistory1[i] == 0 || oppHistory2[i] == 0) {
							deathSpiral = false;
							break;
						}
					}
					if (deathSpiral)
						return 0;
				}
			}

			// ── STAGE 6: SUDDEN BETRAYAL GUARD ───────────────────────────────────
			// Catches strategies that cooperate for many rounds before abruptly
			// defecting (sleeper bots, horizon exploiters, students who "go nasty late").
			//
			// Percentage threshold (Fix 3): the original hard threshold of d < 3
			// deactivated this guard once either opponent accumulated 3 defections,
			// which could happen from noise as early as round 20. Replacing with
			// d < n * 0.15 keeps the guard active throughout for mostly-cooperative
			// opponents. A player who has defected 15% of the time overall but just
			// defected twice in a row is a genuine threat deserving retaliation.
			//
			// OR logic: a sudden betrayal by one opponent in a 3-player game is a
			// signal to defend immediately against both, since the third party may be
			// coordinating or may cascade from the resulting score imbalance.
			if (n > 10 && (d1 < n * 0.15 || d2 < n * 0.15)) {
				boolean suddenStab1 = (oppHistory1[n - 1] == 1 && oppHistory1[n - 2] == 1);
				boolean suddenStab2 = (oppHistory2[n - 1] == 1 && oppHistory2[n - 2] == 1);
				if (suddenStab1 || suddenStab2)
					return 1;
			}

			// ── STAGE 7: ROUND 1 GUARD ───────────────────────────────────────────
			// With only one history point, TF2T cannot run. Require BOTH opponents to
			// have defected before retaliating — a single round-1 defection is likely
			// T4T copying an unknown third party, FreakyNasty before confirmation, or
			// noise. Single-defector retaliation triggers the TolerantPlayer cascade.
			if (n == 1) {
				return (oppHistory1[0] == 1 && oppHistory2[0] == 1) ? 1 : 0;
			}

			// ── STAGE 8: TIT-FOR-TWO-TATS (AND Logic) ────────────────────────────
			// Core mid-game defence. Retaliate only when an opponent has defected in
			// BOTH of their last two consecutive rounds.
			//
			// AND logic protects the cooperative third party: OR logic retaliates
			// against the cooperative opponent too, tipping TolerantPlayer's majority
			// and cascading the whole match into DDD. AND requires both to show
			// sustained aggression before we respond.
			//
			// Self-forgiving: two consecutive cooperations by either opponent resets
			// the condition automatically, no explicit forgiveness window needed.
			// Mirror-safe: two copies of this strategy cooperate by default; neither
			// defects twice consecutively against the other, so TF2T-AND never fires.
			if (n >= 2) {
				boolean tf1 = (oppHistory1[n - 1] == 1 && oppHistory1[n - 2] == 1);
				boolean tf2 = (oppHistory2[n - 1] == 1 && oppHistory2[n - 2] == 1);
				if (tf1 && tf2)
					return 1;
			}

			// ── DEFAULT: COOPERATE ───────────────────────────────────────────────
			// Fall back to mutual cooperation for 6/round. No exploit here — the
			// Stage 3 farming already captures the available surplus from passive
			// opponents without the cascade risk of unconditional defection.
			return 0;
		}
	}

	float[] scoresOfMatch(Player A, Player B, Player C, int rounds) {
		int[] HistoryA = new int[0], HistoryB = new int[0], HistoryC = new int[0];
		float ScoreA = 0, ScoreB = 0, ScoreC = 0;

		for (int i = 0; i < rounds; i++) {
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
		float[] result = { ScoreA / rounds, ScoreB / rounds, ScoreC / rounds };
		return result;
	}

	int[] extendIntArray(int[] arr, int next) {
		int[] result = new int[arr.length + 1];
		for (int i = 0; i < arr.length; i++) {
			result[i] = arr[i];
		}
		result[result.length - 1] = next;
		return result;
	}

	int numPlayers = 10;

	Player makePlayer(int which) {
		switch (which) {
			case 0:
				return new NicePlayer();
			case 1:
				return new NastyPlayer();
			case 2:
				return new RandomPlayer();
			case 3:
				return new TolerantPlayer();
			case 4:
				return new FreakyPlayer();
			case 5:
				return new T4TPlayer();
			case 6:
				return new AdaptivePlayer();
			case 7:
				return new Old_Class();
			case 8:
				return new Old_player1();
			case 9:
				return new Ong_Leonard_Player();
		}
		throw new RuntimeException("Bad argument passed to makePlayer");
	}

	public static void main(String[] args) {
		ThreePrisonersDilemma instance = new ThreePrisonersDilemma();
		instance.runTournament();
	}

	boolean verbose = true;

	void runTournament() {
		float[] totalScore = new float[numPlayers];

		for (int i = 0; i < numPlayers; i++)
			for (int j = i; j < numPlayers; j++)
				for (int k = j; k < numPlayers; k++) {

					Player A = makePlayer(i);
					Player B = makePlayer(j);
					Player C = makePlayer(k);
					int rounds = 90 + (int) Math.rint(20 * Math.random());
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

		for (int i = 0; i < numPlayers; i++) {
			int j = i - 1;
			for (; j >= 0; j--) {
				if (totalScore[i] > totalScore[sortedOrder[j]])
					sortedOrder[j + 1] = sortedOrder[j];
				else
					break;
			}
			sortedOrder[j + 1] = i;
		}

		if (verbose)
			System.out.println();
		System.out.println("Tournament Results");
		for (int i = 0; i < numPlayers; i++)
			System.out.println(makePlayer(sortedOrder[i]).name() + ": "
					+ totalScore[sortedOrder[i]] + " points.");

	}
}