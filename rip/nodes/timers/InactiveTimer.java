package projects.rip.nodes.timers;

import projects.rip.nodes.nodeImplementations.FNode;
import sinalgo.nodes.Node;
import sinalgo.nodes.timers.Timer;

public class InactiveTimer extends Timer {
	

	boolean isActive = true; 
	InactiveTimer inactiveTimer = null;
	Node node;

	public void deactivate() {
		isActive = false;
	}
	

	public InactiveTimer(Node node) {
		this.node = node;
		this.isActive = true;
	}

	@Override
	public void fire() {
		if(isActive){
			FNode n = (FNode) this.node;
			for(Node no : n.routingTable.vizinhos) {
				if(no != null) {
					int index = n.routingTable.vizinhos.indexOf(no);
					int aux = n.routingTable.timersVizinho.get(index) - 1;
					n.routingTable.timersVizinho.set(index, aux);
					if(aux == 0) {
						n.shareInactiveNode(no);
						System.out.println("Conxão Perdida"+n);	
					}
				}
			}
			inactiveTimer = new InactiveTimer(this.node);
			inactiveTimer.startRelative(1,this.node);
		}
	}
}
