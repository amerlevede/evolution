package main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import util.ThrowingFunction;

/**
 * Class encapsulating functionality of the command line interface.
 * These functionalities are (a) IO, and (b) the ability to interpret command line options.
 *
 * @author adriaan
 */
public abstract class CommandLineInterface {

	public CommandLineInterface(String[] args) {
		this.args = args;
		this.output.read();
		this.logger.read();
	}

	protected final String[] args;

	protected final List<Option<?>> options = new ArrayList<>();

	/**
	 * Option class represents command line options and ways to read them as usable objects in a program.
	 * Usage is to define an Option field in a CommandLineInterface class, and access the value using {@link Option#read()}.
	 *
	 * @author adriaan
	 * @param <T> - The type of the object that should be described by this Option.
	 */
	public abstract class Option<T> {

		protected Option() {
			CommandLineInterface.this.options.add(this);
		}

		abstract public String identifier();
		abstract public Optional<T> defaultValue();
		abstract public T transform(String optionString) throws Exception;

		Optional<T> value = Optional.empty();

		public boolean isInitialized() {
			return this.value.isPresent();
		}

		public void logDefault() {
			CommandLineInterface.this.log("Read option '"+this.identifier()+"' as default (="+this.defaultValue().get()+")");
		}
		public void logValue() {
			CommandLineInterface.this.log("Read option '"+this.identifier()+"' as "+CommandLineInterface.this.readArg(this.identifier()).get()+" (="+value.get()+")");		}

		public T read() {
			if (this.value.isEmpty()) {
				this.value =
					readArg(this.identifier())
					.map((s) -> {
						try {
							return this.transform(s);
						} catch (Exception e) {
							CommandLineInterface.this.err("Illegal value for option '"+this.identifier()+"' ("+s+"):\n");
							return null; // unreachable
						}
					});

				if (value.isPresent()) {
					this.logValue();
				} else {
					if (this.defaultValue().isPresent()) {
						this.value = this.defaultValue();
						this.logDefault();
					} else {
						CommandLineInterface.this.err("No value found for mandatory option "+this.identifier());
						return null; // unreachable
					}
				}
			}
			return this.value.get();
		}
	}

	public Optional<String> readArg(String identifier) {
		return Stream.of(this.args)
					.filter((s) -> s.startsWith(identifier + "="))
					.reduce((a,b) -> b) // Get last element, thus allow overriding of options
					.map((s) -> s.replaceFirst(Pattern.quote(identifier + "="), ""));
	}

	/**
	 * Define a command line option
	 * @param <T> - The type of variable specified by the command line option
	 * @param identifier - The name of the option (i.e. identifier=value will read "value" for this option)
	 * @param defaultValue - Default value, if any (error will be thrown when attempting to read the value while default is absent and no value is given as a command line option)
	 * @param transform - Specifies how to read the string given by the user on the command line into a runtime object of the right type.
	 * @return An "Option" type, that allows reading the value supplied on the command line using {@link Option#read()}.
	 */
	public <T> Option<T> option(String identifier, Optional<T> defaultValue, ThrowingFunction<String,T,Exception> transform) {
		return new Option<T>() {
			@Override
			public String identifier() {
				return identifier;
			}
			public Optional<T> defaultValue() {
				return defaultValue;
			};
			@Override
			public T transform(String optionString) throws Exception {
				return transform.apply(optionString);
			}
		};
	}

	/**
	 * Option with no default.
	 * @see #option(String, Optional, ThrowingFunction)
	 */
	public <T> Option<T> option(String identifier, ThrowingFunction<String,T,Exception> transform) {
		return option(identifier, Optional.empty(), transform);
	}

	/**
	 * Option with default.
	 * @see #option(String, Optional, ThrowingFunction)
	 */
	public <T> Option<T> option(String identifier, T defaultValue, ThrowingFunction<String,T,Exception> transform) {
		return option(identifier, Optional.of(defaultValue), transform);
	}

	public <T> Option<T> optionWithStringDefault(String identifier, String defaultValue, ThrowingFunction<String,T,Exception> transform) {
		if (this.readArg(identifier).isPresent()) {
			// It is important not to calculate the default value if it is not needed, because calculating it might depend on e.g. some other options being specified
			return option(identifier, transform);
		} else {
			try {
				return option(identifier, transform.apply(defaultValue), transform);
			} catch (Exception e) {
				throw new IllegalArgumentException("String-valued default option value cannot be read");
			}
		}
	}

	/**
	 * Specifies an "option" type that is not actually represented on the command line.
	 * Defining an Option<T> field as an autoOption is essentially equivalent to defining a method readT(), except it has the same form as other options making later refactorization easier.
	 */
	public <T> Option<T> autoOption(Supplier<T> read) {
		return new Option<T>() {
			public String identifier() {
				return null;
			};
			public Optional<T> defaultValue() {
				return Optional.empty();
			};
			public T transform(String optionValue) {
				return read.get();
			}
			public T read() {
				this.value = Optional.of(this.value.orElseGet(read));
				return this.value.get();
			};
		};
	}

	public <T> Option<T> printedAutoOption(String id, Supplier<T> read) {
		return new Option<T>() {
			public String identifier() {
				return null;
			};
			public Optional<T> defaultValue() {
				return Optional.empty();
			};
			public T transform(String optionString) {
				return read.get();
			};
			public T read() {
				T val = read.get();
				CommandLineInterface.this.log("Calculated value "+id+" as "+val);
				return val;
			};
		};
	}

	public <T> Option<T> silentOption(Option<T> opt) {
		return new Option<T>() {
			@Override
			public Optional<T> defaultValue() {
				return opt.defaultValue();
			}
			@Override
			public String identifier() {
				return opt.identifier();
			}
			@Override
			public T transform(String optionString) throws Exception {
				return opt.transform(optionString);
			}
			@Override
			public void logDefault() {
				// Do nothing
			}
			public void logValue() {
				// Do nothing
			};
		};
	}

	public void print(Object str) {
		if (this.output.isInitialized()) {
			this.output.read().print(str.toString());
		}
	}

	/**
	 * Print output.
	 * The receiving stream is defined by the {@link #output} option.
	 */
	public void println(Object str) {
		print(str == null ? "null" : str.toString());
		print("\n");
	}

	public void println() {
		println("");
	}

	/**
	 * Log some information.
	 * The receiving stream is defined by the {@link #logger} option.
	 */
	public void log(String str) {
		if (this.logger.isInitialized()) {
			this.logger.read().println("# " + str);
		}
	}

	public void warn(String str) {
		System.err.println(str);
	}

	/**
	 * Print an error and quit.
	 * Intended to end the program more gracefully than simply throwing, but current implementation does not achieve that goal.
	 */
	public void err(String str) throws IllegalArgumentException {
		System.err.println(str);
		System.exit(1);
	}

	/**
	 * Option that allows specifying where output will be printed (with {@link #println(String)}.
	 * Using {@link #readStream(String)} allows either a file or special file handles.
	 */
	public final Option<PrintStream> output = option("output", System.out, CommandLineInterface::readStream);

	/**
	 * Option that allows specifying where logging information will be printed (with {@link #log(String)}).
	 * Using {@link #readStream(String)} allows either a file or special file handles.
	 */
	public final Option<PrintStream> logger = option("logger", System.out, CommandLineInterface::readStream);

	/**
	 * Read a printstream from a string given on the command line.
	 * Results in either a file or special file handle.
	 */
	public static PrintStream readStream(String f) throws FileNotFoundException {
		switch (f) {
		case "-":
		case "/dev/stdout":
			return System.out;
		case "/dev/stderr":
			return System.err;
		case "/dev/null":
			return new PrintStream(new OutputStream() {@Override public void write(int b) throws IOException {}});
		default:
			return new PrintStream(f);
		}
	}

	public final Option<Integer> cycles = option("cycles", Integer::valueOf);
	public final Option<Long> seed = option("seed", Long::valueOf);
	public final Option<Random> rng = autoOption(() -> new Random(this.seed.read()));

	public final Option<Boolean> dryrun = silentOption(option("dryrun", false, Boolean::valueOf));

	/**
	 * Run this Command Line Interface.
	 */
	public abstract void run(boolean dryrun);

	public final void run() {
		boolean dryrun = this.dryrun.read();
		this.run(dryrun);
	}

	public List<String> unusedVariables() {
		List<String> result = new ArrayList<>(List.of(this.args));
		result.remove(0); // first arg is main method specification and is always used

		for (Option<?> o : this.options) {
			if (o.value.isPresent()) {
				result.removeIf(arg -> arg.startsWith(o.identifier() + "="));
			}
		}
		return result;
	}

	public void warnUnusedVariables() {
		for (String arg : this.unusedVariables()) {
			warn("Warning: unused command line argument " + arg);
		}
	}

}

