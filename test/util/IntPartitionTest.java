package util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class IntPartitionTest {

	@Test
	public void testDisconnected() {
		int n = 5;
		IntPartition p = IntPartition.disconnected(n);

		for (int i=0; i<n; i++) {
			assertEquals(i, p.head(i));
		}
		for (int i=0; i<n; i++) {
			assertEquals(i, p.head(i));
		}
	}

}
