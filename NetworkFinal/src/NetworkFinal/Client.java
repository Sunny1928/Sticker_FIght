package NetworkFinal;

import java.io.*;
import java.net.*;

public class Client {
	
	/**
	 * @param Server_TCP_PORT is a Server's TCP port that is a fixed number.
	 * @param Server_UDP_Port is specified to UDP port that server bound.
	 * @param serverIP        is Server's IP address in string format.
	 * @param Disconnect_Port is a UDP port which is bound at a UDP socket that
	 *                        listens to disconnect request.
	 * @param BUFFER_SIZE     is specified to MAX size of packet.
	 * @param ds              is a client side UDP socket.
	 */
	private static final int Server_TCP_PORT = 10000;
	private int Server_UDP_Port;
	private int My_UDP_Port;
	private String serverIP;
	private int Disconnect_Port = 10001;
	private static final int BUFSIZE = 256;
	private DatagramSocket ds = null;

	public static void main(String[] args) {
		Client c = new Client();
	}

	public Client() {

		Socket s = null;
		try {
			
			/**
			 * Create TCP socket by Socket(), and create UDP socket by self-defined class
			 * UDPThread
			 */
			s = new Socket("localhost", Server_TCP_PORT);
			UDPThread UDP_thread = new UDPThread();

			/**
			 * @param instream  is used to receive Server side UDP socket info by TCP
			 *                  socket.
			 * @param outstream is used to send Client side UDP socket info by TCP socket.
			 */
			DataInputStream instream = new DataInputStream(s.getInputStream());
			DataOutputStream outstream = new DataOutputStream(s.getOutputStream());

			outstream.writeUTF(InetAddress.getLocalHost().getHostAddress());
			outstream.writeInt(My_UDP_Port);

			serverIP = instream.readUTF();
			Server_UDP_Port = instream.readInt();

			new Thread(UDP_thread).start();

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		/**
		 * After we've send Client side UDP socket info to Server, and also get info of
		 * Server side UDP socket, we can close TCP socket in Client side. Because it is
		 * of no usage.
		 */
		finally {
			try {
				if (s != null) {
					s.close();
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	private class UDPThread implements Runnable {
		
		/**
		 * Create UDP socket
		 */
		UDPThread() {
			try {
				ds = new DatagramSocket();
				My_UDP_Port = ds.getLocalPort();
			} 
			catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

		@Override
		public void run() {
			byte[] buf = new byte[BUFSIZE];
			
			/**
			 * Receive packet from server and parse message
			 */
			while (ds != null) {
				try {
					DatagramPacket d_packet = new DatagramPacket(buf, buf.length);
					ds.receive(d_packet);

					// still need to parse message here
				} 
				
				/*
				 * When socket is closed after sending disconnect request,
				 * ds.receive() will throw SocketException, and how I deal with it here is
				 * by break to leave out this infinite loop to make thread end properly.
				 */
				catch(SocketException soe) {
					break;
				}
				catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * sendDisconnectRequest() will send client UDP port to server,
	 * and then server will end the connection.
	 */
	public void sendDisconnectRequest() {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFSIZE);
		DataOutputStream dos = new DataOutputStream(baos);
		
		try {
			dos.writeInt(My_UDP_Port);
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
		}
		finally {
			try {
				if(dos != null) {
					dos.close();
				}
			}
			catch(IOException ioe) {
				ioe.printStackTrace();
			}
		}
		
		byte[] buf = new byte[BUFSIZE];
		buf = baos.toByteArray();
		
		try {
			DatagramPacket d_packet = new DatagramPacket(buf,buf.length,
					new InetSocketAddress(serverIP,Disconnect_Port));
			ds.send(d_packet);
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
		}
		finally {
			try {
				if(baos != null) {
					baos.close();
				}
				if(ds != null) {
					ds.close();
				}
			}
			catch(IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
}
