package benchMarking;

import java.time.Instant;

public abstract class Thread extends java.lang.Thread{
	static volatile boolean running = true;
	static long before = -1;
	static long after = -1;
	public static void sta() {
		running = true;
		before = Instant.now().toEpochMilli();
	}
	public static void stp() {
		running = false;
		after = Instant.now().toEpochMilli();
//		System.out.println(after - before);
	};
}
