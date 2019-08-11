package projects.rip.nodes.timers;

import projects.rip.nodes.messages.PayloadMsg;
import projects.rip.nodes.nodeImplementations.FNode;
import sinalgo.nodes.timers.Timer;


public class RetryPayloadMessageTimer extends Timer {
	
	PayloadMsg message; // the msg to send
	boolean isActive = true; // flag that indicates whether the timer should still perform its action
	

	public void deactivate() {
		isActive = false;
	}
	
	/**
	 * @param message The message to resend
	 */
	public RetryPayloadMessageTimer(PayloadMsg message) {
		this.message = message;
	}
	
	@Override
	public void fire() {
		if(isActive) {
			FNode n = (FNode) this.node;
			n.sendPayloadMessage(message); 
			// we could also invalidate the routing entry, and search again
		}
	}

}
