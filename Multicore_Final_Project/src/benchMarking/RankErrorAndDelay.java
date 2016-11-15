package benchMarking;

import java.util.Random;
import java.util.concurrent.PriorityBlockingQueue;

import lockfreepriorityqueue.linearizable.LockFreePriorityQueueWrapper;
import lockfreepriorityqueue.nonlinearlizable.SkipQueue;
import multiqueue.MultiQueue;

public class RankErrorAndDelay {
	public double rankError(int c) {
		int loopCount = 100;
		int sum = 0;
	for (int loop = 0; loop < loopCount; loop++) {
//			MultiQueue queue = new MultiQueue(c, 1);
			LockFreePriorityQueueWrapper queue = new LockFreePriorityQueueWrapper();
			PriorityBlockingQueue<Integer> defaultQueue = new PriorityBlockingQueue<>();
			Random r = new Random();
			int N = 10000;
			for (int i = 0 ; i < N; i++) {
				int cur = r.nextInt();
				queue.add(cur);
				defaultQueue.add(cur);
			}
			int rankerror = 0;
			int target = queue.poll();
			while (target != defaultQueue.poll()) rankerror++;
			sum += rankerror;
		}
		return ((double)sum) / loopCount;
	}
	
	public double[][] rankErrorofDiffC(){
		double[][] datapoints = new double[2][24];
		
		for (int i = 0; i < 24; i++) {
			datapoints[0][i] = i + 1;
		}
		
		for (int i = 0; i < 24; i++) {
			datapoints[1][i] = rankError((int)datapoints[0][i]);
			System.out.format("%d, %f\n", (int)datapoints[0][i], datapoints[1][i]);
		}
		
		return datapoints;
	}
	
	public static void main(String args[]) {
		RankErrorAndDelay rd = new RankErrorAndDelay();
		rd.rankErrorofDiffC();
	}

}
