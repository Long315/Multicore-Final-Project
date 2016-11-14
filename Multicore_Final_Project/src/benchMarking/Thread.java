package benchMarking;

public abstract class Thread extends java.lang.Thread{
	static volatile boolean running = true;
	static long before = -1;
	static long after = -1;
	public static void sta() {
		running = true;
		before = System.currentTimeMillis();
	}
	public static void stp() {
		running = false;
		after = System.currentTimeMillis();
//		System.out.println(after - before);
	};
}
