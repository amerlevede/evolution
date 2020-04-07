package fitness;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class TravellingSalesmanTest {

	public static final List<String> TSP_PROBLEMS = List.of(
			"a280.tsp", "ali535.tsp", "att48.tsp", "att532.tsp", "bayg29.tsp", "bays29.tsp", "berlin52.tsp", "bier127.tsp", "brazil58.tsp", "brd14051.tsp", "brg180.tsp", "burma14.tsp", "ch130.tsp", "ch150.tsp", "d1291.tsp", "d1655.tsp", "d18512.tsp", "d198.tsp", "d2103.tsp", "d493.tsp", "d657.tsp", "dantzig42.tsp", "dsj1000.tsp", "eil101.tsp", "eil51.tsp", "eil76.tsp", "fl1400.tsp", "fl1577.tsp", "fl3795.tsp", "fl417.tsp", "fnl4461.tsp", "fri26.tsp", "gil262.tsp", "gr120.tsp", "gr137.tsp", "gr17.tsp", "gr202.tsp", "gr21.tsp", "gr229.tsp", "gr24.tsp", "gr431.tsp", "gr48.tsp", "gr666.tsp", "gr96.tsp", "hk48.tsp", "kroA100.tsp", "kroA150.tsp", "kroA200.tsp", "kroB100.tsp", "kroB150.tsp", "kroB200.tsp", "kroC100.tsp", "kroD100.tsp", "kroE100.tsp", "lin105.tsp", "lin318.tsp", "linhp318.tsp", "nrw1379.tsp", "p654.tsp", "pa561.tsp", "pcb1173.tsp", "pcb3038.tsp", "pcb442.tsp", "pla33810.tsp", "pla7397.tsp", "pla85900.tsp", "pr1002.tsp", "pr107.tsp", "pr124.tsp", "pr136.tsp", "pr144.tsp", "pr152.tsp", "pr226.tsp", "pr2392.tsp", "pr264.tsp", "pr299.tsp", "pr439.tsp", "pr76.tsp", "rat195.tsp", "rat575.tsp", "rat783.tsp", "rat99.tsp", "rd100.tsp", "rd400.tsp", "rl11849.tsp", "rl1304.tsp", "rl1323.tsp", "rl1889.tsp", "rl5915.tsp", "rl5934.tsp", "si1032.tsp", "si175.tsp", "si535.tsp", "st70.tsp", "swiss42.tsp", "ts225.tsp", "tsp225.tsp", "u1060.tsp", "u1432.tsp", "u159.tsp", "u1817.tsp", "u2152.tsp", "u2319.tsp", "u574.tsp", "u724.tsp", "ulysses16.tsp", "ulysses22.tsp", "usa13509.tsp", "vm1084.tsp", "vm1748.tsp",
			"br17.atsp", "ft53.atsp", "ft70.atsp", "ftv170.atsp", "ftv33.atsp", "ftv35.atsp", "ftv38.atsp", "ftv44.atsp", "ftv47.atsp", "ftv55.atsp", "ftv64.atsp", "ftv70.atsp", "kro124p.atsp", "p43.atsp", "rbg323.atsp", "rbg358.atsp", "rbg403.atsp", "rbg443.atsp", "ry48p.atsp"
			);

	@Test
	public void testImportCities_2dcoords() {
		for (String problem : TSP_PROBLEMS) {
			if (!TravellingSalesman.tsplib_coordtype(problem).equals(Optional.of("EUC_2D"))) continue;

			int n = TravellingSalesman.tsplib_problemSize(problem);

			assertEquals(n, TravellingSalesman.tsplib_cities(problem).size());
		}
	}

	@Test
	public void testImportCities_FULL_MATRIX() {
		for (String problem : TSP_PROBLEMS) {
			if (!TravellingSalesman.tsplib_coordtype(problem).equals(Optional.of("FULL_MATRIX"))) continue;

			int n = TravellingSalesman.tsplib_problemSize(problem);

			assertEquals(n*n, TravellingSalesman.tsplib_distances(problem).length);
		}
	}

}
