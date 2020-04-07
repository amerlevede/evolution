# Evolution

A genetic algorithm framework in Java.
This code is used for my research on crossovers operators.

## Compile

Create jar with

```
ant jar
```

Run with 

```
java -jar varcross.jar [main functionality] [option=value]..
```

Developed and built with `java 12.0.1`.

## Usage

The program can be run with several "main" functionalities. The most important main is `evolve`, which is used for running an evolutionary algorithm. `score` is used to calculate homology and linkage scores for crossover operators (see [this paper][1]). Others can be used for inspection of certain algorithms such as crossovers, mutations, and alignments.

The program reads modifiable aspects of the main functionality as options from the command line. All interpreted options are given as output on the first lines starting with `# `. Use `logger=/dev/null`, `logger=stderr` or `logger=` file to redirect this part of the output. 

All pseudo-random evaluations depend on an extenal seed given by `seed=`. Using the same seed should lead to identical output.

### `evolve`

Run a genetic algorithm using binary genomes. One individual is replaced in the population according to a selection scheme and reproduction operator. Number of generations is given by `cycles` option. Use `skip` option to avoid printing output ever generation.

Initialisation:
 * `length=` specify initial genome size. Initial population consists of random sequences.
 * `populationSize=` specify population size.

Selection:
 * `selectGood=` specify how to select individuals to reproduce each generation. Possible values: `tournament` (take the best out of `size` random indivudals), `roulettewheel` (sample using RWS method), `list` (deterministically take the best individual).
 * `selectBad=` specify how to select individuals to kill each generation. Same values as `selectGood`.
 * `fitness=` specify the fitness function. Possible values: `string` (alignment score compared to `target`), `substrings` (number of elements in the `targets` list that are present in the genome), `triangles` (see RBF [here][1]).
The `elitism` option excludes the worst individuals.

Mutation:
 * `snpRate=`, `indelRate=`, `transRate=` specify how much of the genome should be (on average) affected by each of the mutations single nucleotide polymorphism or bitflip, indel or insertion/deletion of segment, translocation of segment.
 * `indelPower=`, `transPower=` specify the length of segments affected by indel and translocation mutations. These lengths are drawn from a power law distribution with the specified exponent.
 * `indelMinSize=`, `transMinSize=` specify the minimum size of the mutations (the lower cutoff value of the power law).
 * `mutationtype=` specify distribution of the number of mutations. Possible options: `binomial` (number of snp, indel, and translocation mutations are drawn from a binomial distribution where n is the size of the genome and p is equal to `snpRate` for snps, or `indelRate`/`transRate` divided by the mean mutation size for indels and translocations); `exponential` (similar to `binomial` but with an exponential distribution); `exact` (ensure that exactly `*Rate` of the genome is affected by each mutation); `exact1` (ensure that exactly `*Rate` of the genome is affected by each mutation, but instead of drawing multiple indels and translocations with power-law-distributed sizes, just make one indel or translocation with the appropriate size).
 
Crossover:
 * `crossoverN=` specify a number of crossover points. Can be either a number of `uniform` to take the number from a binomial distribution with a mean value of half the smallest parent genome size (see [here][1]).
 * `crossoverProbability=` specify how often to do crossover. Asexual reproduction with mutation is applied in other cases (no mutation is applied to sexual reproduction).
 * `crossover=` specify a crossover algorithm. Possible values: `cloning` (copy one of the parents, noop), `mutate` (copy one of the parents and apply mutation), `messy` (apply messy or headless chicken algorithm), `onegap` ( [one-gap][1] algorithm), `synapsing` (synapsing variable-length crossover (SVLC) with minimum synapse size `synapseSize`), `synapsing_general` (synapsing but using a general affine local alignment instead of longest-common-substring), `global` (global alignment based crossover), `glocal` (glocal alignment crossover).
 * `scoreMatch=`, `scoreMismatch=`, `scoreGapOpen=`, `scoreGapExtend=` specify parameters for alignment where applicable.
 
## `score`

Calculate homology or linkage score. `cycles` different pairs of parents will be generated by creating one randomly and applying a mutation operator to generate the other parent. Then, crossover is applied and the score is calculated. See `evolve` documentation for mutation and crossover parameters.

`type=` is used to tell the program whether to calculate `homology` or `linkage` score.
`stat=` takes values `mean` or `all`. In the latter case, the score is given for each parent pair, along with a printout of which mutations separate the parents (i.e. size and number of snps, indels, and translocations). 

## Mutate

Generate a random genome and a mutated variant. Useful to inspect the behaviour of a set of mutation settings.

## Align

Generate two random genomes and visualise their alignment. Uses a `type=` option to differentiate between `global`, `local`, `synapsing`, `synapsing_general`, `lcss` (longest common substring), `onegap`, `glocal`, or `perfect` (which uses the known mutation history of the genomes, and is thus inaccessible in practical applications) alignment. The aligned genomes are specified using the `genomeA=` (`random`) and `genomeB=` (`random`, `mutated`, `identical`, `reversed` or `reversedmutated` comparet to genomeA) options.

## Crossover

Generate two random genomes and perform crossover. See `evolve` documentation for crossover specification. See `align` documentation for `genomeA` and `genomeB` specification.

## permutationevolve

Similar to `evolve` but using permutations instead of binary genomes. 

Instead of snps, indels, and translocations, uses `grayflips` (switching the place of two adjacent elements), `randflips` (switching the place of two random elements), `translocations` (move a swath/section in the ordering) and `transinvs` (move a swath/section in the ordering, possibly changing its orientation).

Crossover values are `cloning` or `nox` (noop), `cycle` or `cx` (cycle cross), `edgepreserving` or `epx` (algorithm that perfectly preserves permutation edges), `edgepreservingoptimal` or `epox` (algorithm that perfectly preserves permutation edges and returns the optimal offspring given a fitness TSP-like fitness function. Returns the best parent after inspecting `maxtries` possible offspring), `edgerecombination` or `erx` (edge recombination crossover).

Fitness options are `stringKT` (match a `target` permutation, fitness equal to the Kendall Tau distance), and `tsp` (travelling salesperson problem). In the case of TSP, the option `cities=` specifies the problem graph, using `random` to generate `size` random cities in the unit square, `grid` to distribute `size` cities on a rectangular grid, or the name of a TSPLIB problem (e.g. `a280.tsp`) (in the latter case, add `size=tsplib`).

## permutationcross

Perform a crossover algorithm (see `permutationevolve`) on a pair of permutations.



[1]: https://www.ncbi.nlm.nih.gov/pubmed/30605463
