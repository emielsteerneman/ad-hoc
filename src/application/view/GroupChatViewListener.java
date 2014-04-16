package application.view;


public interface GroupChatViewListener {
	public void onGroupMessageSend(String message);
	public void onGroupFileSend(byte[] file, String filename);
}
