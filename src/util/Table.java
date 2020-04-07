package util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Table<X,Y,Z> {
	
	private final Set<X> xs;
	private final Set<Y> ys;
	private final Map<X,Map<Y,Z>> zs;
	private final BiFunction<X,Y,Z> fun;
	
	public Table(BiFunction<X,Y,Z> fun) {
		this.fun = fun;
		this.xs = new HashSet<>();
		this.ys = new HashSet<>();
		this.zs = new HashMap<>();
	}
	
	public void add(X x, Y y) {
		this.addX(x);
		this.addY(y);
	}
	
	public void addX(X x) {
		this.xs.add(x);
	}
	
	public void addY(Y y) {
		this.ys.add(y);
	}
	
	/**
	 * @throws NoSuchElementException - When either x or y is missing from this table.
	 */
	public Z get(X x, Y y) {
		return this.getOptional(x, y).orElseThrow(() -> new NoSuchElementException());
	}
	
	public Optional<Z> getOptional(X x, Y y) {
		if (this.xs.contains(x) && this.ys.contains(y)) {
			return Optional.of(this.getUnchecked(x,y));
		} else {
			return Optional.empty();
		}
	}
	
	public Z getOrAdd(X x, Y y) {
		this.add(x, y);
		return this.getUnchecked(x, y);
	}
	
	protected Z getUnchecked(X x, Y y) {
		return this.zs.computeIfAbsent(x, xval -> new HashMap<>())
			          .computeIfAbsent(y, yval -> this.fun.apply(x, yval));
	}
	
	/**
	 * @throws NoSuchElementException - When x is not present in this table.
	 */
	public Map<Y,Z> getX(X x) {
		return this.getOptionalX(x).orElseThrow(() -> new NoSuchElementException());
	}
	
	public Optional<Map<Y,Z>> getOptionalX(X x) {
		if (this.xs.contains(x)) {
			return Optional.of(this.getUncheckedX(x));
		} else {
			return Optional.empty();
		}
	}
	
	public Map<Y,Z> getOrAddX(X x) {
		this.addX(x);
		return this.getUncheckedX(x);
	}
	
	protected Map<Y,Z> getUncheckedX(X x) {
		return this.ys.stream().collect(Collectors.toMap(y -> y, y -> this.getUnchecked(x, y)));
	}
	
	/**
	 * @throws NoSuchElementException - When y is not present in this table.
	 */
	public Map<X,Z> getY(Y y) {
		return this.getOptionalY(y).orElseThrow(() -> new NoSuchElementException());
	}
	
	public Optional<Map<X,Z>> getOptionalY(Y y) {
		if (this.ys.contains(y)) {
			return Optional.of(this.getUncheckedY(y));
		} else {
			return Optional.empty();
		}
	}
	
	public Map<X,Z> getOrAdd(Y y) {
		this.addY(y);
		return this.getUncheckedY(y);
	}
	
	protected Map<X,Z> getUncheckedY(Y y) {
		return this.xs.stream().collect(Collectors.toMap(x -> x, x -> this.getUnchecked(x, y)));
	}
	
	public void removeX(X x) {
		this.xs.remove(x);
		this.zs.remove(x);
	}
	
	public void removeY(Y y) {
		this.ys.remove(y);
		this.zs.forEach((x, yToZ) -> yToZ.remove(y));
	}
	
	public Set<X> getXs() {
		return new HashSet<>(this.xs);
	}
	
	public Set<Y> getYs() {
		return new HashSet<>(this.ys);
	}
	
	public void removeXIf(Predicate<X> p) {
		for (X x : this.getXs()) {
			if (p.test(x)) this.removeX(x);
		}
	}
	
	public void removeXIf(BiPredicate<X,Map<Y,Z>> p) {
		for (X x : this.getXs()) {
			if (p.test(x, this.getUncheckedX(x))) this.removeX(x);
		}
	}
	
	public void removeYIf(Predicate<Y> p) {
		for (Y y : this.getYs()) {
			if (p.test(y)) this.removeY(y);
		}
	}
	
	public void removeYIf(BiPredicate<Y,Map<X,Z>> p) {
		for (Y y : this.getYs()) {
			if (p.test(y, this.getUncheckedY(y))) this.removeY(y);
		}
	}
	

}
