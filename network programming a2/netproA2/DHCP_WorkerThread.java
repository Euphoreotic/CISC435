package netproA2;

import java.io.*;
import java.net.*;

public class DHCP_WorkerThread extends Thread {
	private byte[] buf = new byte[DHCP_Server.BUF_LEN];
	private final String GATEWAY_IP = "192.168.2.1";
	private final int DNS_SERVER_PORT = 9090;
	private final int LEASE_SEC = 60;
	
	private DatagramSocket dhcpSocket;
	private DatagramPacket packet;
	private String message;
	private InetAddress address;
	private int port;
	
	// create new DHCP worker thread using the DHCP socket and request packet
	public DHCP_WorkerThread(DatagramSocket dhcpSocket, DatagramPacket packet) {
		this.dhcpSocket = dhcpSocket;
		this.packet = packet;  
		address = packet.getAddress();
		port = packet.getPort();
	}
	
	@Override
	public void run(){
		// convert byte array to string 
		message = new String(packet.getData(), 0, packet.getLength());
		String[] configPair = message.split(": ");
		
		String newMessage;
		switch(configPair[0]) {
		case "requestIpConfig":
			System.out.println("IP configuration request received.");
			System.out.println("Sending IP configurations.");
			newMessage = "IP: " + DHCP_Server.getAddress() + ", GW: " + GATEWAY_IP + ", DNS Server Port#: "
						+ DNS_SERVER_PORT + ", Lease: " + LEASE_SEC;
			break;
			
		case "release":
			DHCP_Server.returnAddress(configPair[1]);
			newMessage = "Address released.";
			System.out.println("Releasing IP address: " + configPair[1]);
			break;
			
		case "renew":
			DHCP_Server.renewTimer(configPair[1]);
			newMessage = "Lease timer renewed.";
			System.out.println("Renewed lease timer for: " + configPair[1]);
			break;
		
		default:
			newMessage = "Invalid request";
			break;
		}
		
		// convert new message to byte array and send the return packet to the client 
		buf = newMessage.getBytes();
		DatagramPacket returnPacket = new DatagramPacket(buf, buf.length, address, port);
		try {
			dhcpSocket.send(returnPacket);
		} catch (IOException e) {
			System.out.println("io exception");
		}
	}

}
