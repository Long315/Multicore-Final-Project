package benchMarking;

import java.util.Random;

import interfacepackage.ParallelPriorityQueue;

public class Producer extends Thread{
	  int Id;
	  ParallelPriorityQueue queue;
	  int[] counts;
	  Random r = new Random();
	  int[] valuelist;
	  
	  public Producer(int Id, ParallelPriorityQueue queue, int[] counts, int[] valuelist) {
		this.Id = Id;
	    this.queue = queue;
	    this.counts = counts;
//	    this.gate = gate;
	    this.valuelist = valuelist;
	    this.setPriority(MAX_PRIORITY);
	  }
	  
	  public void run() {
			  int i = Math.abs(r.nextInt());
			  while(!running);
//			  before = Instant.now().toEpochMilli();
			  while(running){
				  queue.add(valuelist[(++i) % 5000000]);
				  counts[Id] = counts[Id] + 1;
			  }
	  }
}
