package projects.rip.nodes.messages;

import projects.rip.nodes.timers.RetryPayloadMessageTimer;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class PayloadMsg extends Message {
	public Node destination;
	public Node sender; 
	public int sequenceNumber; 
	public int ttl = 0; 
	public RetryPayloadMessageTimer ackTimer; 
	
	/**
	 * Constructor
	 * @param destination The node to send this msg to
	 * @param sender The sender who sends this msg
	 */
	public PayloadMsg(Node destination, Node sender) {
		this.destination = destination;
		this.sender = sender;
	}
	
	@Override
	public Message clone() {
		return this; // requires the read-only policy
	}

}
