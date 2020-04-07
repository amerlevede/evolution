package mutation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Data structure allowing any context where mutation is applied to remember how much of each mutation type was done exactly.
 * 
 * @author adriaan
 *
 */
public class MutationStats {
	
	public final static class MutationType {
		public final String type;
		public MutationType(String type) {
			this.type = type;
		}
		@Override
		public String toString() {
			return this.type;
		}
	}
	
	private Map<MutationType,Integer> sizes = new HashMap<>();
	private Map<MutationType,Integer> times = new HashMap<>();
	
	public void add(MutationType mutationType, int size) {
		this.sizes.compute(mutationType, (k, v) -> (v==null?0:v) + size);
		this.times.compute(mutationType, (k, v) -> (v==null?0:v) + 1);
	}
	
	public int count(MutationType mutationType) {
		return this.times.getOrDefault(mutationType, 0);
	}
	
	public int size(MutationType mutationType) {
		return this.sizes.getOrDefault(mutationType, 0);
	}
	
	public Set<MutationType> mutationTypes() {
		return this.times.keySet();
	}
	
	public String toStringOneLine() {
		StringBuilder result = new StringBuilder();
		result.append("{");
		boolean first = true;
		for (MutationType m : this.mutationTypes()) {
			if (first) first = false; else result.append(',');
			result.append(m);
			result.append(':');
			result.append(this.size(m));
			result.append('/');
			result.append(this.count(m));
		}
		result.append("}");
		
		return result.toString();
	}
	
	@Override
	public String toString() {
		if (this.mutationTypes().isEmpty()) {
			return "No mutations were recorded.";
		} else {
			StringBuilder result = new StringBuilder();
			int nameWidth = this.mutationTypes().stream().mapToInt(m->m.toString().length()).max().getAsInt();
			int sizeWidth = this.mutationTypes().stream().mapToInt(m->1+(int)Math.log10(size(m))).max().getAsInt();
			
			boolean start=true;
			for (MutationType m : this.mutationTypes()) {
				if (start) start = false; else result.append('\n');
				result.append(String.format("%"+nameWidth+"s % "+sizeWidth+"d % "+sizeWidth+"d", m, this.count(m), this.size(m)));
			}
			
			return result.toString();
		}
		
	}

}
