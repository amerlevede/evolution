package alignment;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.SortedSet;
import java.util.TreeSet;

import genome.binary.BitGenomeWithHistory;
import util.Assert;
import util.IntPair;

public final class PerfectAlignment {
	
	private PerfectAlignment() {
		Assert.utilityClass();
	}
	
	/**
	 * Alignment of two sequences by shared history.
	 * This alignment will only work if there are no duplications.
	 */
	public static VarOAlignment<BitGenomeWithHistory> alignFromHistory(BitGenomeWithHistory a, BitGenomeWithHistory b) {
		
		List<SortedSet<IntPair>> segments = new ArrayList<>();
		SortedSet<IntPair> segment = null;
		int segmenti = -1;
		int otheri = -2;
		
		for (int i=0; i<a.size(); i++) {
			OptionalInt matchMaybe = b.homologsOf(a, i).findFirst();
			if (matchMaybe.isEmpty()) continue;
			int match = matchMaybe.getAsInt();
			
			if (match != otheri + 1) {
				if (segmenti != -1) segments.add(segment);
				segmenti++;
				segment = new TreeSet<>();
			}
			segment.add(IntPair.of(i, match));
			otheri = match;
		}
		if (segment != null) segments.add(segment);
		VarOAlignment<BitGenomeWithHistory> result = new VarOAlignment<>(segments, a, b);
		return result.simplify();
	}

//	public static VarOAlignment<BitGenomeWithHistory> alignFromHistory(BitGenomeWithHistory a, BitGenomeWithHistory b) {
//		List<SortedSet<IntPair>> segments = new ArrayList<>();
//	
//		int segmenti = 0;
//		List<IntPair> segmentStarts = new ArrayList<IntPair>(List.of(IntPair.of(0, 0)));
//	
//		while (segmenti < segmentStarts.size()) {
//			int ia = segmentStarts.get(segmenti).x;
//			int ib = segmentStarts.get(segmenti).y;
//			segmenti++;
//			SortedSet<IntPair> cursegment = new TreeSet<>();
//			while (ia < a.size() && ib < b.size()) {
//				// End this segment if overlapping existing segment
//				final int iafinal = ia;
//				final int ibfinal = ib;
//				if (segments.stream().anyMatch(segment -> segment.stream().anyMatch(pair -> pair.x == iafinal || pair.y == ibfinal))) {
//					ia = a.size();
//					ib = b.size();
//					break;
//				}
//				// Add match if identical, gap if unique
//				if (a.getId(ia) == b.getId(ib)) {
//					cursegment.add(IntPair.of(ia, ib));
//	    			ia++;
//	    			ib++;
//				} else if (b.homologsOf(a, ia).count() == 0) {
//	    			ia++;
//	    		} else if (a.homologsOf(b, ib).count() == 0) {
//	    			ib++;
//	    		// Break segment if ambiguous
//	    		} else break;
//			}
//			segments.add(cursegment);
//			final int ibfinal = ib;
//			final int iafinal = ia;
//			// Segments end when both bits have at least one homolog (and they are not homologous to each other)
//			// So both sides are starts of new segments
//			if (ia < a.size()) b.homologsOf(a, ia).filter(jb -> jb > ibfinal).findFirst().ifPresent(jb -> segmentStarts.add(IntPair.of(iafinal, jb)));
//			if (ib < b.size()) a.homologsOf(b, ib).filter(ja -> ja > iafinal).findFirst().ifPresent(ja -> segmentStarts.add(IntPair.of(ja, ibfinal)));
//		}
//	
//		VarOAlignment<BitGenomeWithHistory> result = new VarOAlignment<>(segments, a, b);;
//		return result.simplify();
//	}
	

}
