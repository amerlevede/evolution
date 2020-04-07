package population;

import java.util.function.ToDoubleFunction;

/**
 * Class of organisms.
 * An organism has a genome and memoises its fitness value.
 * 
 * @author adriaan
 *
 * @param <G> - the genome type.
 */
public class Org<G> implements Comparable<Org<G>> {
	
	protected final G genome;
	protected final double fitness;
	
	public double getFitness() {
		return this.fitness;
	}
	
	public G getGenome() {
		return this.genome;
	}
	
	private Org(G genome, double fitness) {
		this.genome = genome;
		this.fitness = fitness;
	}
	
	public static <G> OrganismOp<G> factory(ToDoubleFunction<G> fitnessfunction) {
		return (g) -> {
			double fitness = fitnessfunction.applyAsDouble(g);
			return new Org<>(g, fitness);
		};
	}
	
//	public final static AsciiCereal<Org> ascii = new AsciiCereal<>() {
//		@Override
//		public String toAscii(Org a) {
//			return BitGenome.ascii.toAscii(a.genome) + "," + a.fitness;
//		}
//		
//		@Override
//		public Optional<Org> fromAscii(String ascii) {
//			String[] parts = ascii.split(",");
//			try {
//				if (parts.length != 0) throw new ArrayIndexOutOfBoundsException();
//				BitGenome g = BitGenome.ascii.fromAscii(parts[0]).get();
//				double f = Double.parseDouble(parts[1]);
//				return Optional.of(new Org(g, f));
//			} catch (ArrayIndexOutOfBoundsException|NoSuchElementException|NumberFormatException e) {
//				return Optional.empty();
//			}
//		}
//	};

	@Override
	public int compareTo(Org<G> that) {
		return Double.compare(this.fitness, that.fitness);
	}

}
