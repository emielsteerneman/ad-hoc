package network.routing;

import java.io.IOException;
import java.net.InetAddress;

import network.NetworkInterface;
import network.NetworkPacket;

public class SimpleRoutingProtocol implements RoutingProtocol {

	@Override
	public void rout(NetworkPacket networkPacket, NetworkInterface networkInterface) throws IOException {
		if (!networkPacket.getDestinationAddress().equals(InetAddress.getLocalHost())) {
			if (networkPacket.getSourceAddress().equals(InetAddress.getLocalHost())) {
				return;
			} 
			
			if (networkPacket.getHopcount() > 0){
				networkPacket.decrementHopcount();
				
				networkInterface.send(networkPacket);
			} else {
				return;
			}
		} else {
			if (networkPacket.getHopcount() == 0) {
				networkInterface.process(networkPacket);
			} else {
				return;
			}
		}
	}
	
}
