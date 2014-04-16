package application.view;

import network.NetworkDevice;

public interface PrivateChatViewListener {
	public void onMessageSend(NetworkDevice device, String message);
	public void onFileSend(NetworkDevice device, byte[] file, String filename);
}
