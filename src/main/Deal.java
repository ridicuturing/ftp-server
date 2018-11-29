package main;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class Deal implements Runnable{
	PrintWriter pw = null;
	Socket s = null;
	BufferedReader br = null;
	Myfile f = null;
	OutputStream ou = null;
	int usertype = 0;// 0:unlogin 1:anonymous 2:normal user
	int type = 1; // 1:asc 2:bin
	Deal(Socket s) throws IOException{
		this.s = s;
		ou = s.getOutputStream();
		pw = new PrintWriter(ou,true);
		br = new BufferedReader(new InputStreamReader(s.getInputStream()));
		f = new Myfile();
	}

	@Override
	public void run() {
		String tmp = null;
		String[] tmps = null;
		pw.println("220");
		System.out.println("connect");
		//samsara
		boolean alive = true;
		while(alive) {
			try {
				tmp = br.readLine();
				tmps = tmp.split(" ");
				System.out.println(tmp);
				if(usertype == 0 && tmp.split(" ")[0].equals("PORT") && tmp.split(" ")[0].equals("CWD")) {
					pw.println("530 Please login ");
					continue;
				}
				switch(tmps[0]) {
				case "USER":
					if(tmps.length == 1) {
						usertype = 2;
						//TODO for text pw.println("230 Login successful."); 
						pw.println("530 Login incorrect.");
					}else {
						login(tmps[1]);
					}
					break;
				case "opts":
				case "OPTS":
					pw.println("200 UTF8 ok");
					break;
					
				case "PORT":
					connect(tmps[1]);
					break;
					
				case "CWD":
					if(f.cd(tmps[1])) {
						pw.println("250 Directory successfully changed.");
					}else {
						pw.println("550 Failed to change directory.You should input the right name");
					}
					break;
				case "XPWD":
				case "PWD":
					String tmp1 = null;
					if((tmp1 = f.pwd()) != null) {
						pw.println("257 " + tmp1);
					}else {
						pw.println("451");
					}
					break;
					
				case "DELE": //delete file
					if(f.del(tmps[1]))
						pw.println("250 Delete operation successful.");
					else
						pw.println("550 Delete operation failed.");
					break;
					
				case "XRMD": //rmdir
					if(f.rmdir(tmps[1]))
						pw.println("250 Remove directory operation successful.");
					else
						pw.println("550 Remove directory operation failed.");
					break;
					
				case "XMKD":	//mkdir
					if(f.mkdir(tmps[1])) {
						f.cd(tmps[1]);
						pw.println(f.pwd() + " created");
						f.cd("..");
					}
					else
						pw.println("550 Remove directory operation failed.");
					break;
					
				case "QUIT":
					pw.println("221 bye");
					alive = false;
					break;
				case "TYPE":
					if(tmps[1].equals("I")) {
						type = 2;
						pw.println("200 Switching to Binary mode.");
					}else if(tmps[1].equals("A")){
						type = 1;
						pw.println("200 Switching to ASCII mode.");
					}else {
						System.out.println("not bin???");
					}
					break;
				case "RNFR": //rename
					rename(tmp);
					break;
				default:
					pw.println("200 ok");
					
					
				}
			} catch (IOException e) {
				break;
			}
		}
	}

	boolean login(String username) {
		String tmp = null;
		
		pw.println("331 Please specify the password.");
		String password = null;
		
		try {
			tmp = br.readLine();
			if(tmp.split(" ").length > 1)
				password = tmp.split(" ")[1];
			if(checkUser(username,password)) {
				pw.println("230 Login successful.");
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		pw.println("530 Login incorrect.");
		return false;
	}
	boolean checkUser(String username,String password) {
		switch(username) {
		case "anonymous":
			usertype = 1;
			return true;
		case "123":
			if(password.equals("123")) {
				usertype = 2;
				return true;
			}else {
				return false;
			}
			
		default:
			return false;
		}
	}
	private void connect(String str) {
		String tmp = null;
		String[] tmps = str.split(",");
		String ip = tmps[0] + "." + tmps[1] + "." + tmps[2] + "." + tmps[3];
		int port = Integer.valueOf(tmps[4])*256 + Integer.valueOf(tmps[5]);
		try {
			Socket s1 = new Socket(ip,port,InetAddress.getByName("127.0.0.1"),20);
			pw.println("200 PORT command successful");
			BufferedReader br1 = new BufferedReader(new InputStreamReader(s1.getInputStream()));
			OutputStream ou1 = s1.getOutputStream();
			PrintWriter pw1 = new PrintWriter(ou1,true);
			tmp = br.readLine();
			System.out.println("PORT :"+tmp);
			switch(tmp.split(" ")[0]) {
			case "NLST": // ls
				ls(pw1);
				break;
			case "LIST": // dir
				dir(pw1);
				break;
			case "RETR": // get
				if(usertype != 2) {
					pw.println("550 Permission denied.");
					br1.close();
					s1.close();
					return;
				}
				get(pw1,ou1,tmp.split(" ")[1]);
				break;
			case "STOR": //put
				put(br1,tmp.split(" ")[1]);
				break;
			}
			
			br1.close();
			s1.close();
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
	}
	void ls(PrintWriter tp) {
		pw.println("150 directory listing");
		tp.println(f.ls());
		pw.println("226 Directory send OK");
	}
	void dir(PrintWriter tp) {
		pw.println("150 directory listing");
		tp.println(f.dir());
		pw.println("226 Directory send OK");
	}
	void get(PrintWriter tp,OutputStream ou1,String filename){
		if(type == 1) { //asc
			BufferedReader tb = null;
			tb = f.getBufferedReader(filename);
			if(tb == null) {
				pw.println("550 file open error!You may input the wrong file name");
				return;
			}
			pw.println("150 download "+ filename +" .");
			String str;
			try {
				str = tb.readLine();
			while(str != null) {
				tp.println(str);
				str = tb.readLine();
			}
			} catch (IOException e) {
				e.printStackTrace();
			}
			pw.println("226 Transfer complete.");
			tp.close();
		}else if(type == 2) {//bin
			FileInputStream fin = f.getFileInputStream(filename);
			FileOutputStream fou = null;
			try {
				fou = f.getFileOutputStream("2.jpg");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if(fin == null) {
				pw.println("550 file open error!You may input the wrong file name");
				return;
			}
			pw.println("150 download "+ filename +" .");
			byte[] buf = new byte[1300];
			try {
				int i = 1;
				while(fin.read(buf) >= 0) {
					ou1.write(buf);
					fou.write(buf);
					System.out.println(i++);
				}
				ou1.flush();
				fou.flush();
				fin.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			pw.println("226 Transfer complete.");
			
		}
	}
	void put(BufferedReader b,String filename) {
		PrintWriter p = null;
		try {
			p = f.getPrintWriter(filename);
		} catch (IOException e) {
			pw.println("550 file open error!You may input the wrong file name");
			System.out.println("open file error");
			e.printStackTrace();
		}
		if(p == null) {
			pw.println("550 file open error!You may input the wrong file name");
			System.out.println("open file error");
		}
		pw.println("150 Ok to send data.");
		String str;
		try {
			str = b.readLine();
			while(str != null) {
				p.println(str);
				str = b.readLine();
			}
		} catch (IOException e) {
			System.out.println("here0");
			e.printStackTrace();
		}
		p.close();
		pw.println("226 Transfer complete.");
	}
	void rename(String s) {
		if(s.length() < 2) {
			pw.println("550 please input file name");
			return;
		}
		String prename = s.split(" ")[1];
		if(!f.isexist(prename)) {
			pw.println("550 RNFR command failed");
			return;
		}
		pw.println("350 Ready for RNTO.");
		try {
			String tmp = br.readLine();
			System.out.println(tmp);
			String aftername = tmp.split(" ")[1];
			f.rename(prename, aftername);
			pw.println("250 Rename successful.");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
}
