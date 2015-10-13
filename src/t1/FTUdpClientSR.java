package t1;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;

import static t1.TftpPacket.OP_DATA;
import static t1.TftpPacket.OP_WRQ;

public class FTUdpClientSR {
	static PrintStream err = System.err;
	static PrintStream out = System.out;

	static final int DEFAULT_TIMEOUT = 3000;
	static final int DEFAULT_MAX_RETRIES = 5;
	static final int DEFAULT_BLOCKSIZE = 512; // default block size as in TFTP
												// RFC

	static int WindowSize = 10;
	static int BlockSize = DEFAULT_BLOCKSIZE;
	static int Timeout = DEFAULT_TIMEOUT;

	private String filename;

	private DatagramSocket socket;
	volatile private SocketAddress srvAddress;

	private SelectiveRepeatProtocol srProtocol;

	FTUdpClientSR(String filename, SocketAddress srvAddress) {
		try {
			this.socket = new MyDatagramSocket();
		} catch (SocketException e) {
			throw new SocketCreateException("Could not create socket", e);
		}
		this.srProtocol = new SelectiveRepeatProtocol(WindowSize, socket, srvAddress, Timeout, DEFAULT_MAX_RETRIES);
		this.filename = filename;
		this.srvAddress = srvAddress;
	}

	void sendFile() {

		out.println("sending file: \"" + filename + "\" to server: " + srvAddress + " from local port:" + socket.getLocalPort());
		TftpPacket wrr = new TftpPacket().putShort(OP_WRQ).putString(filename).putByte(0).putString("octet").putByte(0)
				.putString("selective_repeat").putByte(0).putString("true").putByte(0);

		this.srProtocol.send(wrr, 0L);
		this.srProtocol.waitUntilWindowIsNotEmpty();

		FileInputStream f = null;
		try {
			f = new FileInputStream(filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		long byteCount = 1; // block byte count starts at 1

		// read and send blocks
		int n;
		int lastN = 0;
		byte[] buffer = new byte[BlockSize];
		try {
			while ((n = f.read(buffer)) > 0) {
				TftpPacket pkt = new TftpPacket().putShort(OP_DATA).putLong(byteCount).putBytes(buffer, n);
				this.srProtocol.send(pkt);
				byteCount += n;
				lastN = n;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		//****
		//adapt byteCount for buggy server implementation
		if(lastN != BlockSize){
			byteCount = byteCount - lastN + BlockSize;
		}
		//end of adapt
		//****

		// Send an empty block to signal the end of file.
		TftpPacket pkt = new TftpPacket().putShort(OP_DATA).putLong(byteCount).putBytes(new byte[0], 0);
		this.srProtocol.waitUntilWindowIsNotEmpty();
		this.srProtocol.send(pkt);

		try {
			f.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.srProtocol.waitUntilWindowIsNotEmpty();
	}

	public static void main(String[] args) throws Exception {
		MyDatagramSocket.init(1, 1);

		switch (args.length) {
		case 5:
			WindowSize = Integer.parseInt(args[4]);
		case 4:
			Timeout = Integer.parseInt(args[3]);
		case 3:
			BlockSize = Integer.valueOf(args[2]);
		case 2:
			break;
		default:
			out.printf("usage: java FTUdpClient filename servidor [blocksize [ timeout [ windowsize ]]]\n");
			System.exit(0);
		}

		String filename = args[0];

		// Preparar endereco e o porto do servidor
		String server = args[1];
		SocketAddress srvAddr = new InetSocketAddress(server, FTUdpServer.DEFAULT_PORT);

		try {
			new FTUdpClientSR(filename, srvAddr).sendFile();
		}
		catch (Exception e){
			System.out.println("FAILED: " + e.getMessage());
		}

	System.exit(0);
	}

}
