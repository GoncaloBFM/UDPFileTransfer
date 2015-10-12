package t1;

import java.io.IOException;
import java.net.DatagramPacket;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static t1.TftpPacket.MAX_TFTP_PACKET_SIZE;

/**
 * Created by Andre Pontes on 12/10/2015.
 */
public class SelectiveRepeteProtocol {
    private DatagramSocket udpSocket;

    private SocketAddress destAddr;
    private int port;

    private Window window;
    private ACKReceiverThread ackReceiverThread = new ACKReceiverThread();
    private Alarm alarm = new Alarm();

    private long millisTimeout;

    public SelectiveRepeteProtocol(int initialWindowSize, DatagramSocket udpSocket, long millisTimeout) {
        this.window = new Window(initialWindowSize);

        this.port = udpSocket.getPort();
        this.destAddr = udpSocket.getRemoteSocketAddress();

        this.ackReceiverThread.start();

        this.millisTimeout = millisTimeout;
    }

    public void send(TftpPacket packet) throws IOException {
        sendPacket(packet);

        long seqN = packet.getBlockSeqN();
        alarm.schedule(seqN, millisTimeout, () -> {
            sendPacket(window.getPacket(seqN));
        });
    }

    private void sendPacket(TftpPacket packet) throws IOException {
        udpSocket.send(new DatagramPacket(packet.getPacketData(), packet.getLength(), destAddr));
    }

    private class ACKReceiverThread extends Thread {
        @Override
        public void run(){
            try {
                for (;;) {
                    byte[] buffer = new byte[MAX_TFTP_PACKET_SIZE];
                    DatagramPacket msg = new DatagramPacket(buffer, buffer.length);
                    udpSocket.receive(msg);

                    // update server address (it may change due to WRQ coming from a different port
                    destAddr = msg.getSocketAddress();

                    // make the packet available to sender process
                    TftpPacket pkt = new TftpPacket(msg.getData(), msg.getLength());

                    if(pkt.getOpcode() != TftpPacket.OP_ACK)
                        throw new Exception("Not an ACK packet");

                    this.setACK(pkt);
                }
            } catch (Exception e) {
                System.err.println("Error in receiver thread! " + e.getMessage() );
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
