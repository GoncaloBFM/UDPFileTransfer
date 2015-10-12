package t1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

import static t1.TftpPacket.MAX_TFTP_PACKET_SIZE;

/**
 * Created by Andre Pontes on 12/10/2015.
 */
public class SelectiveRepeteProtocol {
    private DatagramSocket udpSocket;

    private SocketAddress destAddr;
    private int port;

	private Alarm alarm;
    private Window window;

    private long millisTimeout;

    public SelectiveRepeteProtocol(int initialWindowSize, DatagramSocket udpSocket, SocketAddress address, long millisTimeout) {
		this.alarm = new Alarm();
        this.window = new Window(initialWindowSize);

        this.port = udpSocket.getPort();
        this.destAddr = address;

		ACKReceiverThread ackReceiverThread = new ACKReceiverThread();
		ackReceiverThread.start();

        this.millisTimeout = millisTimeout;
    }

    public void send(TftpPacket packet) throws SocketSendException {
        this.sendPacket(packet);

        long seqN = packet.getBlockSeqN();
        alarm.schedule(seqN, millisTimeout, () -> sendPacket(window.getPacket(seqN)));
    }

    private void sendPacket(TftpPacket packet) throws SocketSendException {
		try {
			udpSocket.send(new DatagramPacket(packet.getPacketData(), packet.getLength(), this.destAddr));
		} catch (IOException e) {
			throw new SocketSendException("Could not send package", e);
		}
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

        private synchronized void setACK(TftpPacket packet){
            long seqN = packet.getBlockSeqN();
            alarm.unschedule(seqN);
            window.setACK(seqN);
        }
    }
}
