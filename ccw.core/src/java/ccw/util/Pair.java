package ccw.util;

public final class Pair<E1,E2> {
	public final E1 e1;
	public final E2 e2;
	
	public Pair(E1 e1, E2 e2) {
		this.e1 = e1;
		this.e2 = e2;
	}
	
	@Override
	public String toString() {
		return "<Pair>{e1 " + e1 + ", e2 " + e2 + "}";
	}
}
