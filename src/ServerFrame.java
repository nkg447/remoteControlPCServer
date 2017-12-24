
import java.awt.Color;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class ServerFrame extends JFrame implements ActionListener {
	static String OS = System.getProperty("os.name").toLowerCase();
	JLabel status = new JLabel("status - ");
	JButton start = new JButton("Start Connection");
	JTextField port = new JTextField("port number");
	JLabel msg = new JLabel("");

	public ServerFrame() {
		this.setTitle("Server");
		this.setSize(300, 300);
		this.setLayout(null);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		initComponents();
	}

	private void initComponents() {
		String ip = "IP not found autometically fild manually";
		try {
			ip = getLocalIP();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JLabel label = new JLabel("Your local ip is " + ip);
		label.setBounds(20, 20, 200, 40);

		port.setBounds(20, 70, 100, 20);
		start.setBounds(20, 100, 150, 20);
		status.setBounds(20, 130, 250, 40);
		msg.setBounds(20, 170, 250, 40);
		
		port.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				start.doClick();
			}
		});
		
		port.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				port.setText("");
			}
		});

		start.addActionListener(this);

		this.add(port);
		this.add(start);
		this.add(label);
		this.add(status);
		this.add(msg);

		this.setVisible(true);
	}

	private String getLocalIP() throws Exception {
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

		while (interfaces.hasMoreElements()) {
			NetworkInterface networkInterface = interfaces.nextElement();

			if (!networkInterface.isUp())
				continue;

			Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
			while (addresses.hasMoreElements()) {
				InetAddress addr = addresses.nextElement();
				System.out.println(networkInterface.getName()+"   "+addr.getHostAddress());
				if (networkInterface.getName().startsWith("wl") && addr.getHostAddress().length() <= 15) {
					return addr.getHostAddress();
				}
			}
		}
		return null;
	}

	public static void main(String[] args) {
		ServerFrame frame = new ServerFrame();

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				createServer();
				run();

			}
		}).start();

	}

	void createServer() {
		ServerSocket ss = null;
		Socket s = null;
		BufferedReader in = null;
		PrintWriter out = null;
		try {
			ss = new ServerSocket(Integer.parseInt(port.getText()));
			status.setForeground(new Color(0, 128, 0));
			status.setText("Status -Server started at port - " + port.getText());
			this.repaint();

			s = ss.accept();
			status.setForeground(new Color(0, 128, 0));
			status.setText("Status -Connected - :-)");
			this.repaint();

			in = new BufferedReader(new InputStreamReader(s.getInputStream()));

			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), false);

			String str = "";
			while (!str.equals("stop")) {
				str = in.readLine();

				if (str.startsWith("cmd://")) {
					Runtime.getRuntime().exec(str.substring(6));
					continue;
				}

				if (OS.indexOf("win") >= 0 && (str.equals("/home") || str.equals("/media"))) {
					System.out.println("client says: Drives");
					msg.setText("Drives");
					this.repaint();

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
				msg.setText(str);
				msg.setSize(msg.getPreferredSize());
				this.repaint();
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
			status.setForeground(new Color(128, 0, 0));
			status.setText("Status -PORT already in use");
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
		}
	}

}
