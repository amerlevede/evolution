package selection;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A generator of organisms.
 *
 * This is used to encapsulate selection mechanism of organisms of a population (though there is no requirement that organisms are part of a population, a Selector can also generate new organisms for example).
 * Implementations may choose to have some dependency between the generated organisms (for example only giving distinct organisms), and may choose to return a fniite or infinite stream.
 *
 * Optionally, streams can be parameterized by one or more organisms.
 * These organisms must be excluded from the resulting stream, but implementations may also pose additional constraints (for example geographical proximity).
 *
 * Utility methods depends on the stream methods and should probably not be overridden.
 *
 * @author adriaan
 * @param <O> - Organism class
 */
@FunctionalInterface
public interface Selector<O> extends Supplier<Stream<O>> {

	@Override
	Stream<O> get();

	default Stream<O> getExcluding(O other) {
		return this.get().filter((o) -> !o.equals(other));
	}

	default Stream<O> getExcluding(Collection<O> others) {
		return this.get().filter((o) -> !others.contains(o));
	}



	default Optional<O> take1() {
		return this.get().findFirst();
	}

	default Collection<O> take(int n) {
		return this.get().limit(n).collect(Collectors.toList());
	}

	default Set<O> takeDistinct(int n) {
		return this.get().distinct().limit(n).collect(Collectors.toSet());
	}



	default Optional<O> take1Excluding(O other) {
		return this.getExcluding(other).findFirst();
	}

	default Collection<O> takeExcluding(int n, O other) {
		return this.getExcluding(other).limit(n).collect(Collectors.toList());
	}

	default Set<O> takeDistinctExcluding(int n, O other) {
		return this.getExcluding(other).limit(n).collect(Collectors.toSet());
	}



	default Optional<O> take1Excluding(Collection<O> others) {
		return this.getExcluding(others).findFirst();
	}

	default Collection<O> takeExcluding(int n, Collection<O> others) {
		return this.getExcluding(others).limit(n).collect(Collectors.toList());
	}

	default Set<O> takeDistinctExcluding(int n, Collection<O> others) {
		return this.getExcluding(others).limit(n).collect(Collectors.toSet());
	}



	/**
	 * Create a Selector that generates organisms from a supplier of organisms.
	 * The resulting Selector will generate endless streams based on the supplier.
	 */
	static <O> Selector<O> generate(Supplier<O> take1) {
		// Use iterate instead of generate to guarantee ordering of results is consistent (should be stable with random seed)
		return () -> Stream.iterate(take1.get(), (ignored) -> take1.get());
	}

	/**
	 * Create a Selector that generates organisms from a supplier of optional organisms.
	 * The resulting Selector will generate endless streams based on the supplier.
	 * Empty optionals are ignored in the resulting Selector.
	 */
	static <O> Selector<O> generateOptional(Supplier<Optional<O>> take1) {
		// Use iterate instead of generate to guarantee ordering of results is consistent (should be stable with random seed)
		return () -> Stream.iterate(take1.get(), (ignored) -> take1.get()).flatMap(Optional::stream);
	}

//	public O take1();
//
//	public default O take1Distinct(O other) {
//		O o = this.take1();
//		while (o.equals(other)) {
//			o = this.take1();
//		}
//		return o;
//	}
//
//	public default O take1Distinct(Collection<O> other) {
//		O o = this.take1();
//		while (other.contains(o)) {
//			o = this.take1();
//		}
//		return o;
//	}
//
//	public default Collection<O> take(int n) {
//		List<O> result = new LinkedList<>();
//		for (int i=0; i<n; i++) {
//			result.add(this.take1Distinct(result));
//		}
//		return result;
//	}
//
//	public default Collection<O> takeDistinct(int n, O other) {
//		return takeDistinct(n, List.of(other));
//	}
//
//	public default Collection<O> takeDistinct(int n, Collection<O> other) {
//		List<O> result = new LinkedList<>();
//		result.addAll(other);
//		for (int i=0; i<n; i++) {
//			result.add(this.take1Distinct(result));
//		}
//		return result.subList(other.size(), result.size());
//	}

//	public default void kill(int n) {
//		this.removeAll(this.take(n));
//	}
//
//	public default void killDistinct(int n, O other) {
//		this.removeAll(this.takeDistinct(n, other));
//	}
//
//	public default void killDistinct(int n, Collection<O> other) {
//		this.removeAll(this.takeDistinct(n, other));
//	}

}
