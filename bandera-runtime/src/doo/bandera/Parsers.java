package doo.bandera;

public class Parsers {
	public static int SafeParse(String what, int def) {
		try {
			return Integer.parseInt(what);
		} catch (Exception e) {
			return def;
		}
	}


	public static int SafeParse(Object what, int def) {
		return SafeParse(what != null ? what.toString() : "", def);
	}
}
