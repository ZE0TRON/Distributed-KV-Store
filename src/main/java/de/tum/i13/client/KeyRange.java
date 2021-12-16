package de.tum.i13.client;

public class KeyRange {
	public String from;
	public String to;
	public String host;
	public int port;

	/**
	 * Constructs a {@code KeyRange} with the given beginning (from), end (to),
	 * address of the server(host), and port of the server.
	 * 
	 * @param from the beginning of this {@code KeyRange}
	 * @param to   the end of this {@code KeyRange}
	 * @param host address of the server that this {@code KeyRange} belongs to.
	 * @param port port of the server that this {@code KeyRange} belongs to.
	 */
	public KeyRange(String from, String to, String host, int port) {
		this.from = from;
		this.to = to;
		this.host = host;
		this.port = port;
	}


}
