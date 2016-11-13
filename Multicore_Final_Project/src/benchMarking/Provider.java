package benchMarking;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import interfacepackage.ParallelPriorityQueue;

public class Provider extends Thread{
	  int Id;
	  ParallelPriorityQueue queue;
	  CyclicBarrier gate;
	  int[] counts;
	  Random r = new Random();
	  int[] valuelist;
	  
	  public Provider(int Id, ParallelPriorityQueue queue, int[] counts, CyclicBarrier gate, int[] valuelist) {
		this.Id = Id;
	    this.queue = queue;
	    this.counts = counts;
	    this.gate = gate;
	    this.valuelist = valuelist;
	  }
	  
	  public void run() {
//		  int item = 0;
		  try {
			  int i = Math.abs(r.nextInt());
//			  System.out.println("threads started");
			  gate.await();
//			  System.out.println("passed gate");
			  before = Instant.now().toEpochMilli();
//			  System.out.println(running);
			  while(running){
				  queue.add(valuelist[(++i) % 5000000]);
				  counts[Id] = counts[Id] + 1;
			  }
//			  return Integer.valueOf(1);
		  } catch (InterruptedException | BrokenBarrierException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
		  }

	  }
}
