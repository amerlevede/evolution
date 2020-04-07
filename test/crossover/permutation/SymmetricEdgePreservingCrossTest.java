package crossover.permutation;

import static org.junit.Assert.assertTrue;

import java.util.stream.Stream;

import crossover.CrossoverOp;
import genome.integer.IntGenome;
import genome.integer.Permutation;

class SymmetricEdgePreservingCrossTest extends PermutationCrossoverTest {
	
	@Override
	public CrossoverOp<IntGenome> crossover() {
		return SymmetricEdgePreservingCross.<IntGenome>crossover(1000000).apply(rng);
	}
	
	public static boolean undirectedEdgePreserving(IntGenome a, IntGenome b, IntGenome aRef, IntGenome bRef) {
		
		Permutation ea = a.permutationView().edgeTransform();
		Permutation eaRef = aRef.permutationView().edgeTransform();
		Permutation eaRefInv = eaRef.inverse();
		Permutation ebRef = bRef.permutationView().edgeTransform();
		Permutation ebRefInv = ebRef.inverse();

		for (int i=0; i<a.size(); i++) {
			int thisi = i;
			boolean edgeExists = Stream
				.of(eaRef, eaRefInv, ebRef, ebRefInv)
				.mapToInt(e -> e.get(thisi))
				.anyMatch(j -> j == ea.get(thisi));
			if (!edgeExists) return false;
		}
		return true;
	}
	
	public static boolean undirectedEdgeRespectful(IntGenome a, IntGenome b, IntGenome aRef, IntGenome bRef) {
		Permutation ea = a.permutationView().edgeTransform();
		Permutation eaInv = ea.inverse();
		Permutation eaRef = aRef.permutationView().edgeTransform();
		Permutation ebRef = bRef.permutationView().edgeTransform();
		Permutation ebRefInv = ebRef.inverse();
		
		for (int i=0; i<a.size(); i++) {
			if (eaRef.get(i) == ebRef.get(i) || eaRef.get(i) == ebRefInv.get(i)) {
				if (!(ea.get(i) == eaRef.get(i) || eaInv.get(i) == eaRef.get(i))) {
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	public void otherConstraints(IntGenome a, IntGenome b, IntGenome aRef, IntGenome bRef) {
		assertTrue(undirectedEdgePreserving(a, b, aRef, bRef));
		assertTrue(undirectedEdgeRespectful(a, b, aRef, bRef));
	}

	public void testCrossover_manualTestCase() {
		IntGenome a = IntGenome.of(5, 6, 2, 0, 4, 1, 3, 7, 8);
		IntGenome b = IntGenome.of(5, 4, 6, 1, 2, 3, 0, 8, 7);
		IntGenome aRef = a.copy();
		IntGenome bRef = b.copy();
		
		System.out.println("before:");
		System.out.println(a);
		System.out.println(b);

		crossover().accept(a, b);
		
		System.out.println("after:");
		System.out.println(a);
		System.out.println();
		
		otherConstraints(a, b, aRef, bRef);
	}

}
