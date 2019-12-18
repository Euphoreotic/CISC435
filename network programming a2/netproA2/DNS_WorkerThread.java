package netproA2;

import java.io.*;
import java.net.*;

public class DNS_WorkerThread extends Thread{
	private byte[] buf = new byte[DHCP_Server.BUF_LEN];
	
	private DatagramSocket dnsSocket;
	private DatagramPacket packet;
	private String domainNameQuery;
	private InetAddress address;
	private int port;
	
	// create new DNS worker thread using the DNS socket and received packet
	public DNS_WorkerThread(DatagramSocket dnsSocket, DatagramPacket packet) {
		this.dnsSocket = dnsSocket;
		this.packet = packet;
		this.address = packet.getAddress();
		this.port = packet.getPort();
	}
	
	@Override
	public void run() {
		domainNameQuery = new String(packet.getData(), 0, packet.getLength());
		
		String queryReply = DNS_Server.getDomainAddresses(domainNameQuery);
		buf = queryReply.getBytes();
		DatagramPacket returnPacket = new DatagramPacket(buf, buf.length, address, port);
		try {
			dnsSocket.send(returnPacket);
		} catch (IOException e) {
			System.out.println("io exception");
		}
	}

}
