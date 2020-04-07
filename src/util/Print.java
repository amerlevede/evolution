package util;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class Print {
	
	private Print() {
		Assert.utilityClass();
	}
	
	public static String matrix(int[][] m) {
		return Stream.of(m).map(row ->
			IntStream.of(row).mapToObj(v -> String.format("% 5d ", v)).collect(Collectors.joining("|", "|", "|")))
		.collect(Collectors.joining("\n"));
	}

}
