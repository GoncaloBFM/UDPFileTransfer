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
    private static final int BITRATE_TEST_STEP = 10;
    private static int BITRATE_TEST_CURRENT = BITRATE_TEST_STEP;

    private DatagramSocket udpSocket;
    private SocketAddress destAddr;

	private Alarm alarm;
    private Window window;

    private long millisTimeout;
    private int maxTries;

    private boolean addaptable;

    public SelectiveRepeatProtocol(int initialWindowSize, DatagramSocket udpSocket, SocketAddress address, long millisTimeout, int maxTries) {
        init(udpSocket, address, maxTries);

        this.window = new Window(initialWindowSize);
        this.millisTimeout = millisTimeout;

        this.addaptable = false;
    }

    public SelectiveRepeatProtocol(DatagramSocket udpSocket, SocketAddress address, int maxTries){
        init(udpSocket, address, maxTries);

        this.window = new Window();
        this.millisTimeout = StatsU.getOptimalTimeout();

        this.addaptable = true;
    }

    private void init(DatagramSocket udpSocket, SocketAddress address, int maxTries){
        this.alarm = new Alarm();
        this.udpSocket = udpSocket;
        this.destAddr = address;

        ACKReceiverThread ackReceiverThread = new ACKReceiverThread();
        ackReceiverThread.start();

        this.maxTries = maxTries;
    }

    public void send(TftpPacket packet) throws SocketSendException {
       send(packet, packet.getBlockSeqN());
    }

    public void send(TftpPacket packet, long expectedACK){
        WindowSlot ws = new WindowSlot(packet, expectedACK);

        window.addToWindow(ws);
        this.sendRetry(ws);
    }

    private void sendRetry(WindowSlot ws){
        long expectedACK = ws.getExpectedACK();
        if( ws.getNumberOfTries() >= maxTries) {
            if(ws.getPacket().getBlockData().length != 0) {
                System.out.println("ERRoR ACK not received: " + ws.getExpectedACK());
                System.exit(002);
            } else {
                window.setACK(ws.getExpectedACK());
            }
            this.alarm.cancelAll();
            return;
        } else if(addaptable && (ws.getExpectedACK() < window.getFirst().getExpectedACK() + window.getCapacity() * 512)){
            window.setCapacity((int) Math.max(window.getCapacity() * 0.50, 1));
        }

        TftpPacket pkt = ws.getPacket();
        //TftpPacket pkt = window.getPacket(expectedACK);

        if(pkt == null || ws.isAcked()) return;

        if(ws.getExpectedACK() < window.getFirst().getExpectedACK() + window.getCapacity() * 512) {
            if(ws.getNumberOfTries() > 0)
                StatsU.notifyPacketReSent();

            sendPacket(pkt);
            ws.incrementTries();
        }

        if(addaptable)
            millisTimeout = StatsU.getOptimalTimeout();

        alarm.trySchedule(expectedACK, millisTimeout, () -> sendRetry(ws));
    }

    private void sendPacket(TftpPacket packet) throws SocketSendException {
		try {
            if(addaptable && BITRATE_TEST_CURRENT == BITRATE_TEST_STEP) {
                long transmissionTime = System.currentTimeMillis();
                udpSocket.send(new DatagramPacket(packet.getPacketData(), packet.getLength(), this.destAddr));
                transmissionTime = System.currentTimeMillis() - transmissionTime;
                StatsU.addTransmissionTimeSample((int) transmissionTime);
                BITRATE_TEST_CURRENT = 0;
            }
            else{
                udpSocket.send(new DatagramPacket(packet.getPacketData(), packet.getLength(), this.destAddr));
                BITRATE_TEST_CURRENT++;
            }

            StatsU.notifyPacketSent();

            System.err.println(">>> Sent: " + packet.getBlockSeqN() + " | " + packet.getBlockSeqN() / 512);
		} catch (IOException e) {
			throw new SocketSendException("Could not send package", e);
		}
	}

    public boolean emptyWindow(){
        return this.window.isEmpty();
    }

    public void waitUntilWindowIsNotEmpty(){
        try {
            this.window.waitUntilIsNotEmpty();
        } catch (InterruptedException e) {
            e.printStackTrace();
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

                    StatsU.notifyACKReceived();

                    this.setACK(pkt);
                }
            } catch (WrongOPCodeException | IOException e) {
                System.err.println("Error in receiver thread! \n" + e.getMessage());
            }
        }

        private void setACK(TftpPacket packet){
            long seqN = packet.getBlockSeqN();
            WindowSlot ws = window.setACK(seqN);

            if (ws != null && ws.getNumberOfTries() == 1) {

                if(addaptable) {
                    window.setCapacity(window.getCapacity() + 1);
                    //System.out.println("inc: " + window.getCapacity());
                }

                StatsU.addRTTSample(ws.getRTT());
                //System.out.println("Sample: " + ws.getRTT());
                //System.out.println("Current: " + StatsU.getOptimalTimeout());
            }

            alarm.unschedule(seqN);

            System.err.println("<<< ACK : " + seqN);
        }
    }
}
