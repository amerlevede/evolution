package population;

import java.util.function.Function;

public interface OrganismOp<G> extends Function<G,Org<G>> {
	
	@Override
	Org<G> apply(G t);

}
