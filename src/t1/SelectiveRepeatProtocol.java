package t1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

import static t1.TftpPacket.MAX_TFTP_PACKET_SIZE;

/**
 * Created by Andre Pontes on 12/10/2015.
 */
public class SelectiveRepeatProtocol {
    private DatagramSocket udpSocket;

    private SocketAddress destAddr;

	private Alarm alarm;
    private Window window;

    private long millisTimeout;
    private int maxTries;

    public SelectiveRepeatProtocol(int initialWindowSize, DatagramSocket udpSocket, SocketAddress address, long millisTimeout, int maxTries) {
		this.alarm = new Alarm();
        this.window = new Window(initialWindowSize);

        this.udpSocket = udpSocket;
        this.destAddr = address;

		ACKReceiverThread ackReceiverThread = new ACKReceiverThread();
		ackReceiverThread.start();

        this.millisTimeout = millisTimeout;
        this.maxTries = maxTries;
    }

    public void send(TftpPacket packet) throws SocketSendException {
       send(packet, packet.getBlockSeqN());
    }

    public void send(TftpPacket packet, long expectedACK){
        WindowSlot ws = new WindowSlot(packet, expectedACK);
        window.addToWindow(ws);
        this.sendRetry(ws);

        //ws.incrementTries();
        //this.sendPacket(packet);

        //alarm.schedule(expectedACK, millisTimeout, () -> sendRetry(ws));
    }

    private void sendRetry(WindowSlot ws){
        long expectedACK = ws.getExpectedACK();
        if( ws.getNumberOfTries() >= maxTries )
            throw new RuntimeException("ACK not received: " + ws.getExpectedACK());

        TftpPacket pkt = window.getPacket(expectedACK);

        if(pkt == null) {
            System.out.println("potato: " + expectedACK);
            return;
        }

        if(ws.isAcked()) {
            System.out.println("banana: " + ws.getPacket());
            return;
        }

        sendPacket(pkt);
        ws.incrementTries();

        alarm.trySchedule(expectedACK, millisTimeout, () -> sendRetry(ws));
    }

    private void sendPacket(TftpPacket packet) throws SocketSendException {
		try {
			udpSocket.send(new DatagramPacket(packet.getPacketData(), packet.getLength(), this.destAddr));
            System.err.println(">>> Sent: " + packet.getBlockSeqN() + " | " + packet.getBlockSeqN() / 512);
		} catch (IOException e) {
			throw new SocketSendException("Could not send package", e);
		}
	}

    public boolean emptyWindow(){
        return this.window.isEmpty();
    }

    private class ACKReceiverThread extends Thread {
        @Override
        public void run(){
            try {
				while (true) {
                    byte[] buffer = new byte[MAX_TFTP_PACKET_SIZE];
                    DatagramPacket msg = new DatagramPacket(buffer, buffer.length);
                    udpSocket.receive(msg);

                    // update server address (it may change due to WRQ coming from a different port
                    destAddr = msg.getSocketAddress();

                    // make the packet available to sender process
                    TftpPacket pkt = new TftpPacket(msg.getData(), msg.getLength());

                    if(pkt.getOpcode() != TftpPacket.OP_ACK) {
						throw new WrongOPCodeException("Not an ACK packet");
					}

                    this.setACK(pkt);
                }
            } catch (WrongOPCodeException | IOException e) {
                System.err.println("Error in receiver thread! ");
                e.printStackTrace();
            }
        }

        private void setACK(TftpPacket packet){
            long seqN = packet.getBlockSeqN();
            window.setACK(seqN);
            alarm.unschedule(seqN);

            System.err.println("<<< ACK : " + seqN);
        }
    }
}
