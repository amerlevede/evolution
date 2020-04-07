package population;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import crossover.CrossoverOp;
import genome.Genome;
import mutation.MutationOp;
import selection.Selector;

/**
 * Population of organisms.
 * Gives access to a Collection of organisms but also contains various methods for modifying the population, for example by reproduction.
 * 
 * @author adriaan
 *
 * @param <G> - The type of genome data structure of the organisms in the population.
 */
public class Population<G extends Genome<G>> {

	final List<Org<G>> pop;

	public Population() {
		this.pop = new ArrayList<>();
	}

	public Collection<Org<G>> asCollection() {
		return this.pop;
	}

	public Stream<Org<G>> stream() {
		return this.pop.stream();
	}

	public void add(Org<G> o) {
		this.addAndCheck(o);
	}

	public void addAll(Collection<Org<G>> o) {
		this.addAndCheck(o);
	}

	public void addAll(Population<G> o) {
		this.addAll(o.pop);
	}

	public boolean addAndCheck(Org<G> o) {
		return this.pop.add(o);
	}

	public boolean addAndCheck(Collection<Org<G>> o) {
		return this.pop.addAll(o);
	}

	public void create(OrganismOp<G> ofactory, G g) {
		this.addAndGet(ofactory, g);
	}

	public Optional<Org<G>> addAndGet(OrganismOp<G> ofactory, G g) {
		Org<G> newborn = ofactory.apply(g);
		boolean success = this.addAndCheck(newborn);
		return success
				? Optional.of(newborn)
				: Optional.empty();
	}

	public void kill(Org<G> o) {
		this.pop.remove(o);
	}

	public void killAll(Collection<Org<G>> o) {
		this.pop.removeAll(o);
	}

	public void kill(Selector<Org<G>> sel) {
		this.killAndGet(sel);
	}

	public void kill(int n, Selector<Org<G>> sel) {
		this.killAndGet(n, sel);
	}

	public Optional<Org<G>> killAndGet(Selector<Org<G>> sel) {
		Optional<Org<G>> klutz = sel.take1();
		if (klutz.isPresent()) this.kill(klutz.get());
		return klutz;
	}

	public Collection<Org<G>> killAndGet(int n, Selector<Org<G>> sel) {
		Collection<Org<G>> klutzes = sel.take(n);
		this.killAll(klutzes);
		return klutzes;
	}

	public void replace(Org<G> oold, Org<G> onew) {
		this.replaceAndCheck(oold, onew);
	}

	public void replace(Org<G> oold, OrganismOp<G> ofactory, G g) {
		this.replaceAndGet(oold, ofactory, g);
	}

	public boolean replaceAndCheck(Org<G> oold, Org<G> onew) {
		boolean success= this.addAndCheck(onew);
		if (success) {
			this.kill(oold);
			return true;
		} else {
			return false;
		}
	}

	public Optional<Org<G>> replaceAndGet(Org<G> oold, OrganismOp<G> ofactory, G g) {
		this.kill(oold);
		return this.addAndGet(ofactory, g);
	}

	/**
	 * Asexual reproduction of an organism.
	 * The genome is mutated, and a new organism is added to the population using the provided method.
	 *
	 * To also return the genome before spawning a new organism, use {@link #vegetateAndGet(MutationOp, OrganismOp, Org)}
	 * To generate the parent from a selector, use {@link #vegetate(MutationOp, OrganismOp, Selector)}
	 */
	public void vegetate(MutationOp<G> mutationf, OrganismOp<G> ofactory, Org<G> o) {
		this.vegetateAndGet(mutationf, ofactory, o);
	}

	/** @see #vegetate(MutationOp, OrganismOp, Org) */
	public void vegetate(MutationOp<G> mutationOp, OrganismOp<G> ofactory, Selector<Org<G>> goodSelOp) {
		this.vegetateAndGet(mutationOp, ofactory, goodSelOp);
	}

	/** @see #vegetate(MutationOp, OrganismOp, Org) */
	public Optional<Org<G>> vegetateAndGet(MutationOp<G> mutationf, OrganismOp<G> ofactory, Org<G> o) {
		G g = o.genome.copy();
		mutationf.mutate(g);
		return this.addAndGet(ofactory, g);
	}

	/** @see #vegetate(MutationOp, OrganismOp, Org) */
	public Optional<Org<G>> vegetateAndGet(MutationOp<G> mutationOp, OrganismOp<G> ofactory, Selector<Org<G>> goodSelOp) {
		Optional<Org<G>> parent = goodSelOp.take1();
		if (parent.isPresent()) {
			return this.vegetateAndGet(mutationOp, ofactory, parent.get());
		} else {
			return Optional.empty();
		}
	}

	/**
	 * Sexual reproduction of two organisms.
	 * The genomes of the two parents are crossed over, and a new organism is added to the population using the provided method.
	 *
	 * @see To also return the new organism (if successful), use {@link #copulateAndGet(CrossoverOp, OrganismOp, Org, Org)}
	 * @see To also mutate the genome before spawning a new organism, use {@link #copulateAndMutate(CrossoverOp, MutationOp, OrganismOp, Selector)}
	 * @see To generate two distinct parents using a selector, use {@link #copulate(CrossoverOp, OrganismOp, Selector)}
	 */
	public void copulate(CrossoverOp<G> crossf, OrganismOp<G> ofactory, Org<G> mom, Org<G> dad) {
		copulateAndGet(crossf, ofactory, mom, dad);
	}

	/** @see #copulate(CrossoverOp, OrganismOp, Org, Org) */
	public void copulate(CrossoverOp<G> crossOp, OrganismOp<G> ofactory, Selector<Org<G>> goodSelOp) {
		copulateAndGet(crossOp, ofactory, goodSelOp);
	}

	/** @see #copulate(CrossoverOp, OrganismOp, Org, Org) */
	public void copulateAndMutate(CrossoverOp<G> crossf, MutationOp<G> mutationf, OrganismOp<G> ofactory, Org<G> mom, Org<G> dad) {
		this.copulateAndMutateAndGet(crossf, mutationf, ofactory, mom, dad);
	}

	/** @see #copulate(CrossoverOp, OrganismOp, Org, Org) */
	public void copulateAndMutate(CrossoverOp<G> crossOp, MutationOp<G> mutationOp, OrganismOp<G> ofactory, Selector<Org<G>> goodSelOp) {
		this.copulateAndMutateAndGet(crossOp, mutationOp, ofactory, goodSelOp);
	}

	/** @see #copulate(CrossoverOp, OrganismOp, Org, Org)
	 * @return The new offspring if successfully added to the population. */
	public Optional<Org<G>> copulateAndGet(CrossoverOp<G> crossOp, OrganismOp<G> ofactory, Org<G> mom, Org<G> dad) {
		return copulateAndMutateAndGet(crossOp, (g, stats) -> {}, ofactory, mom, dad);
	}

	/** @see #copulate(CrossoverOp, OrganismOp, Org, Org)
	 * @return The new offspring if successfully added to the population. */
	public Optional<Org<G>> copulateAndGet(CrossoverOp<G> crossOp, OrganismOp<G> ofactory, Selector<Org<G>> goodSelOp) {
		return copulateAndMutateAndGet(crossOp, (g, stats) -> {}, ofactory, goodSelOp);
	}

	/** @see #copulate(CrossoverOp, OrganismOp, Org, Org)
	 * @return The new offspring if successfully added to the population. */
	public Optional<Org<G>> copulateAndMutateAndGet(CrossoverOp<G> crossf, MutationOp<G> mutationf, OrganismOp<G> ofactory, Org<G> mom, Org<G> dad) {
		G g1 = mom.genome.copy();
		G g2 = dad.genome.copy();
		crossf.accept(g1, g2);
		mutationf.mutate(g1);
		return this.addAndGet(ofactory, g1);
	}

	/** @see #copulate(CrossoverOp, OrganismOp, Org, Org)
	 * @return The new offspring if successfully added to the population. */
	public Optional<Org<G>> copulateAndMutateAndGet(CrossoverOp<G> crossOp, MutationOp<G> mutationOp, OrganismOp<G> ofactory, Selector<Org<G>> goodSelOp) {
		Optional<Org<G>> mom = goodSelOp.take1();
		Optional<Org<G>> dad = mom.flatMap(goodSelOp::take1Excluding);

		if (mom.isPresent() && dad.isPresent()) {
			return copulateAndMutateAndGet(crossOp, mutationOp, ofactory, mom.get(), dad.get());
		} else {
			return Optional.empty();
		}
	}

//	public static final AsciiCereal<Population<Org>> ascii = new AsciiCereal<>() {
//		@Override
//		public String toAscii(Population<Org> a) {
//			return a.pop.stream()
//					.map(Org.ascii::toAscii)
//					.collect(Collectors.joining("\n"));
//		}
//		@Override
//		public Optional<Population<Org>> fromAscii(String ascii) {
//			return List.of(ascii.split("\n")).stream()
//					// Read lines as Org (optional because conversion may fail)
//					.map(Org.ascii::fromAscii)
//					// Convert stream of optionals to optional stream (Optional.empty() when any of the lines was not a valid organism)
//					.collect(Functional.iterateOptional())
//					// Convert optional stream to optional list
//					.map((Stream<Org> stream) -> stream.collect(Collectors.toList()))
//					// Convert optional list to optional population
//					.map(Population::new);
//		}
//
//	};

}
