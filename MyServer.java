import java.net.*;
import java.util.Enumeration;
import java.util.Scanner;
import java.awt.Desktop;
import java.io.*;

class MyServer {
	static Scanner sc = new Scanner(System.in);
	static String OS = System.getProperty("os.name").toLowerCase();

	public static void main(String args[]) throws Exception {

		test();

		System.out.println("Enter the wlp ip in your android phone\n\n");
		System.out.println("Enter port number");
		int port = sc.nextInt();
		createServer(port);
	}

	static void test() throws Exception {
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

		while (interfaces.hasMoreElements()) {
			NetworkInterface networkInterface = interfaces.nextElement();
			// drop inactive
			if (!networkInterface.isUp())
				continue;

			// smth we can explore
			Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
			while (addresses.hasMoreElements()) {
				InetAddress addr = addresses.nextElement();
				System.out.println(String.format("NetInterface: name [%s], ip [%s]", networkInterface.getDisplayName(),
						addr.getHostAddress()));
			}
		}
	}

	static void createServer(int port) {
		ServerSocket ss = null;
		Socket s = null;
		BufferedReader in = null;
		PrintWriter out = null;
		try {
			ss = new ServerSocket(port);
			System.out.println("Server started at port - " + port);
			s = ss.accept();
			System.out.println("Connected - :-)");
			in = new BufferedReader(new InputStreamReader(s.getInputStream()));

			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), false);

			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

			String str = "", str2 = "";
			while (!str.equals("stop")) {
				str = in.readLine();

				if (str.startsWith("cmd://")) {
					Runtime.getRuntime().exec(str.substring(6));
					continue;
				}

				if (OS.indexOf("win") >= 0 && (str.equals("/home") || str.equals("/media"))) {
					System.out.println("client says: Drives");
					out.println("START_MSG");
					for (File f : File.listRoots()) {
						if (f.isDirectory())
							out.println((char) 16 + f.getPath());
						else
							out.println((char) 17 + f.getPath());
					}
					out.println("END_MSG");
					out.flush();
					continue;
				}
				if (OS.indexOf("win") >= 0 && str.startsWith("/home")) {
					str = str.substring(6);
				}
				if (OS.indexOf("win") >= 0 && str.startsWith("/media")) {
					str = str.substring(7);
				}

				if (!(new File(str).isDirectory())) {
					Desktop desktop = Desktop.getDesktop();
					try {
						desktop.open(new File(str));
					} catch (IOException e1) {
						System.out.println("error");
					}
					continue;
				}

				System.out.println("client says: " + str);
				out.println("START_MSG");
				for (File f : new File(str).listFiles()) {
					if (f.getName().charAt(0) == '.')
						continue;
					if (f.isDirectory())
						out.println((char) 16 + f.getName());
					else
						out.println((char) 17 + f.getName());
				}
				out.println("END_MSG");
				out.flush();
			}
			in.close();
			out.close();
			s.close();
			ss.close();
		} catch (BindException e) {
			System.out.println("port already in use, please re-enter:");
			createServer(sc.nextInt());
		} catch (Exception e) {
			e.printStackTrace();
			try {
				in.close();
				out.close();
				s.close();
				ss.close();
			} catch (Exception ee) {
				// TODO: handle exception
			}
			createServer(port);
		}
	}
}