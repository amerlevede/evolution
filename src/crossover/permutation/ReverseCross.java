package crossover.permutation;

import crossover.CrossoverOp;
import crossover.CrossoverRule;
import genome.LinearGenome;

@Deprecated
public class ReverseCross {

	/**
	 * Transform a one-directional edge crossover to a bidirectional edge crossover by repeating it with different orientations.
	 * If reps < 0, no matching will be done. If reps == 0, the crossover will occur only once, but will involve a random orientation of the second parent.
	 * This implementation assumes that only the first parent is modified by the crossover!
	 */
	public static <G extends LinearGenome<G>> CrossoverRule<G> heuristicMixMatchOnlyFirstParent(CrossoverRule<G> cross, int reps) {
		return (rng) -> {
			CrossoverOp<G> crossOp = cross.apply(rng);
			if (reps < 0) {
				return crossOp;
			} else return (a, b) -> {
				int[] partners = new int[reps]; // List of partners to cross over with. 0 -> a; 1 -> b; 2 -> reverse a; 3 -> reverse b
				int firstPartner = rng.nextBoolean() ? 1 : 3; // Always start with either b or reverse b
				int last = firstPartner;
				boolean apresent = false;
				for (int i=0; i<reps; i++) {
					// Set new partner randomly but avoid crossing over twice in succession with same partner
					do partners[i] = rng.nextInt(5);
						while (partners[i] == last || (i==0 && partners[i] == 0));
					if (partners[i] == 0 || partners[i] == 2) apresent = true;
				}

				G aref = apresent ? a.copy() : null;

				crossOp.accept(a,decode(firstPartner, null, b));
				for (int i=0; i<reps; i++) {
					crossOp.accept(a, decode(firstPartner, aref, b));
				}
			};
		};
	}

	@Deprecated
	public static <G extends LinearGenome<G>> CrossoverRule<G> both(CrossoverRule<G> cross) {
		return (rng) -> (a, b) -> {
			G aref = a.reversedView().copy();
			cross.apply(rng).accept(a, b.reversedView().copy());
			cross.apply(rng).accept(aref, b);
		};
	}

	static <G extends LinearGenome<G>> G decode(int code, G a, G b) {
		return code == 0 ? a
			 : code == 1 ? b
			 : code == 2 ? a.reversedView()
			 :             b.reversedView();
	}

}
