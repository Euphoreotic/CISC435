package netproA2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class DHCP_Server {
	public static final int BUF_LEN = 256;
	private static Boolean dhcpServStat = true;
	private static byte[] buf = new byte[BUF_LEN];
	private static final int DHCP_PORT = 7070;
	private static final int LEASE_TIME_SEC = 60;
	
	private static ConcurrentHashMap<String, Integer> takenAddresses = new ConcurrentHashMap<String, Integer>();
	private static ArrayList<String> addressPool = new ArrayList<String>();
	private static Timer serverTimer = new Timer();

	public static void main(String[] args) throws IOException{
		
		// open DHCP server socket on port 7070
		DatagramSocket dhcpSocket = new DatagramSocket(DHCP_PORT);
		
		System.out.println("Starting DHCP server on port: " + DHCP_PORT);
		
		// populate address pool
		addressPool.add("192.168.1.26/24");
		addressPool.add("192.168.1.15/24");
		addressPool.add("192.168.1.57/24");
		addressPool.add("192.168.1.96/24");
		addressPool.add("192.168.1.39/24");
		Collections.sort(addressPool);
		
		// schedule a task to run every second to decrease the lease timer of all taken address by one second
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				Set<String> keys = takenAddresses.keySet();
				for (String key : keys) {
					if (takenAddresses.containsKey(key)) {
						int timeLeft = takenAddresses.get(key);
						if (timeLeft == 1) {
							System.out.println("Lease time expired, releasing address: " + key);
							returnAddress(key);
						} else {
							takenAddresses.put(key, timeLeft - 1);
						}
					}
				}
			}
		};
		serverTimer.scheduleAtFixedRate(task, 0, 1000);
		
		// while the server is still running
		while (dhcpServStat) {
			// receive client requests and create a new thread
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			dhcpSocket.receive(packet);
			DHCP_WorkerThread workerThread = new DHCP_WorkerThread(dhcpSocket, packet);
			workerThread.start();
		}
		
		dhcpSocket.close();
	}
	
	// return first address in address pool as they should be assigned in order
	public static String getAddress() {
		// get the first address in the address pool and remove it from the address pool
		String address = addressPool.get(0);
		addressPool.remove(address);

		// add the address to the taken address pool
		takenAddresses.put(address, 60);
		return address;
	}
	
	// returns an expired/released address back to the address pool
	public static void returnAddress(String returnedAddress) {
		
		// removes the address from the list of taken addresses
		takenAddresses.remove(returnedAddress);
		
		// returns it to the address pool and reorders the pool
		addressPool.add(returnedAddress);
		Collections.sort(addressPool);
	}
	
	// renews the timer for the specified IP address
	public static void renewTimer(String ipAddress) {
		// update only if address has not already been released
		if (takenAddresses.containsKey(ipAddress)) {
			takenAddresses.put(ipAddress, LEASE_TIME_SEC);
		} else {
			System.out.println("IP address has already been released.");
		}
		return;
	}
}