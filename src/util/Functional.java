package util;

import java.util.Optional;
import java.util.Random;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class Functional {
	
	private Functional() {
		Assert.utilityClass();
	}
	
	/**
	 * Convert a Stream of Optional values to an Optional containing a Stream.
	 * The resulting Optional is empty iff any of the Optionals in the given Stream is empty (thus, the concept of an empty Optional as a "failure" is propagated to the level of the stream).
	 * Use: {@code Optional<Stream<T>> result = (Stream<Optional<T>> mystream).collect(Functional.iterateOptional())}
	 */
	public static <T> Collector<Optional<T>,?,Optional<Stream<T>>> iterateOptional() {
		return Collectors.reducing(
				Optional.of(Stream.empty()),
				(opt) -> opt.map(Stream::of),
				(opta, optb) -> opta.flatMap(a -> optb.map(b -> Stream.<T>concat(a, b)))
				);
	}
	
	/**
	 * @see #randoms(long)
	 */
	public static Stream<Random> randoms(Random init) {
		return LongStream
				.iterate(init.nextLong(), (seed) -> new Random(seed).nextLong())
				.mapToObj(Random::new);
	}

	/**
	 * Generate an ordered, sequential stream of Randoms.
	 * Used to avoid a potential issue because Stream.generate is not guaranteed to return results in any particular order and thus may result in different results being given for the same seed.
	 */
	public static Stream<Random> randoms(long seed) {
		return randoms(new Random(seed));
	}
	
	public static <V> Stream<V> streamOfIterable(Iterable<V> iter) {
		return StreamSupport.stream(iter.spliterator(), false);
	}
		
}
