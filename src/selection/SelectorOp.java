package selection;

import java.util.function.UnaryOperator;

/**
 * Functional interface representing modifications of a selector rule.
 * 
 * @author adriaan
 *
 * @param <O>
 */
public interface SelectorOp<O> extends UnaryOperator<SelectorRule<O>> {

	@Override
	SelectorRule<O> apply(SelectorRule<O> selectorRule);

}
