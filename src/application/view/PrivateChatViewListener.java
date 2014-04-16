package application.view;

import network.NetworkDevice;

public interface PrivateChatViewListener {
	public void onPrivateMessageSend(NetworkDevice device, String message);
	public void onPrivateFileSend(NetworkDevice device, byte[] file, String filename);
}
