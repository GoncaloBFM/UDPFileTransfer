package t1;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by gbfm on 10/12/15.
 */
public class Window {
	private TreeMap<Long, WindowSlot> windowSlots = new TreeMap<Long, WindowSlot>();
	private int capacity;

	public Window(int capacity) {
		this.setCapacity(capacity);
	}

	public Window(){
		StatsU.updateConstants();
		this.setCapacity(StatsU.getOptimalWindowSize());
	}

	public synchronized void addToWindow(WindowSlot elem){
		while(this.isFull())
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}


		this.windowSlots.put(elem.getExpectedACK(), elem);
	}

	public synchronized WindowSlot setACK(long expectedACK){
		WindowSlot ws = this.windowSlots.get(expectedACK);
		if(ws == null) return null;

		ws.setAcked();

		while(isFirstAcked()) {
			pollFirst();
			this.notifyAll();
		}

		return ws;
	}

	public synchronized WindowSlot pollFirst(){
		return this.windowSlots.pollFirstEntry().getValue();
	}

	public synchronized boolean isFull(){
		return this.size() >= this.capacity;
	}

	public synchronized boolean isEmpty() { return this.size() == 0; }

	public synchronized int size(){
		return this.windowSlots.size();
	}

	public synchronized boolean isFirstAcked(){
		Map.Entry<Long, WindowSlot> entry = this.windowSlots.firstEntry();
		if(entry == null) return false;

		WindowSlot ws = entry.getValue();
		if(ws == null) return false;

		return ws.isAcked();
	}

	public synchronized TftpPacket getPacket(long expectedACK){
		WindowSlot ws = this.windowSlots.get(expectedACK);
		return ws == null ? null : ws.getPacket();
	}

	public synchronized void waitUntilIsNotEmpty() throws InterruptedException {
		while(!this.isEmpty())
			this.wait();
	}

	public void setCapacity(int capacity) {
		if(this.capacity != capacity)
			StatsU.notifyWindowSizeChange(capacity);

		this.capacity = capacity;
	}

	public int getCapacity() {
		return this.capacity;
	}
	public WindowSlot getFirst(){
		Map.Entry<Long, WindowSlot> entry = this.windowSlots.firstEntry();
		if(entry == null) return null;

		WindowSlot ws = entry.getValue();
		if(ws == null) return null;

		return ws;
	}
}
