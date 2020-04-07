package util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.DoubleSummaryStatistics;

import org.junit.jupiter.api.Test;

import genome.RandomInit;

class ContinuousDistributionTest extends RandomInit {
	
	@Test
	public void testPowerLawAvg_min1() {
		assertEquals(144.62,  ContinuousDistribution.powerLawAvgInclusiveEnd(-1, 1, 1000), 0.01);
		assertEquals(187.795, ContinuousDistribution.powerLawAvgInclusiveEnd(-1, 5, 1000), 0.01);
	}
	
	@Test
	public void testPowerLawAvg_min2() {
		assertEquals(6.91467, ContinuousDistribution.powerLawAvgInclusiveEnd(-2, 1, 1000), 0.01);
		assertEquals(26.6247, ContinuousDistribution.powerLawAvgInclusiveEnd(-2, 5, 1000), 0.01);
	}
	
	@Test
	public void testPowerLawAvg() {
		assertEquals(31.6228, ContinuousDistribution.powerLawAvgInclusiveEnd(-1.5, 1, 1000), 0.01);
		assertEquals(70.7107, ContinuousDistribution.powerLawAvgInclusiveEnd(-1.5, 5, 1000), 0.01);
	}
	
	@Test
	public void testPowerLaw_mean_min1() {
		double power = -1;
		int x0 = 1;
		int x1 = 1000;
		int sample = 1000000;
		double numerical = randoms().mapToDouble(r -> ContinuousDistribution.getPowerLaw(r, power, x0, x1)).limit(sample).average().getAsDouble();
		double analytical = ContinuousDistribution.powerLawAvg(power, x0, x1); 
		assertEquals(analytical, numerical, analytical/100.);
	}
	
	@Test
	public void testPowerLaw_mean_min2() {
		double power = -2;
		int x0 = 1;
		int x1 = 1000;
		int sample = 1000000;
		double numerical = randoms().mapToDouble(r -> ContinuousDistribution.getPowerLaw(r, power, x0, x1)).limit(sample).average().getAsDouble();
		double analytical = ContinuousDistribution.powerLawAvg(power, x0, x1); 
		assertEquals(analytical, numerical, analytical/100.);
	}
	
	@Test
	public void testPowerLaw_mean() {
		double power = -1.5;
		int x0 = 1;
		int x1 = 1000;
		int sample = 1000000;
		double numerical = randoms().mapToDouble(r -> ContinuousDistribution.getPowerLaw(r, power, x0, x1)).limit(sample).average().getAsDouble();
		double analytical = ContinuousDistribution.powerLawAvg(power, x0, x1); 
		assertEquals(analytical, numerical, analytical/100.);
	}
	
	@Test
	public void testPowerLaw_bounds_min1() {
		double power = -1;
		int x1 = 100;
		int sample = 1000000;
		{
		int x0 = 1;
		DoubleSummaryStatistics stats = randoms().mapToDouble(r -> ContinuousDistribution.getPowerLaw(r, power, x0, x1)).limit(sample).summaryStatistics();
		assertEquals(x0, stats.getMin(), .01);
		assertEquals(x1-1, stats.getMax(), .01);
		}
		{
		int x0 = 5;
		DoubleSummaryStatistics stats = randoms().mapToDouble(r -> ContinuousDistribution.getPowerLaw(r, power, x0, x1)).limit(sample).summaryStatistics();
		assertEquals(x0, stats.getMin(), .01);
		assertEquals(x1-1, stats.getMax(), .01);
		}
	}
	
	@Test
	public void testPowerLaw_bounds_min2() {
		double power = -2;
		int x1 = 100;
		int sample = 1000000;
		{
		int x0 = 1;
		DoubleSummaryStatistics stats = randoms().mapToDouble(r -> ContinuousDistribution.getPowerLaw(r, power, x0, x1)).limit(sample).summaryStatistics();
		assertEquals(x0, stats.getMin());
		assertEquals(x1-1, stats.getMax());
		}
		{
		int x0 = 5;
		DoubleSummaryStatistics stats = randoms().mapToDouble(r -> ContinuousDistribution.getPowerLaw(r, power, x0, x1)).limit(sample).summaryStatistics();
		assertEquals(x0, stats.getMin());
		assertEquals(x1-1, stats.getMax());
		}
	}
	
	@Test
	public void testPowerbounds_mean() {
		double power = -1.5;
		int x1 = 100;
		int sample = 1000000;
		{
		int x0 = 1;
		DoubleSummaryStatistics stats = randoms().mapToDouble(r -> ContinuousDistribution.getPowerLaw(r, power, x0, x1)).limit(sample).summaryStatistics();
		assertEquals(x0, stats.getMin());
		assertEquals(x1-1, stats.getMax());
		}
		{
		int x0 = 5;
		DoubleSummaryStatistics stats = randoms().mapToDouble(r -> ContinuousDistribution.getPowerLaw(r, power, x0, x1)).limit(sample).summaryStatistics();
		assertEquals(x0, stats.getMin());
		assertEquals(x1-1, stats.getMax());
		}
	}

}
