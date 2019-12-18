package netproA2;

import java.io.*;
import java.net.*;
import java.util.HashMap;

public class DNS_Server {
	
	public static final int BUF_LEN = 256;
	private static final int DNS_PORT = 9090;
	private static Boolean dnsServStat = true;
	private static byte[] buf = new byte[BUF_LEN];
	
	private static HashMap<String, String> ipv4Addresses = new HashMap<String, String>();
	private static HashMap<String, String> ipv6Addresses = new HashMap<String, String>();

	public static void main(String[] args) throws IOException {
		// open socket on port 9090
		DatagramSocket dnsSocket = new DatagramSocket(DNS_PORT);
		
		System.out.println("Starting DNS server on port: " + DNS_PORT);
		
		// initialize mappings for IPv4 and IPv6 addresses
		ipv4Addresses.put("www.sdxcentral.com", "104.20.242.119");
		ipv4Addresses.put("www.lightreading.com", "104.25.195.108");
		ipv4Addresses.put("www.linuxfoundation.org", "23.185.0.2");
		ipv4Addresses.put("www.cncf.io", "23.185.0.3");
		
		ipv6Addresses.put("www.sdxcentral.com", "2606:4700:10::6814:f277");
		ipv6Addresses.put("www.lightreading.com", "2606:4700:20::6819:c46c");
		ipv6Addresses.put("www.linuxfoundation.org", "2620:12a:8000::2");
		ipv6Addresses.put("www.cncf.io", "2620:12a:8000::3");
		
		while (dnsServStat) {
			// receive client requests and create a new thread
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			dnsSocket.receive(packet);
			DNS_WorkerThread workerThread = new DNS_WorkerThread(dnsSocket, packet);
			workerThread.start();
		}
		
		dnsSocket.close();
	}
	
	// gets the IPv4 and IPv6 addresses associated with the domain name
	public static String getDomainAddresses (String domainName) {
		if (!ipv4Addresses.containsKey(domainName) || !ipv6Addresses.containsKey(domainName)) {
			System.out.println("Could not find entry for: " + domainName);
			return "Domain name not found.";
		} else {
			System.out.println("Returning IP addresses for: " + domainName);
			return "IPv4: " + ipv4Addresses.get(domainName) + ", IPv6: " + ipv6Addresses.get(domainName);
		}
	}
}
