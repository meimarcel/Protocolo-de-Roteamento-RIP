package projects.rip.nodes.timers;

import projects.rip.nodes.messages.ConnectMsg;
import projects.rip.nodes.nodeImplementations.FNode;
import sinalgo.nodes.Node;
import sinalgo.nodes.timers.Timer;

public class SendConnect extends Timer {
	

    public ConnectMsg msg;
    public Node node;


	public SendConnect(ConnectMsg msg, Node node) {
        this.msg = msg;
        this.node = node;
	}

	@Override
	public void fire() {
        this.node.broadcast(msg);
	}
}
