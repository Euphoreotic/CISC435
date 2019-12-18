package netproA2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class Client {
	
	private static final int BUF_LEN = 256;
	private static String serverIpAddress = "localhost";
	private static int dhcpPort = 7070;
	private static boolean clientRunning = true;
	
	private static final String CLIENT_MAC = "8C-16-45-03-2F-03";
	private static final String GW_MAC = "B4:D5:BD:C2:A0:88";
	private static final String SOURCE_TCP_PORT = "49696";
	
	private static String userMsg;
	
	private static Timer clientTimer = new Timer();
	private static byte[] buf;
	private static int dnsPort;
	private static String ipAddress;
	private static int leaseTimeSec;
	
	public static void main(String[] args) throws IOException{
		// create socket
		DatagramSocket clientSocket = new DatagramSocket();
		
		// send IP config request to DHCP server
		String message = "requestIpConfig";
		System.out.println("Sending request to DHCP server for IP configurations.");
		
		String ipConfig = sendPacket(clientSocket, serverIpAddress, dhcpPort, message);
		System.out.println("IP configurations are: {" + ipConfig + "}");
		
		getAddress(ipConfig);
		System.out.println("IP address is: " + ipAddress);
		
		// set up a timer release the IP address and stop program execution
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				System.out.println("Lease time expired. Closing socket and stopping program execution.");
				clientRunning = false;
				ipAddress = "";
				clientSocket.close();
				System.exit(0);
			}
		};
		clientTimer.schedule(task, leaseTimeSec * 1000);
		
		BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
		
		while (clientRunning) { 
			System.out.println("Enter \"dhcp\" to send requests to the DHCP server. \n"
					+ "Enter \"dns\" to send requests to the DNS server.");
			
			String userServChoice = userInput.readLine();
			
			// assuming user only enters valid inputs
			if (userServChoice.toLowerCase().equals("dhcp")) {
				System.out.println("Enter \"renew\" to renew lease.\n"
						+ "Enter \"release\" to release the assigned IP address.");
				String userRequest = userInput.readLine();
				
				switch (userRequest.toLowerCase()) {
				
				case "release":
					userMsg = userRequest + ": " + ipAddress;
					clientRunning = false;
					clientTimer.cancel();
					break;
					
				case "renew":
					userMsg = userRequest + ": " + ipAddress;
					
					// cancel the current timer
					clientTimer.cancel();
					clientTimer.purge();
					
					// create and start a new instance of the task and timer
					task = new TimerTask() {
						@Override
						public void run() {
							System.out.println("Lease time expired. Closing socket and stopping program execution.");
							clientRunning = false;
							ipAddress = "";
							clientSocket.close();
							System.exit(0);
						}
					};
					clientTimer = new Timer();
					clientTimer.schedule(task, leaseTimeSec * 1000);
					break;
				
				// client should not be able to request IP config from DHCP server through the console
				case "requestIpConfig":
					userMsg = "aa";
					break;
					
				default:
					userMsg = userRequest;
					break;
				}
				
				String dhcpServResponse = sendPacket(clientSocket, serverIpAddress, dhcpPort, userMsg);
				System.out.println(dhcpServResponse);
				
			} else if (userServChoice.toLowerCase().equals("dns")) {
				System.out.println("Enter the domain name:");
				String domainName = userInput.readLine();
				String dnsServRepsonse = sendPacket(clientSocket, serverIpAddress, dnsPort, domainName);
				
				if (dnsServRepsonse.equals("Domain name not found.")) {
					System.out.println(dnsServRepsonse);
				} else { // domain name was found
					String domainIpv4Address = getIpv4Address(dnsServRepsonse);
//					 GW MAC | Your MAC | cncf.io IPv4 address | Your IP address | Dest. TCP port: 80 | Src. TCP
//					 port: your client’s port | Application data: HTTP request to load cncf.io homepage (simply include
//					 the domain name www.cncf.io)|
					System.out.println("| GW MAC: " + GW_MAC + " | Client Mac: " + CLIENT_MAC + " | " + domainName + " IP address: " 
							+ domainIpv4Address + " | Client IP: " + ipAddress + " | " + "Dest. TCP port: 80 | Src. TCP port: " 
							+ SOURCE_TCP_PORT + " | " + "Application data: HTTP request to load " + domainName);
					
				}
			} else {
				System.out.println("Unexpected input.");
			}
		}

		// close UDP connection
		System.out.println("Closing client socket.");
		clientSocket.close();
	}
	
	// send packets to server at ipAddress:port
	public static String sendPacket(DatagramSocket clientSocket, String ipAddress, int port, String message) throws IOException{
		// convert message to byte array and send the packet to the a server using the given address and port num
		buf = new byte[BUF_LEN];
		buf = message.getBytes();
		InetAddress address = InetAddress.getByName(ipAddress);
		DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
		clientSocket.send(packet);
		
		// receive packet from the server and convert it back to a string
		buf = new byte[BUF_LEN];
		packet = new DatagramPacket(buf, buf.length);
		clientSocket.receive(packet);
		String receivedPacket = new String(packet.getData(), 0, packet.getLength());
		
		return receivedPacket;
	}
	
	// parses IP config into usable data
	public static void getAddress(String ipConfig) {
		// create mapping for IP config fields
		HashMap<String, String> ipConfigMap = new HashMap<String, String>();
		
		// splits IP config into each field
		String[] fields = ipConfig.split(", ");
		for (String field : fields) {
			// split each field into key value pairs
			String[] configPair = field.split(": ");
			ipConfigMap.put(configPair[0], configPair[1]);
		}
		
		// set save IP address and DNS port# provided by DHCP server
		ipAddress = ipConfigMap.get("IP");
		dnsPort = Integer.parseInt(ipConfigMap.get("DNS Server Port#"));
		leaseTimeSec = Integer.parseInt(ipConfigMap.get("Lease"));
	}
	
	// retrieves IPv4 address from DNS server response
	public static String getIpv4Address(String domainAddresses) {
		HashMap<String, String> domainAddressMap = new HashMap<String, String>();
		
		// splits domain addresses into IPv4 and IPv6 addresses
		String[] addresses = domainAddresses.split(", ");
		for (String address : addresses) {
			// split each field into key value pairs
			String[] configPair = address.split(": ");
			domainAddressMap.put(configPair[0], configPair[1]);
		}
		return domainAddressMap.get("IPv4");
	}
}
