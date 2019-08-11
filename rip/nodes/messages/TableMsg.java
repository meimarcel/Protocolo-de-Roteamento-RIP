package projects.rip.nodes.messages;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;
import projects.rip.nodes.nodeImplementations.FNode.RoutingTable;


public class TableMsg extends Message {
	
	/**
	 * Constructor
	 * @param destination The node to whom the ACK should be sent
	 * @param sender The node that acknowledges receipt of a message
	 */
	public Node destination;
	public Node sender;
	public RoutingTable routingTable;

	public TableMsg(Node destination, Node sender, RoutingTable routingTable) {
		this.destination = destination;
		this.sender = sender;
		this.routingTable = routingTable;
	}

	@Override
	public Message clone() {
		return this; // read-only policy
	}

}
