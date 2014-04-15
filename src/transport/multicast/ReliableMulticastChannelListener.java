package transport.multicast;

import java.net.InetAddress;

public interface ReliableMulticastChannelListener {
	public void onMulticastReceive(InetAddress device, byte[] bytes);
}
