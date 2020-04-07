package util;

@FunctionalInterface
public interface ThrowingFunction<A,B,E extends Exception> {
	
	public B apply(A arg) throws E;

}
