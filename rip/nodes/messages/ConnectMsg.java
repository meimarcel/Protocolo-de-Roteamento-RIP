package projects.rip.nodes.messages;

import projects.rip.nodes.nodeImplementations.FNode.RoutingTable;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class ConnectMsg extends Message {

	public Node sender;

	public ConnectMsg(Node sender) {
		this.sender = sender;
	}
	
	@Override
	public Message clone() {
		// This message requires a read-only policy
		return this;
	}
	

}
