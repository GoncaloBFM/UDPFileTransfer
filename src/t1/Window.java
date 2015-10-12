package t1;

import java.util.TreeMap;

/**
 * Created by gbfm on 10/12/15.
 */
public class Window {
	private TreeMap<Long, WindowSlot> windowSlots;
	private int capacity;

	public Window(int capacity) {
		this.windowSlots =  new TreeMap<Long, WindowSlot>();
		this.capacity = capacity;
	}

	public synchronized void addToWindow(WindowSlot elem){

		while(this.isFull())
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}


		this.windowSlots.put(elem.getPacket().getBlockSeqN(), elem);
	}

	public void setACK(long seqN){
		WindowSlot ws = this.windowSlots.get(seqN);
		if(ws == null) return;

		ws.setAcked();

		if(isFirstAcked()) {
			pollFirst();
			this.notifyAll();
		}
	}

	public WindowSlot pollFirst(){
		return this.windowSlots.pollFirstEntry().getValue();
	}

	public boolean isFull(){
		return this.size() >= this.capacity;
	}

	public int size(){
		return this.windowSlots.size();
	}

	public boolean isFirstAcked(){
		return this.windowSlots.firstEntry().getValue().isAcked();
	}

	public TftpPacket getPacket(long seqN){
		return this.windowSlots.get(seqN).getPacket();
	}

}
