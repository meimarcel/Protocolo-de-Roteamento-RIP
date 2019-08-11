package projects.rip.nodes.messages;

import projects.rip.nodes.nodeImplementations.FNode.RoutingTable;
import projects.rip.nodes.timers.RetryFloodingTimer;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;


public class FloodFindMsg extends Message {

	public Node sender;
	
	

	public int sequenceID;
	public boolean att = false; 
	
	public RetryFloodingTimer retryTimer = null;

	public Node origem;
	public RoutingTable routingTable;
	

	public FloodFindMsg(int seqID, Node sender, Node origem, RoutingTable routingTable) {

		sequenceID = seqID;
		this.sender = sender;
		this.routingTable = routingTable;
		this.origem = origem;
	}
	
	@Override
	public Message clone() {
		// This message requires a read-only policy
		return this;
	}
	
	
	/**
	 * @return A real clone of this message, i.e. a new message object
	 */
	public FloodFindMsg getRealClone() {
		FloodFindMsg msg = new FloodFindMsg(this.sequenceID, this.sender, this.origem, this.routingTable);
		msg.routingTable = this.routingTable;
		msg.retryTimer = this.retryTimer;
		return msg;
	}

}
