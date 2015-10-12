package t1;

/**
 * Created by gbfm on 10/12/15.
 */
public class WindowSlot {
	private TftpPacket packet;
	private int numberOfTries;
	private boolean acked;

	public WindowSlot(TftpPacket packet) {
		this.packet = packet;
		this.numberOfTries = 0;
		this.acked = false;
	}

	public void incrementTries() {
		this.numberOfTries++;
	}

	public void setAcked() {
		this.acked = true;
	}

	public boolean isAcked() {
		return this.acked;
	}

	public TftpPacket getPacket() {
		return this.packet;
	}

	public int getNumberOfTries() {
		return this.numberOfTries;
	}
}
