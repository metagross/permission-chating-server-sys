

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

class client{
	private String id, passwd;
	private ArrayList<connection> connectingList = new ArrayList<connection>();
	int gcPoint = 0;
	ScriptEngineManager SEM = new ScriptEngineManager();
	ScriptEngine jsEngine = SEM.getEngineByName("JavaScript");
	
	private ArrayList<cmd_DATA> cmdList = new ArrayList<cmd_DATA>();
	private int cmdListCount;
	
	//����ũž ����
	private boolean menuLogic = true;
	
	public client(String id, String passwd){
		this.id = id;
		this.passwd = passwd;
	}
	
	//�� ����
	private int newEnterTheRoom(String host, int port) throws IOException{
		Socket s = new Socket(host, port);
		if(s == null) {
			System.out.println("�ش� ��Ʈ�� �������� ���߽��ϴ�.");
			return 1;
		}
		
		connection c = new connection(s);
		if(c == null) {
			return 1;
		}
		c.start();
		
		System.out.println("���� ���� �� ���� ������ ���� �Ϸ�");
		return 0;
	}
	
	public void sysin_menu() throws IOException, InterruptedException {
		Scanner sc = new Scanner(System.in);

		String t;
		int p;
		while(menuLogic) {
			System.out.println("0. ����\n1. �޼��� ������\n2. cmd �߰�\n3.�� ����\n4.���� ������ �� ����Ʈ\n");
			ArrayList<String> arylistString;
			p = sc.nextInt();
			sc.nextLine();
			switch(p) {
			case 0 :
				
				break;
			case 1 :
				//sendMessage
				int tempNum;
				connection temp;
				
				
				System.out.println("������ ����ּ���(����� -1)");
				for(int i=0;i<connectingList.size();i++) {
					temp = connectingList.get(i);
					System.out.println(i+". "+temp.s.getInetAddress().toString()+"\n");
				}
				System.out.println(" = ");
				tempNum = sc.nextInt();
				sc.nextLine();
				
				if(tempNum == -1){
					System.out.println("cancle selectServer");
					break;
				}else if(tempNum > connectingList.size()){
					System.out.println("input error");
					break;
				}
				temp = connectingList.get(tempNum);
					
				
				
				System.out.println("���� ��ȣ�� ������(����� -1)");
				for(int i=0;i<temp.roomNameList.size();i++) {
					System.out.println(i+". "+temp.roomNameList.get(i)+"\n");
				}
				tempNum = sc.nextInt();
				sc.nextLine();
				if(tempNum == -1){
					System.out.println("cancle selectServer");
					break;
				}else if(tempNum > temp.roomNameList.size()){
					System.out.println("input error");
					break;
				}
				
				System.out.println("�޼����� �Է��ϼ���");
				temp.sendMessage(sc.nextLine(), temp.roomNameList.get(tempNum));
				sc.nextLine();
				break;
			case 2 :
				//cmd �߰�
				System.out.println("�޼��� �ȿ��� ���� Ŀ��带 �Է��ϼ���. �����Ͻ÷��� ` �� �Է��ϼ���\n");
				arylistString = new ArrayList<String>();
				while(!(t = sc.nextLine()).equals("`")) {
					arylistString.add(t);
				}
				
				break;
			case 3 :
				int po;
				System.out.print("���� �� ���� ip�� �Է����ּ��� : ");
				t = sc.nextLine();
				System.out.print("���� �� ���� port�� �Է����ּ���(���� 8988) : ");
				po = sc.nextInt();
				sc.nextLine();
				newEnterTheRoom(t,po);
				break;
			case 4 :
				for(int i=0;i<connectingList.size();i++) {
					connection c = connectingList.get(i);
					System.out.println(i+". " + c.s.getInetAddress().toString()+"\n");
					for(int j=0;j<c.roomNameList.size();j++) {
						System.out.println(c.roomNameList.get(j)+"\n");
					}
				}
				break;
			}
		}
		
	}
	
	
	
	private synchronized void cmd_run(String line) throws IOException, ScriptException {
		//�Ҳ� �Ҳ� �Ҳ� cmd_Data �����ʿ�
		cmd_DATA cmdTemp;
		for(int i=0;i<cmdList.size();i++) {
			cmdTemp = cmdList.get(i);
			for(int j=0;j<cmdTemp.cmdAry.length;j++) {
				if(line.contains(cmdTemp.cmdAry[j])) {
					cmdTemp.run(null);
					break;
				}
			}
		}
	}
	
	class cmd_DATA{
		private String cmdStringCode;//���� ���߿� ����
		private String cmdName;
		private String[] cmdAry;
		private File f;
		private int instanceNum;
		
		//������ �ϳ��� �����ͺ��̽��� �߰��ϴ� ���
		//filePath�� ������ �ڹٽ�ũ��Ʈ ���ϰ���̴�.
		private cmd_DATA(String[] cmdAry, String cmdName, String filePath) throws IOException {
			this.cmdAry = cmdAry;
			f = new File("./cmd");
			if(!f.exists()) {
				f.mkdir();
			}
			else if(!f.isDirectory()) {
				f.delete();
				f.mkdir();
			}
			
			f = new File("./cmdScript");
			if(!f.exists()) {
				f.mkdir();
			}
			else if(!f.isDirectory()) {
				f.delete();
				f.mkdir();
			}

			f = new File("./cmd/"+cmdName);
			if(f.exists()) {
				new Exception("Already, Cliant have same_name_script.");
				return;
			}
			
			f = new File("./cmdScript/"+cmdName);
			if(f.exists()) {
				new Exception("Already, Cliant have same_name_script.");
				return;
			}
			
			FileReader fr = new FileReader(new File(filePath));
			FileWriter fw = new FileWriter(f);
			BufferedReader br = new BufferedReader(fr);
			BufferedWriter bw = new BufferedWriter(fw);
			String temp;
			
			while((temp = br.readLine()) != null) {
				bw.write(temp);
			}
			br.close();
			bw.close();
			
			f = new File("./cmd/"+cmdName);
			
			fw = new FileWriter(f);
			bw = new BufferedWriter(fw);
			
			bw.write(instanceNum+"\n");
			for(int i=0;i<cmdAry.length;i++) {
				bw.write(cmdAry[i]+"\n");
			}
			bw.close();
			this.cmdName = cmdName;
			cmdList.add(this);
		}
		
		private cmd_DATA(String cmdAryName) throws IOException {
			ArrayList<String> tempArray = new ArrayList<String>();
			String temp;
			
			BufferedReader br = new BufferedReader(new FileReader(new File("./cmd/"+cmdAryName)));
			this.cmdName = cmdAryName;
			while((temp = br.readLine()) != null) {
				tempArray.add(temp);
			}
			this.cmdAry = tempArray.toArray(new String[] {});
			
			this.f = new File("./cmdScript/" + cmdAryName);
		}
		
		//�� �Լ��� �������̵� �ϼ���
		private synchronized String run(String[] args) throws IOException, ScriptException {//���� ���߿� ����
			BufferedReader br = new BufferedReader(new FileReader(f));
			StringBuilder sb = new StringBuilder();
			String temp;
			while((temp = br.readLine()) != null) {
				sb.append(temp+"\n");
			}
			br.close();
			
			return jsEngine.eval(sb.toString()).toString();
		}
		
		
	}
	
	
	
	static class console{
		private static Scanner sc = new Scanner(System.in);
		
		public int readInt() {
			String line = sc.nextLine();
			return Integer.parseInt(line);
		}
		
		public String readString() {
			String line = sc.nextLine();
			return line;
		}
		
		public String[] readStrAry(String[] ary) {
			return ary;
		}
		
		public void print(String line) {
			System.out.print(line);
		}
		
		public void println(String line) {
			System.out.println(line);
		}
	}
	
	
	class connection extends Thread{
		private Socket s;
		private boolean connectionLogic = true;
		private ArrayList<String> roomNameList = new ArrayList<String>();
		
		private connection(Socket s) throws IOException{
			this.s = s;
			OutputStreamWriter osw = new OutputStreamWriter(s.getOutputStream());
			InputStreamReader isr = new InputStreamReader(s.getInputStream());
			BufferedReader br = new BufferedReader(isr);
			PrintWriter pw = new PrintWriter(osw);
			
			//���̵� ���� ����
			osw.write(id.length());
			osw.flush();
			if(isr.read() != 0) {
				new Exception("serverError");
				return;
			}
			
			
			//���̵� ����
			osw.write(id);
			osw.flush();			
			//���̵� Ȯ�� �Ϸ�
			if(isr.read() != 0) {
				new Exception("serverError");
				return;
			}
			
			//�н����� ���� ����
			osw.write(passwd.length());
			osw.flush();
			if(isr.read() != 0) {
				new Exception("serverError");
				return;
			}
			
			
			//�н����� ����
			osw.write(passwd);
			osw.flush();
			
			//���� Ȯ��
			if(isr.read() != 0) {
				new Exception("serverError");
				return;
			}
			
			//���� ��û
			osw.write(0);
			osw.flush();
			
			
			//���Ϲ���
			while(isr.read() == 1) {
				System.out.println("1����");
				osw.write(2);
				osw.flush();
				System.out.println("2����");
				roomNameList.add(br.readLine());
				System.out.println("���̸� ����");
				osw.write(3);
				osw.flush();
				System.out.println("3����");
			}
			
			connectingList.add(this);
			System.out.println("4����Ϸ�");
			//����Ϸ�
		}
		
		private int menu(int p,String[] args) throws IOException, InterruptedException, ScriptException{
			OutputStreamWriter osw = new OutputStreamWriter(s.getOutputStream());
			osw.write(p);
			switch(p){
				case 1 :
					//�����忡�� �˾Ƽ� �ޱ� �ϰ�����, Ȥ�ø��� �߰���
					getMessage();
					break;
				case 2 : 
					sendMessage(args[0], args[1]);
					break;
				case 3 :
					
					break;
				case 0 :
					synchronized(connectingList) {
						s.close();
						connectionLogic = false;
						Thread.sleep(1000);
						connectingList.remove(this);
						interrupt();
						join();
						if(gcPoint > 3) {
							System.gc();
							gcPoint=0;
						}
						gcPoint++;
					}
					break;
			}
			return 0;
		}
		
		private int inputCode(String filePath, String cmdName, String ary[]) throws IOException{
			File f = new File(filePath);
			cmd_DATA a = new cmd_DATA(ary, cmdName, filePath);
			
			cmdList.add(a);
			
			return 0;
		}

		//cmd_run�̶� ����
		private int executeCode(String msg) throws IOException, ScriptException{
			for(int i=0;i<cmdList.size();i++) {
				cmd_DATA temp = cmdList.get(i);
				for(int j=0;j<temp.cmdAry.length;j++) {
					if(msg.contains(temp.cmdAry[j])) {
						temp.run(msg.split(" "));
					}
				}
				
			}
			
			return 0;
		}
		
		private void deleteCode(int num){
			File f = new File("./cmd");
			if(!f.exists()) {
				f.mkdir();
			}
			else if(!f.isDirectory()) {
				f.delete();
				f.mkdir();
			}
			
			f = new File("./cmdScript");
			if(!f.exists()) {
				f.mkdir();
			}
			else if(!f.isDirectory()) {
				f.delete();
				f.mkdir();
			}

			f = new File("./cmd/"+cmdList.get(num).cmdName);
			if(f.exists()) {
				f.delete();
			}
			
			f = new File("./cmdScript/"+cmdList.get(num).cmdName);
			if(f.exists()) {
				f.delete();
			}
			return;
		}
		
		private void deleteCode(String name){
			cmd_DATA temp = null;
			
			for(int i=0;i<cmdList.size();i++) {
				if(((temp = cmdList.get(i)).cmdName.equals(name))){
					break;
				}
			}
			
			File f = new File("./cmd");
			if(!f.exists()) {
				f.mkdir();
			}
			else if(!f.isDirectory()) {
				f.delete();
				f.mkdir();
			}
			
			f = new File("./cmdScript");
			if(!f.exists()) {
				f.mkdir();
			}
			else if(!f.isDirectory()) {
				f.delete();
				f.mkdir();
			}

			f = new File("./cmd/"+temp.cmdName);
			if(f.exists()) {
				f.delete();
			}
			
			f = new File("./cmdScript/"+temp.cmdName);
			if(f.exists()) {
				f.delete();
			}
			return;
		}
		
		
		
		private int sendMessage(String msg, String roomName) throws IOException, SocketException, InterruptedException{
			connectionLogic = false;
			while(!getmessageExit) {
				Thread.sleep(1000);
				System.out.println("���� �����");
				}
			synchronized(this) {
			//�޼��� ��ư ���� ���� �۵�
			try {
				OutputStreamWriter osw = new OutputStreamWriter(s.getOutputStream());
				PrintWriter pw = new PrintWriter(osw);
				InputStreamReader isr = new InputStreamReader(s.getInputStream());
				BufferedReader brConnetionc = new BufferedReader(isr);
				BufferedWriter bw = new BufferedWriter(osw);
				
				//�� �޴� ����
				osw.write(2);
				osw.flush();
				System.out.println("�� �޴� ����");
				if(isr.read() != 2) {
					System.out.println("������ : ���� �޴����� ����");
					connectionLogic = true;
					return 1;
				}
				
				//���̸� ������
				bw.write(roomName+"\n");
				bw.flush();
				System.out.println("�� �̸� ����");
				
				//���Ѱ�� ����
				int logic = isr.read();
				if(logic != 0){
					System.out.println("���� ���� ����");
					connectionLogic = true;
					return 1;
				}
				System.out.println("����� ����");
				
				//�޼��� ����
				bw.write(msg+"\n");
				bw.flush();
				System.out.println("�޼��� ���� = "+msg);
				if(isr.read() != 0){
					System.out.println("������ �޼����� ���������� ó������ ���Ͽ���.");
					connectionLogic = true;
					return 1;
				}System.out.println("���� ���������� ����");
				bw.close();
				brConnetionc.close();
				
				
			}catch(SocketException e) {
				exitConnection();
				return 1;
			}
			connectionLogic = true;
			return 0;
		}
	}
		boolean getmessageExit = false;
		//������ �ð� ���� ���� �޼��� �ޱ� ����
		//���� ���� �̸��� �ð��̴�
		private int getMessage() throws IOException, ScriptException, InterruptedException{
			synchronized(this)
			{
				getmessageExit = false;
				OutputStreamWriter osw = new OutputStreamWriter(s.getOutputStream());
				InputStreamReader isr = new InputStreamReader(s.getInputStream());
				BufferedReader br = new BufferedReader(isr);
				String roomNametemp;
				int filecount;
				String temp;
				osw.write(1);
				osw.flush();
				if(isr.read() != 7) {
					//System.out.println("getmessage �޴����� ���Ӹ���");
					getmessageExit = true;
					return 1;
				}
				//System.out.println("�޴��� �巯��");
				
				//���̸� ������
				osw.write(1);
				osw.flush();
				
				//���̸� ���۹���
				roomNametemp = br.readLine();
				//System.out.println("���̸� ����");
				//Ȯ�� ����
				if(roomNametemp == null) {
					//System.out.println("���̸� ���� ����");
					osw.write(1);
					osw.flush();
					return 1;
				}
				osw.write(0);
				osw.flush();
				//System.out.println("���̸� ���� �Ϸ� = "+roomNametemp);
				
				//������ �������ִ� �ش� ���� ���ϸ���Ʈ ���� ����
				filecount = isr.read();
				
				File f= new File("./"+roomNametemp);
				if(!f.exists()){
                    f.mkdir();
                }else if(!f.isDirectory()){
                    f.delete();
                    f.mkdir();
                }
				
				f= new File("./"+roomNametemp+"/message");
				if(!f.exists()){
                    f.mkdir();
                }else if(!f.isDirectory()){
                    f.delete();
                    f.mkdir();
                }
				System.out.println("������ �������ִ� ���� ���� = "+filecount);
				//Ŭ���̾�Ʈ�� �������ִ� ���� ���� ����
				filecount = f.listFiles().length;
				System.out.println("���� ���� ���� ���� = "+filecount);
				osw.write(filecount);
				osw.flush();
				
				//2 ��� 1 ����
				while(isr.read() == 2) {
					osw.write(2);
					osw.flush();
					
					f = new File("./"+roomNametemp+"/message/"+br.readLine());
					BufferedWriter bw = new BufferedWriter(new FileWriter(f));
					System.out.println("���� ��ü ���� �Ϸ�");
					osw.write(3);
					osw.flush();
					
					while(isr.read() == 4) {
						System.out.println("������ ������ �� �ִٰ� �մϴ�");
						osw.write(4);
						osw.flush();
						bw.write((temp=br.readLine()));
						
						System.out.println("�������� ���� ���� ����");
						executeCode(temp);
						osw.write(5);
						osw.flush();
					}
					bw.close();
					System.out.println("�� �۵���");
				}
			}
			
			getmessageExit = true;
			return 0;
		}
		boolean threadLogic = true;
		@Override
		public void run() {
			while(threadLogic) {
				try {
					//System.out.println("�� ����־�"+threadLogic);
					if(connectionLogic) {
						getMessage();	
					}
					Thread.sleep(1000);
				} catch (SocketException e) {
					//�������� ���� ����
					e.printStackTrace();
					System.out.println("������ ���� ����");
					exitConnection();
				} catch (IOException e) {
					e.printStackTrace();
					exitConnection();
				} catch (ScriptException e) {
					e.printStackTrace();
					exitConnection();
				} catch (InterruptedException e) {
					e.printStackTrace();
					exitConnection();
				} 
			}
		}
		
		public void exitConnection() {
			synchronized(connectingList) {
				try {
					s.close();
					threadLogic = false;
					FileWriter fw = new FileWriter(new File("./lastTime"));
					fw.write(String.valueOf(System.currentTimeMillis()/(1000*60)));
					fw.close();
					connectingList.remove(this);
					interrupt();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
			}
		}
	}
}

public class client6{
	public static void main(String[] args){
		client c = new client("0000","0000");
		try {
			c.sysin_menu();
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}