package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Myfile {
	private String path = "D:\\ftp";
	File now = null;
	Myfile(){
		now = new File(path);
		if(!now.exists()) {
			now.mkdir();
		}
		
	}
	boolean cd(String i) {
		try {
			if(i.equals("..") && now.getCanonicalPath().equals(path)) {
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		String newpath = null;
		if(!i.equals(".."))
			newpath = now + "/" + i;
		else
			newpath = now.getParent();
		File tmp = new File(newpath);
		if(!tmp.exists()) {
			return false;
		}else {
			now = tmp;
		}
		return true;
	}
	String pwd() {
		String i = null;
		try {
			i = now.getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String a = "qwe";
		if(i.length()>6) { 
			a = i.substring(6,i.length());
			a = "\"" + a + "\"";
		}
		else 
			a = "\"/\"";
		
		return a;
	}
	String ls() {
		String str = "";
		for(String f :now.list()) {
			str += f + "\n";
		}
		return str;
	}
	String dir() {
		String str = "";
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd  HH:mm");
		int dirnum = 0;
		int filenum = 0;
		long filesize = 0;
		for(File f :now.listFiles()) {
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(f.lastModified());
			str += df.format(cal.getTime()) + "  ";
			if(f.isDirectory()) {
				str += "<DIR>          ";
				dirnum++;
			}
			else {
				filenum++;
				filesize += f.length();
				str += "     " + String.format("%-10d", f.length());;
			}
			str += f.getName() + "\n";
		}
		str += "        " + filenum + "个文件      " + filesize + "字节\n";
		str += "        " + dirnum  + "个目录" + "\n";
		return str;
	}
	BufferedReader getBufferedReader(String filename) {
		File file = new File(now.getAbsolutePath() + "/" + filename);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			
		} catch (FileNotFoundException e) {
			System.out.println("open file error:  " + file.getAbsolutePath());
			return null;
		}
		return br;
	}
	FileInputStream getFileInputStream(String filename) {
		File file = new File(now.getAbsolutePath() + "/" + filename);
		FileInputStream r = null;
		try {
			r = new FileInputStream(file);
			
		} catch (FileNotFoundException e) {
			System.out.println("open file error:  " + file.getAbsolutePath());
			return null;
		}
		return r;
	}
	PrintWriter getPrintWriter(String filename) throws IOException {
		File f = new File(now.getAbsolutePath() + "/" + filename);
		if(!f.exists()) {
			f.createNewFile();
		}
		PrintWriter w = null;
		w = new PrintWriter(new FileOutputStream(f),true);
		return w;
	}
	FileOutputStream getFileOutputStream(String filename) throws IOException {
		File f = new File(now.getAbsolutePath() + "/" + filename);
		if(!f.exists()) {
			f.createNewFile();
		}
		FileOutputStream w = null;
		w = new FileOutputStream(f);
		return w;
	}
	boolean del(String filename) {
		if(filename.equals("*")) {
			File[] fs = now.listFiles();
			for(File s:fs) {
				if(s.isFile())
					s.delete();
			}
			return true;
		}
		File f = new File(now.getAbsolutePath() + "/" + filename);
		if(f.exists() && f.isFile()) {
			return f.delete();
		}
		return false;
	}
	boolean rmdir(String dirname) {
		File f = new File(now.getAbsolutePath() + "/" + dirname);
		if(f.isDirectory()) {
			return f.delete();
		}
		return false;
	}
	boolean mkdir(String dirname) {
		File f = new File(now.getAbsolutePath() + "/" + dirname);
		if(f.exists()) {
			return false;
		}
		return f.mkdirs();		
	}
	boolean isexist(String filename) {
		File f = new File(now.getAbsolutePath() + "/" + filename);
		if(f.exists()) {
			return true;
		}
		return false;
	}
	boolean rename(String prename,String aftername) {
		File file = new File(now.getAbsolutePath() + "/" + prename);
		file.renameTo(new File(now.getAbsolutePath() + "/" + aftername));
		return true;
	}

}
