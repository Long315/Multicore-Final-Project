package benchMarking;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.PriorityBlockingQueue;

import lockfreepriorityqueue.linearizable.LockFreePriorityQueueWrapper;
import lockfreepriorityqueue.nonlinearlizable.SkipQueue;
import multiqueue.MultiQueue;

public class RankErrorAndDelay {
	public int[] rankError(int c) {
		int loopCount = 100;
		int[] errors = new int[loopCount];
		for (int loop = 0; loop < loopCount; loop++) {
			MultiQueue queue = new MultiQueue(c, 1, 2);
//			LockFreePriorityQueueWrapper queue = new LockFreePriorityQueueWrapper();
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
			errors[loop] = rankerror;
		}
		return errors;
	}
	
	public int[] delay(int c) {
		int loopCount = 100;
		int[] delays = new int[loopCount];
		for (int loop = 0; loop < loopCount; loop++) {
			MultiQueue queue = new MultiQueue(c, 1, 2);
//			LockFreePriorityQueueWrapper queue = new LockFreePriorityQueueWrapper();
			PriorityBlockingQueue<Integer> defaultQueue = new PriorityBlockingQueue<>();
			Random r = new Random();
			int N = 10000;
			for (int i = 0 ; i < N; i++) {
				int cur = r.nextInt();
				queue.add(cur);
				defaultQueue.add(cur);
			}
			int delay = 0;
			int target = defaultQueue.poll();
			while (target != queue.poll()) delay++;
			delays[loop] = delay;
		}
		return delays;
	}
	
	public double getAvg(int[] data) {
		int len = data.length;
		int sum = 0;
		for (int i = 0; i < len; i++) {
			sum += data[i];
		}
		return sum / (double) len;
	}
	
	public double[] getAnalytic(int[] data) {
		int len = data.length;
		double average  = getAvg(data);
		double median = len % 2 == 0 ? (data[len/2 - 1] + data[len/2]) / 2.0 : data[(len + 1) / 2 - 1];
		int midlen = (len + 1) / 2;
		double q1 = midlen % 2 == 0 ? (data[midlen/2 - 1] + data[midlen/2]) / 2.0 : data[(midlen + 1) / 2 - 1];
		double q3 = midlen % 2 == 0 ? (data[midlen/2 + midlen - 1] + data[midlen/2 + midlen]) / 2.0 : data[(midlen + 1) / 2 - 1 + midlen];
		Arrays.sort(data);
		return new double[]{average, data[0], q1, median, q3, data[len - 1]};
	}
	
	public double[][] rankErrorofDiffC(){
		double[][] datapoints = new double[7][7];
		
		for (int i = 0; i < 7; i++) {
			datapoints[0][i] = i + 2;
		}
		
		for (int i = 0; i < 7; i++) {
			double[] analytic = getAnalytic(rankError(i + 2));
			for (int j = 1; j < 7; j++) datapoints[j][i] = analytic[j - 1];
		}
		
		return datapoints;
	}
	
	public int[][] dataPoints(boolean isDelay){
		int[][] data = new int[100][7];
		for (int i = 0; i < 7; i ++) {
			int[] newdata = isDelay? delay(i + 2) : rankError(i + 2);
			for (int j = 0; j < 100; j++) data[j][i] = newdata[j];
		}
		return data;
	}
	
	public static void main(String args[]) {
		String postfix = Long.toString(System.currentTimeMillis() / 1000 % 10000);
		RankErrorAndDelay rd = new RankErrorAndDelay();
//		ThroughPut.write2file(rd.rankErrorofDiffC(), "Analytic_rankerror_" + postfix);
		ThroughPut.write2file(rd.dataPoints(false), "RankError_" + postfix);
//		ThroughPut.write2file(rd.dataPoints(true), "Delay_" + postfix);
	}

}
