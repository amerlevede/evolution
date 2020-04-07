package fitness.binary;

@Deprecated
public abstract class TrainSchedule {
//
//	private TrainSchedule() {
//		Assert.utilityClass();
//	}
//
//	private static final int DIGITS = 10;
//	private static final Gene.Reader<BitGenome,Double> reader = NumberGene.readFractionWithDigits(DIGITS);
//
//	public static <G extends BinaryGenome<G>> FitnessFunction<G> of(LinearGenome.KMP<G> driveTag, DoubleUnaryOperator target, double threshold) {
//		G coastTag = BitGenome.not(driveTag);
//		return (g) -> {
//			Iterator<Double> driveTags = Gene.Reader.scan(driveTag, g, reader).map(gene -> ((double)gene.getIndex())/((double)g.size())).iterator();
//			Iterator<Double> coastTags = Gene.Reader.scan(coastTag, g, reader).map(gene -> ((double)gene.getIndex())/((double)g.size())).iterator();
//
//			double slope = 0;
//			double y = 0;
//			double error = 0;
//			double nextDrive = driveTags.hasNext() ? driveTags.next() : 1+PRECISION;
//			double nextCoast = coastTags.hasNext() ? coastTags.next() : 1+PRECISION;
//			for (double x=0; x<=1; x+=PRECISION) {
//				while (x >= nextDrive) {
//					slope++;
//					nextDrive = driveTags.hasNext() ? driveTags.next() : 1+PRECISION;
//				}
//				while (x >= nextCoast) {
//					slope--;
//					nextCoast = coastTags.hasNext() ? coastTags.next() : 1+PRECISION;
//				}
//				error += Math.pow(target.applyAsDouble(x) - y, 2)*PRECISION;
//				y += PRECISION*slope;
//			}
//
//			return -error;
//		};
//	}

//	public FitnessFunction scheduleFitness(DoubleUnaryOperator target, Genome consensus, double threshold) {
//		return (g) -> {
//			Iterable<Gene<NumberGene>> genes = Gene.Reader.scan(consensus, g, NumberGene::read)::iterator;
//			double x = 0;
//			boolean driving = false;
//			double y = 0;
//			double cum = 0;
//			double fitness;
//			for (Gene<NumberGene> gene : genes) {
//				cum += gene.getValue().getValue();
//				if (cum > 1) break;
//				driving = !driving;
//				while (x < cum) {
//					if (Math.abs(y - target.applyAsDouble(x)) <= threshold) fitness += PRECISION;
//					y += PRECISION * (driving?+1:-1);
//					x += PRECISION;
//				}
//			}
//			while (x <= 1) {
//				if (Math.abs(y - target.applyAsDouble(x)) <= threshold) fitness += PRECISION;
//				y += PRECISION * (driving?+1:-1);
//				x += PRECISION;
//			}
//
//			return null;
//		};
//	}
//
//	public static final double PRECISION = 1./1024.;

}
