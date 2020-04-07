package fitness;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import genome.integer.IntegerGenome;
import util.Assert;
import util.DoublePair;
import util.Functional;

/**
 * Utility class giving access to implementations of the travelling salesman/woman/other fitness function.
 * The genome is read as a sequence of visits to cities with a given distance matrix. The fitness is then +1 for each unique city visited, - the distance between each two subsequent visits (times a distance weight factor).
 *
 * @author adriaan
 *
 */
public final class TravellingSalesman {

	private TravellingSalesman() {
		Assert.utilityClass();
	}

	@FunctionalInterface
	public interface DistanceFunction {
		double distance(int a, int b);

		static DistanceFunction fromCoords(List<DoublePair> cities) {
			return (a, b) -> DoublePair.distance(cities.get(a), cities.get(b));
		}

		static DistanceFunction fromMatrix(double[][] cities) {
			return (a, b) -> cities[a][b];
		}

		static DistanceFunction fromMatrix(double[] cities) {
			final int len = (int)Math.sqrt(cities.length);
			return (a, b) -> cities[a*len + b];
		}

		/**
		 * Return a "cyclic" version of the distance function. This adds a new node 0 with distance 0 to all other points.
		 * Any genome of size n+1 analysed with distance function makeCyclic(f) is equivalent to a genome of size n with distance function f, if we let 0 be the starting point.
		 */
		static DistanceFunction makeCyclic(DistanceFunction distf) {
			return (a, b) -> a==0 || b==0 ? 0 : distf.distance(a-1, b-1);
		}
	}

	public static List<DoublePair> randomCities(Random rng, int dim) {
		return Functional.randoms(rng).map(r -> DoublePair.of(r.nextDouble(), r.nextDouble())).limit(dim).collect(Collectors.toList());
	}

	public static List<DoublePair> grid(int dim) {
		return IntStream.range(0,dim)
				.mapToObj(i ->
					IntStream.range(0,dim).mapToObj(j ->
						DoublePair.of(i,j)))
				.flatMap(x->x)
				.collect(Collectors.toList());
	}

	/**
	 * A non-cyclic travelling salesman fitness function.
	 * @see #cyclic(DistanceFunction, double)
	 */
	public static <G extends IntegerGenome<G>> FitnessFunction<G> nonCyclic(DistanceFunction distf, double distanceFactor) {
		FitnessFunction<G> cycl = cyclic(distf, distanceFactor);
		return (g) -> {
			double fitness = cycl.applyAsDouble(g);
			if (g.size() > 1) fitness += distf.distance(g.get(g.size()-1),  g.get(0));
			return fitness;
		};
	}

	/**
	 * A travelling salesman fitness function. The fitness of a genome is negative the sum of the distances between all successive pairs in the genome. A fitness bonus is added for each distinct city that is reached.
	 * The trajectory is evaluated in a cyclic way, meaning that the fitness includes the distance between the first and last city in the trajectory. See {@link #nonCyclic(DistanceFunction, double)} for the non-cyclic version.
	 * @param distf - The distance function to use for calculating the cost of travelling between any two cities
	 * @param distanceFactor - The bonus fitness for each distinct city that is visited. (use 0 for no bonus)
	 */
	public static <G extends IntegerGenome<G>> FitnessFunction<G> cyclic(DistanceFunction distf, double distanceFactor) {
		if (distanceFactor == 0) {
			return (g) -> {
				double fitness = 0;

				if (g.size() > 1) fitness -= distf.distance(g.get(g.size()-1), g.get(0));
				for (int i=1; i<g.size(); i++) {
					fitness -= distf.distance(g.get(i-1), g.get(i));
				}

				return fitness;
			};
		} else {
			return (g) -> {
				boolean[] visits = new boolean[g.size()];
				double fitness = 0;

				visits[g.get(0)] = true;
				fitness += distanceFactor;
				if (g.size() > 1) fitness -= distf.distance(g.get(g.size()-1), g.get(0));

				for (int i=1; i<g.size(); i++) {
					int city = g.get(i);
					fitness -= distf.distance(g.get(i-1), city);
					if (!visits[city]) {
						visits[city] = true;
						fitness += distanceFactor;
					}
				}

				return fitness;
			};
		}
	}

//	/**
//	 * A travelling salesperson fitness function where city visits are read from the genome by looking for the supplied list of tags. The ith tag represents a visit to the ith city in the distance matrix.
//	 */
//	public static <G extends BitGenome> FitnessFunction<G> sparseTags(List<G> tags, double[][] distanceMatrix, double distanceFactor) {
//		return (g) -> {
//			int visitedN = 0;
//			int[] visits = new int[g.size()]; // Valued j+1 at position i if g contains tag j at index i, and 0 otherwise
//			for (int j=0; j<tags.size(); j++) {
//				List<Integer> positions = g.findAllOverlapping(tags.get(j));
//				for (int i=0; i<positions.size(); i++) {
//					if (i==0) visitedN++;
//					visits[positions.get(i)] = j+1;
//				}
//			}
//
//			double distance = 0;
//			int lastTag = -1;
//			for (int i=0; i<visits.length; i++) {
//				if (visits[i] != 0) {
//					if (lastTag != -1) distance += distanceMatrix[visits[i]-1][lastTag];
//					lastTag = visits[i]-1;
//				}
//			}
//
//			return ((double)visitedN) - distance*distanceFactor;
//		};
//	}
//
//	/**
//	 * A travelling salesperson fitness function where city visits are read from the genome as occurrences of a consensus sequence followed by a Gray-coded number representing the index in the distance matrix.
//	 */
//	public static <G extends BitGenome> FitnessFunction<G> numberTags(double[][] distanceMatrix, double distanceFactor, G consensus) {
//		int digits = (int)Math.ceil(Math.log(distanceMatrix.length) / Math.log(2));
//		Gene.Reader<BitGenome,Integer> reader = NumberGene.readIntWithDigits(digits);
//		return (g) -> {
//			Iterable<Integer> visits = Gene.Reader.scan(consensus, g, reader)
//					.map(Gene::getValue)
//					.filter(v->v<distanceMatrix.length)
//					::iterator;
//			boolean[] visited = new boolean[distanceMatrix.length];
//			int visitedN = 0;
//			double distance = 0;
//			int lastValue = -1;
//			for (int visit : visits) {
//				if (!visited[visit]) visitedN++;
//				if (lastValue != -1) distance += distanceMatrix[visit][lastValue];
//				visited[visit] = true;
//				lastValue = visit;
//			}
//			return ((double)(visitedN)) - distance*distanceFactor;
//		};
//	}

	private static BufferedReader tsplib_read(String problem) throws IOException {
		InputStreamReader datastream;
		try {
			datastream = new InputStreamReader(TravellingSalesman.class.getResourceAsStream(problem), "UTF-8");
		} catch (NullPointerException e) {
			datastream = new FileReader("assets/fitness/"+problem);
		}
		BufferedReader data = new BufferedReader(datastream);
		return data;
	}

	public static Optional<String> tsplib_coordtype(String problem) {
		try {
			BufferedReader data = tsplib_read(problem);

			Optional<String> result = data.lines()
				.filter(r -> r.startsWith("EDGE_WEIGHT_TYPE"))
				.findFirst()
				.map(r -> r.split(":")[1].strip());

			if (result.equals(Optional.of("EXPLICIT"))) {
				result = data.lines()
				.filter(r -> r.startsWith("EDGE_WEIGHT_FORMAT"))
				.findFirst()
				.map(r -> r.split(":")[1].strip());
			}

			return result;
		}
		catch (FileNotFoundException e) {
			throw new IllegalArgumentException(problem+" is not a valid TSPLIB problem.");
		} catch (IOException e) {
			throw new IllegalStateException("Something went wrong with IO:\n" + e);
		}
	}

	public static List<DoublePair> tsplib_cities(String problem) {
		try {
			BufferedReader data = tsplib_read(problem);

			String num = "((?:-|\\+)?\\d*(?:\\.\\d+)?(?:e(?:-|\\+)\\d+)?)";
			Pattern coord = Pattern.compile("\\s*\\d+\\s+"+num+"\\s+"+num+"\\s*");

			List<DoublePair> result = data.lines()
				.map(coord::matcher)
				.filter(Matcher::matches)
				.map(r -> DoublePair.of(Double.valueOf(r.group(1)), Double.valueOf(r.group(2))))
				.collect(Collectors.toCollection(ArrayList::new));

			data.close();

			return result;
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException(problem+" is not a valid TSPLIB problem.");
		} catch (IOException e) {
			throw new IllegalStateException("Something went wrong with IO:\n" + e);
		}
	}

	public static DistanceFunction tsplib_distance_EUC_2D(String problem) {
		List<DoublePair> cities = tsplib_cities(problem);
		return DistanceFunction.fromCoords(cities);
	}

	public static double[] tsplib_distances(String problem) {
		try {
			BufferedReader data = tsplib_read(problem);

			String num = "(?:-|\\+)?\\d*(?:\\.\\d+)?(?:e(?:-|\\+)\\d+)?";
			Pattern numPattern = Pattern.compile(num);
			Pattern numsPattern = Pattern.compile("(?:"+num+"\\s*)+");

			double[] distances = data.lines()
				.dropWhile(s -> !s.startsWith("EDGE_WEIGHT_SECTION")).skip(1)
				.takeWhile(s -> numsPattern.matcher(s).matches())
				.map(numPattern::matcher)
				.flatMapToDouble(r -> allGroups(r).mapToDouble(Double::valueOf))
				.toArray();

			return distances;

		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException(problem+" is not a valid TSPLIB problem.");
		} catch (IOException e) {
			throw new IllegalStateException("Something went wrong with IO:\n" + e);
		}
	}

	public static DistanceFunction tsplib_distance_FULL_MATRIX(String problem) {
		double[] distances = tsplib_distances(problem);
		return DistanceFunction.fromMatrix(distances);
	}

	static Stream<String> allGroups(Matcher m) {
		Stream.Builder<String> result = Stream.builder();
		while (m.find()) {
			if (!m.group().isEmpty()) result.accept(m.group());
		}
		return result.build();
	}

	public static DistanceFunction tsplib_distance(String problem) {
		Optional<String> type = tsplib_coordtype(problem);
		if (type.isPresent()) {
			switch (type.get()) {
			case "EUC_2D":
				return tsplib_distance_EUC_2D(problem);
			case "FULL_MATRIX":
				return tsplib_distance_FULL_MATRIX(problem);
			default:
				throw new IllegalArgumentException("Cannot read tsplib problem "+problem+": invalid coord type "+type.get());
			}
		} else {
			throw new IllegalArgumentException("Cannot read tsplib problem "+problem+": no coord type specified in file");
		}
	}

	public static int tsplib_problemSize(String problem) {
		try {
			BufferedReader data = tsplib_read(problem);

			return Integer.valueOf(data.lines()
				.filter(r -> r.startsWith("DIMENSION"))
				.findFirst()
				.map(r -> r.split(":")[1].strip())
				.get());

		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException(problem+" is not a valid TSPLIB problem.");
		} catch (IOException e) {
			throw new IllegalStateException("Something went wrong with IO:\n" + e);
		}
//		Matcher matcher = Pattern.compile("[1234567890]+").matcher(problem);
//		matcher.find();
//		int n = Integer.valueOf(matcher.group());
//		return n;
	}

}
