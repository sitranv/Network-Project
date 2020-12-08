package application;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

public class MyController implements Initializable {
	private Thread[] threads = null;
	private final static int NUM_THREAD = 64;
	@FXML
	private Label hostName, hostIP;
	@FXML
	private Button click, startScan, btnReset, btnStop;
	@FXML
	private TextArea txtResult;
	@FXML
	private ProgressBar statusBar;
	@FXML
	private RadioButton btnDefault, btnRange;
	@FXML
	private TextField ipStart, ipDest, test;
	int[] SubnetMask;
	private long ipNA, ipBA;
	public int range;
	public double count = 0;
	String scanMode = "Default Scan";

	public void getLocalInfo() throws UnknownHostException, SocketException {
		InetAddress local = InetAddress.getLocalHost();
		hostName.setText(local.getHostName());
		hostIP.setText(local.getHostAddress());
		String subnetMask = hostIP.getText().substring(0, hostIP.getText().lastIndexOf(".") + 1);
		ipStart.setText(subnetMask);
		ipDest.setText(subnetMask);
		System.out.println(local.getHostAddress() + "      " + local.getHostName());
		NetworkInterface nw = NetworkInterface.getByInetAddress(local);
		int mask = nw.getInterfaceAddresses().get(0).getNetworkPrefixLength();
		System.out.println("Mask: " + mask);
		System.out.println("SubnetMask:  " + getSubnetmask(mask));
		String[] SubnetMask = getSubnetmask(mask).split("\\.");
		String[] LocalIP = local.getHostAddress().split("\\.");
		StringBuffer NetworkAddress = new StringBuffer();
		StringBuffer BroadcastAddress = new StringBuffer();

		for (int i = 0; i < 4; i++) {
			if (NetworkAddress.length() > 0) {
				NetworkAddress.append(".");
				BroadcastAddress.append(".");
			}
			NetworkAddress.append(Integer.parseInt(LocalIP[i]) & Integer.parseInt(SubnetMask[i]));
			BroadcastAddress.append((Integer.parseInt(LocalIP[i]) & Integer.parseInt(SubnetMask[i]))
					^ (Integer.parseInt(SubnetMask[i]) ^ (0xFF)));
		}
		ipNA = ipToLong(NetworkAddress.toString());
		ipBA = ipToLong(BroadcastAddress.toString());
		System.out.println("NetworkAddress: " + NetworkAddress);
		System.out.println("BroadcastAddress: " + BroadcastAddress);

		range = (int) (ipBA - ipNA + 1);

		System.out.println("Range: " + range);
		txtResult.setText("");
	}

	public String getSubnetmask(int mask) {
		long temp = 0;
		int dem = 0;
		while (mask >= 8) {
			dem++;
			mask -= 8;
		}
		if (mask % 8 != 0) {
			for (int i = 1; i <= mask % 8; i++) {
				temp += (long) power(2, 32 - 8 * dem - i);
			}
		}
		if (dem != 0) {
			for (int i = 1; i <= dem; i++) {
				temp += (long) 255 * power(2, 32 - 8 * i);
			}
		}
		return LongtoIp(temp);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO (don't really need to do anything here).
		try {
			ToggleGroup group = new ToggleGroup();
			btnDefault.setToggleGroup(group);
			btnRange.setToggleGroup(group);
			btnDefault.setSelected(true);
			btnStop.setDisable(true);
			ipStart.setDisable(true);
			ipDest.setDisable(true);
			group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
				@Override
				public void changed(javafx.beans.value.ObservableValue<? extends Toggle> observable, Toggle oldValue,
						Toggle newValue) {
					if (group.getSelectedToggle() != null) {
						RadioButton button = (RadioButton) group.getSelectedToggle();
						scanMode = button.getText().trim();
						System.out.println("You were choosen mode: " + scanMode);
						if (scanMode.equals("Default Scan")) {
							ipStart.setDisable(true);
							ipDest.setDisable(true);
						} else {
							ipStart.setDisable(false);
							ipDest.setDisable(false);
						}
					}
				};
			});
			try {
				getLocalInfo();
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static long ipToLong(String str) {
		String[] ipAddress = str.split("\\.");
		long address;
		long octet1 = (Long.parseLong(ipAddress[0]) * power(2, 24));
		long octet2 = (Long.parseLong(ipAddress[1]) * power(2, 16));
		long octet3 = (Long.parseLong(ipAddress[2]) * power(2, 8));
		long octet4 = (Long.parseLong(ipAddress[3]));
		address = octet1 + octet2 + octet3 + octet4;

		return address;
	}

	public static String LongtoIp(long address) {
		String s = "";
		s = String.format("%d.%d.%d.%d", (address & 0xFF000000) >> 24, (address & 0xFF0000) >> 16,
				(address & 0xFF00) >> 8, (address & 0xFF));
		return s;
	}

	public static long power(int a, int b) {
		long s = 1;
		for (int i = 0; i < b; i++) {
			s *= a;
		}
		return s;
	}

	private String currentDateTime() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis());
	}

	public void RunScan() throws SocketException {
		count = 0;
		btnReset.setDisable(false);
		btnStop.setDisable(false);
		txtResult.setText("");
		if (scanMode.equals("Default Scan")) {
			DefaultScan();
		} else {
			if (checkInput() == false) {
				startScan.setDisable(false);

				btnStop.setDisable(true);
				System.out.println("Nhap lai");
			} else {
				ScanInRange();
			}
		}
	}

	public boolean checkMask(String ip) {
		Long ipL = ipToLong(ip);
		if (ipL >= ipNA && ipL <= ipBA)
			return true;
		return false;
	}

	public boolean checkInput() {
		String ip1 = ipStart.getText().trim();
		String ip2 = ipDest.getText().trim();

		if (ip1.isEmpty() || ip2.isEmpty()) {
			System.out.println("Input Range IP - Try again");
			txtResult.setText("[" + currentDateTime() + "]      Input Range IP - Try again\n");
			return false;
		} else if (ipToLong(ip1) > ipToLong(ip2)) {
			System.out.println("Invalid IP Adrress");
			txtResult.setText("[" + currentDateTime() + "]      Invalid IP Adrress - Try again\n");
			return false;
		} else if (!(checkMask(ip1) && checkMask(ip2))) {
			System.out.println("Invalid SubnetMask");
			txtResult.setText("[" + currentDateTime() + "]      Invalid SubnetMask - Try again\n");
			return false;
		}
		return true;
	}

	public void ScanInRange() throws SocketException {
		System.out.println("Result IP: ");
		txtResult.setText("[" + currentDateTime() + "]      IP Scanner App Ready! \n" + "[" + currentDateTime()
				+ "]      " + "Status change detected: running\n");
		Long ip1 = ipToLong(ipStart.getText().trim());
		Long ip2 = ipToLong(ipDest.getText().trim());

		range = (int) (ip2 - ip1 + 1);
		int numThread = 1;
		for (int i = 1; i < range / 2; i++) {
			if (range % i == 0) {
				numThread = i;
			}
		}
		System.out.println(range + " : " + numThread);
		int ipRange = range / numThread;
		threads = new Scan[numThread];
		for (int i = 0; i < threads.length; i++) {
			System.out.println(LongtoIp(ip1) + " - " + LongtoIp(ip1 + ipRange - 1));
			threads[i] = new Scan(ip1, ip1 + ipRange - 1);
			threads[i].start();
			ip1 += ipRange;
		}
	}

	public void DefaultScan() throws SocketException {
		System.out.println("Result IP: ");
		txtResult.setText("[" + currentDateTime() + "]      IP Scanner App Ready! \n" + "[" + currentDateTime()
				+ "]      " + "Status change detected: running\n");
		threads = new Scan[NUM_THREAD];
		long firstIp = ipNA;
		int ipRange = range / NUM_THREAD;
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new Scan(firstIp, firstIp + ipRange - 1);
			threads[i].start();
			firstIp += ipRange;
		}
	}

	public void Stop() {
		startScan.setDisable(false);
		statusBar.setProgress(0);
		btnReset.setDisable(false);
		for (int i = 0; i < threads.length; i++) {
			threads[i].stop();
		}
		txtResult.appendText("[" + currentDateTime() + "]      Stopped! \n");
	}

	public void Reset() {
		startScan.setDisable(false);
		statusBar.setProgress(0);
		btnStop.setDisable(true);
		txtResult.setText("");
	}

	class Scan extends Thread {
		private long ipStart, ipDest;

		public Scan(long ipStart, long ipDest) {
			this.ipStart = ipStart;
			this.ipDest = ipDest;
		}

		@Override
		public void run() {
			int timeout = 3000;
			for (long i = ipStart; i <= ipDest; i++) {
				String name = LongtoIp(i);
				InetAddress ipAddress;
				try {
					ipAddress = InetAddress.getByName(name);
					if (ipAddress.isReachable(timeout)) {
						String address = ipAddress.getHostAddress();
						String hostName = ipAddress.getHostName();
						if (address.equals(hostName)) {
							hostName = "Unknow";
						}
						txtResult.appendText("[" + currentDateTime() + "]      The IP address " + address + " ("
								+ hostName + ") is available in the network\n");
						System.out.println("[" + currentDateTime() + "]      The IP address " + address + " ("
								+ hostName + ") is available in the network");
						count += (double) 100 / range;
						statusBar.setProgress((double) count / 100);
					} else {
						count += (double) 100 / range;
						statusBar.setProgress((double) count / 100);
					}
					if (count == 100.0) {
						System.out.println("Done!");
						statusBar.setProgress((double) count / 100);
						txtResult.appendText("[" + currentDateTime() + "]      Done! \n");
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
