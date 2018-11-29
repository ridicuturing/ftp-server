package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		ServerSocket ss = null;
		Socket s = null;
		int usernum = 0;
		try {
			ss = new ServerSocket(21);
		} catch (IOException e1) {
			System.out.println("21 port has been opened");
			e1.printStackTrace();
		}
		while(true) {
			try {
				s = ss.accept();
				System.out.println("new user: " + ++usernum);
				new Thread(new Deal(s)).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
