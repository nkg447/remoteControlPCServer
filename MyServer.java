import java.net.*;
import java.io.*;

class MyServer {
	public static void main(String args[]) throws Exception {
		ServerSocket ss = new ServerSocket(3333);
		Socket s = ss.accept();
		
		BufferedReader in=new BufferedReader(new InputStreamReader(s.getInputStream()));
		
		PrintWriter out=new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), false);
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		String str = "", str2 = "";
		while (!str.equals("stop")) {
			str = in.readLine();
			
			if(!(new File(str).isDirectory())){
				Runtime.getRuntime().exec("vlc "+str);
				out.println("EXECUTED");
				out.flush();
				continue;
			}
			
			System.out.println("client says: " + str);
			out.println("START_MSG");
			for(File f : new File(str).listFiles()) {
				out.println(f.getName());
			}
			out.println("END_MSG");
			out.flush();
		}
		in.close();
		out.close();
		s.close();
		ss.close();
	}
}
