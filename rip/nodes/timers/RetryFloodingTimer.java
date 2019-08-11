package projects.rip.nodes.timers;

import projects.rip.nodes.nodeImplementations.FNode;
import sinalgo.nodes.Node;
import sinalgo.nodes.timers.Timer;

/**
 * A timer that is set when flooding. When there is no response after
 * the given amount of time, this timer tirggers another flooding
 * search for the destination, with increased TTL.
 */
public class RetryFloodingTimer extends Timer {

	boolean isActive = true; // used to disable this timer. If false, this timer does perform its action anymore.

	public void deactivate() {
		isActive = false;
	}

	public RetryFloodingTimer() {

		this.isActive = true;
	}

	@Override
	public void fire() {
		if(isActive){
			FNode n = (FNode) this.node;
			n.lookForNode(); // restart a flooding search with TTL twice as big
		}
	}
}
