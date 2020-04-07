package main;

import java.util.stream.Stream;

import main.binary.Align;
import main.binary.Crossover;
import main.binary.Distribution;
import main.binary.GPMap;
import main.binary.Mutate;
import main.binary.Score;
import util.Assert;

/**
 * Main function.
 * The actual behavior of the program is delegated to another class, based on the first command line argument.
 * 
 * @author adriaan
 *
 */
public abstract class Main {

	private Main() {
		Assert.utilityClass();
	}

	public static void main(String[] args) {

		// Allow comma instead of space as argument separator
		args = Stream.of(args)
				.flatMap(s -> Stream.of(s.split(",")))
				.toArray(String[]::new);

		if (args.length < 1) {
			System.err.println("Must specify a main method");
			System.exit(1);
		}

		switch (args[0]) {
		case "evolve":
			new main.binary.Evolve(args).run();
			break;
		case "score":
			new Score(args).run();
			break;
		case "distribution":
			new Distribution(args).run();
			break;
		case "align":
			new Align(args).run();
			break;
		case "cross":
		case "crossover":
			new Crossover(args).run();
			break;
		case "gpmap":
			new GPMap(args).run();
			break;
		case "mutate":
			new Mutate(args).run();
			break;
		case "permutationevolve":
			new main.permutation.Evolve(args).run();
			break;
		case "permutationtime":
			new main.permutation.Timing(args).run();
			break;
		case "permutationcross":
		case "permutationcrossover":
			new main.permutation.Crossover(args).run();
			break;
		case "test":
			new Test(args).run();
			break;
		default:
			System.err.println("Must specify valid main method");
			System.exit(1);
		}
	}

}
