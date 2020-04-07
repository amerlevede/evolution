package genome.gene;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import genome.LinearGenome;
import genome.LinearGenome.KMP;

/**
 * Gene type representing a gene sequence and its value.
 */
public class Gene<G extends LinearGenome<G>,V> {

	public Gene(G sequence, int i, V value) {
		this.sequence = sequence;
		this.value = value;
		this.i = i;
	}

	private final int i;

	public int getIndex() {
		return this.i;
	}

	private final G sequence;
	private final V value;

	public G getSequence() {
		return this.sequence.view();
	};

	public V getValue() {
		return this.value;
	};

	/** @see Reader#read */
	@FunctionalInterface
	public interface Reader<G extends LinearGenome<G>,V> {
		/**
		 * Read a gene into a useful value.
		 * The gene is tagged by a consensus sequence inside a genome, and found at the given index.
		 */
		Optional<Gene<G,V>> read(KMP<G> consensus, G g, int i);

		static <G extends LinearGenome<G>,V> Stream<Gene<G,V>> scan(KMP<G> consensus, G g, Reader<G,V> reader) {
			List<Integer> genePositions = consensus.findAllNonOverlappingIn(g).boxed().collect(Collectors.toList());
			Stream.Builder<Gene<G,V>> result = Stream.builder();
			Gene<G,V> lastGene = null;
			for (int i=0; i<genePositions.size(); i++) {
				if (lastGene == null || genePositions.get(i) >= genePositions.get(i-1) + lastGene.getSequence().size()) {
					Optional<Gene<G,V>> readgene = reader.read(consensus, g, genePositions.get(i));
					if (readgene.isPresent()) {
						result.add(readgene.get());
						lastGene = readgene.get();
					}
				}
			}
			return result.build();
		}
	}

	@Override
	public String toString() {
		return "value " + this.getValue() + " at position " + this.getIndex() + " with sequence " + this.getSequence();
	}
}