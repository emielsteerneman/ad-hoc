package transport.unicast;

import java.net.InetAddress;

public interface ReliableChannelListener {
	public void onReceive(InetAddress device, byte[] bytes);
}
