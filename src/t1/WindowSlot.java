package t1;

/**
 * Created by gbfm on 10/12/15.
 */
public class WindowSlot {
	private TftpPacket packet;
	private int numberOfTries;
	private boolean isAcked;

	private long expectedACK;

	public WindowSlot(TftpPacket packet) {
		this(packet, packet.getBlockSeqN());
	}

	public WindowSlot(TftpPacket packet, long expectedACK){
		this.packet = packet;
		this.numberOfTries = 0;
		this.isAcked = false;

		this.expectedACK = expectedACK;
	}

	public void incrementTries() {
		this.numberOfTries++;
	}

	public void setAcked() {
		this.isAcked = true;
	}

	public boolean isAcked() {
		return this.isAcked;
	}

	public TftpPacket getPacket() {
		return this.packet;
	}

	public int getNumberOfTries() {
		return this.numberOfTries;
	}

	public long getExpectedACK(){
		return this.expectedACK;
	}
}
