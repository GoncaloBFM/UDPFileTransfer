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

	public WindowSlot addToWindow(WindowSlot elem){
		this.windowSlots.put(elem.getPacket().getBlockSeqN(), elem);

		if(this.isFull())
			return this.windowSlots.pollFirstEntry().getValue();

		return null;
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

}
