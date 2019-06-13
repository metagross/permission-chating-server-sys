

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
	
	//데스크탑 전용
	private boolean menuLogic = true;
	
	public client(String id, String passwd){
		this.id = id;
		this.passwd = passwd;
	}
	
	//방 들어가기
	private int newEnterTheRoom(String host, int port) throws IOException{
		Socket s = new Socket(host, port);
		if(s == null) {
			System.out.println("해당 포트로 접속하지 못했습니다.");
			return 1;
		}
		
		connection c = new connection(s);
		if(c == null) {
			return 1;
		}
		c.start();
		
		System.out.println("서버 접속 및 수신 스레드 시작 완료");
		return 0;
	}
	
	public void sysin_menu() throws IOException, InterruptedException {
		Scanner sc = new Scanner(System.in);

		String t;
		int p;
		while(menuLogic) {
			System.out.println("0. 종료\n1. 메세지 보내기\n2. cmd 추가\n3.방 들어가기\n4.현재 접속한 방 리스트\n");
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
				
				
				System.out.println("서버를 골라주세요(종료는 -1)");
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
					
				
				
				System.out.println("방의 번호를 고르세요(종료는 -1)");
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
				
				System.out.println("메세지를 입력하세요");
				temp.sendMessage(sc.nextLine(), temp.roomNameList.get(tempNum));
				sc.nextLine();
				break;
			case 2 :
				//cmd 추가
				System.out.println("메세지 안에서 뽑을 커멘드를 입력하세요. 종료하시려면 ` 를 입력하세요\n");
				arylistString = new ArrayList<String>();
				while(!(t = sc.nextLine()).equals("`")) {
					arylistString.add(t);
				}
				
				break;
			case 3 :
				int po;
				System.out.print("새로 들어갈 방의 ip를 입력해주세요 : ");
				t = sc.nextLine();
				System.out.print("새로 들어갈 방의 port를 입력해주세요(보통 8988) : ");
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
		//불꺼 불꺼 불꺼 cmd_Data 정리필요
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
		private String cmdStringCode;//아주 나중에 구현
		private String cmdName;
		private String[] cmdAry;
		private File f;
		private int instanceNum;
		
		//데이터 하나를 데이터베이스에 추가하는 명령
		//filePath는 복사할 자바스크립트 파일경로이다.
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
		
		//이 함수를 오버라이딩 하세요
		private synchronized String run(String[] args) throws IOException, ScriptException {//아주 나중에 구현
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
			
			//아이디 길이 보냄
			osw.write(id.length());
			osw.flush();
			if(isr.read() != 0) {
				new Exception("serverError");
				return;
			}
			
			
			//아이디 보냄
			osw.write(id);
			osw.flush();			
			//아이디 확인 완료
			if(isr.read() != 0) {
				new Exception("serverError");
				return;
			}
			
			//패스워드 길이 전송
			osw.write(passwd.length());
			osw.flush();
			if(isr.read() != 0) {
				new Exception("serverError");
				return;
			}
			
			
			//패스워드 전송
			osw.write(passwd);
			osw.flush();
			
			//서버 확인
			if(isr.read() != 0) {
				new Exception("serverError");
				return;
			}
			
			//방목록 요청
			osw.write(0);
			osw.flush();
			
			
			//방목록받음
			while(isr.read() == 1) {
				System.out.println("1읽음");
				osw.write(2);
				osw.flush();
				System.out.println("2보냄");
				roomNameList.add(br.readLine());
				System.out.println("방이름 읽음");
				osw.write(3);
				osw.flush();
				System.out.println("3보냄");
			}
			
			connectingList.add(this);
			System.out.println("4연결완료");
			//연결완료
		}
		
		private int menu(int p,String[] args) throws IOException, InterruptedException, ScriptException{
			OutputStreamWriter osw = new OutputStreamWriter(s.getOutputStream());
			osw.write(p);
			switch(p){
				case 1 :
					//스레드에서 알아서 받긴 하겠지만, 혹시몰라 추가함
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

		//cmd_run이랑 같음
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
				System.out.println("한조 대기중");
				}
			synchronized(this) {
			//메세지 버튼 보낸 이후 작동
			try {
				OutputStreamWriter osw = new OutputStreamWriter(s.getOutputStream());
				PrintWriter pw = new PrintWriter(osw);
				InputStreamReader isr = new InputStreamReader(s.getInputStream());
				BufferedReader brConnetionc = new BufferedReader(isr);
				BufferedWriter bw = new BufferedWriter(osw);
				
				//방 메뉴 보냄
				osw.write(2);
				osw.flush();
				System.out.println("방 메뉴 보냄");
				if(isr.read() != 2) {
					System.out.println("보내기 : 방의 메뉴조차 못들어감");
					connectionLogic = true;
					return 1;
				}
				
				//방이름 보내줌
				bw.write(roomName+"\n");
				bw.flush();
				System.out.println("방 이름 보냄");
				
				//권한결과 받음
				int logic = isr.read();
				if(logic != 0){
					System.out.println("방의 권한 없음");
					connectionLogic = true;
					return 1;
				}
				System.out.println("방권한 받음");
				
				//메세지 보냄
				bw.write(msg+"\n");
				bw.flush();
				System.out.println("메세지 보냄 = "+msg);
				if(isr.read() != 0){
					System.out.println("서버가 메세지를 정상적으로 처리하지 못하였다.");
					connectionLogic = true;
					return 1;
				}System.out.println("서버 정상적으로 받음");
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
		//마지막 시간 보낸 이후 메세지 받기 실행
		//받은 파일 이름이 시간이다
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
					//System.out.println("getmessage 메뉴조차 접속못함");
					getmessageExit = true;
					return 1;
				}
				//System.out.println("메뉴는 드러감");
				
				//방이름 보내줘
				osw.write(1);
				osw.flush();
				
				//방이름 전송받음
				roomNametemp = br.readLine();
				//System.out.println("방이름 받음");
				//확인 보냄
				if(roomNametemp == null) {
					//System.out.println("방이름 받지 못함");
					osw.write(1);
					osw.flush();
					return 1;
				}
				osw.write(0);
				osw.flush();
				//System.out.println("방이름 수신 완료 = "+roomNametemp);
				
				//서버가 가지고있는 해당 방의 파일리스트 개수 받음
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
				System.out.println("서버가 가지고있는 파일 개수 = "+filecount);
				//클라이언트가 가지고있는 파일 개수 전송
				filecount = f.listFiles().length;
				System.out.println("내가 가진 파일 개수 = "+filecount);
				osw.write(filecount);
				osw.flush();
				
				//2 계속 1 종료
				while(isr.read() == 2) {
					osw.write(2);
					osw.flush();
					
					f = new File("./"+roomNametemp+"/message/"+br.readLine());
					BufferedWriter bw = new BufferedWriter(new FileWriter(f));
					System.out.println("파일 객체 생성 완료");
					osw.write(3);
					osw.flush();
					
					while(isr.read() == 4) {
						System.out.println("서버가 보내줄 게 있다고 합니다");
						osw.write(4);
						osw.flush();
						bw.write((temp=br.readLine()));
						
						System.out.println("서버에게 글자 라인 받음");
						executeCode(temp);
						osw.write(5);
						osw.flush();
					}
					bw.close();
					System.out.println("잘 작동함");
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
					//System.out.println("나 살아있어"+threadLogic);
					if(connectionLogic) {
						getMessage();	
					}
					Thread.sleep(1000);
				} catch (SocketException e) {
					//서버와의 연결 종료
					e.printStackTrace();
					System.out.println("서버와 연결 종료");
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