package projects.rip.nodes.nodeImplementations;

import java.awt.Graphics;
import java.awt.Color;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.List;
import java.util.ArrayList;

import projects.defaultProject.models.messageTransmissionModels.ConstantTime;
import projects.rip.nodes.messages.TableMsg;
import projects.rip.nodes.messages.ConnectMsg;
import projects.rip.nodes.messages.FloodFindMsg;
import projects.rip.nodes.messages.PayloadMsg;
import projects.rip.nodes.timers.InactiveTimer;
import projects.rip.nodes.timers.PayloadMessageTimer;
import projects.rip.nodes.timers.RetryFloodingTimer;
import projects.rip.nodes.timers.RetryPayloadMessageTimer;
import projects.rip.nodes.timers.SendConnect;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.io.eps.EPSOutputPrintStream;
import sinalgo.gui.helper.NodeSelectionHandler;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.nodes.messages.NackBox;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.Logging;
import sinalgo.nodes.Connections;
import sinalgo.nodes.edges.Edge;

/**
 * A node that implements a flooding strategy to determine paths to other nodes.
 */
public class FNode extends Node {


	/**
	 * A routing table entry
	 */
	public class RoutingTable {
		public List<Node> noDestino = new ArrayList<Node>();
		public List<String> ipDestino = new ArrayList<String>(); 
		public List<String> mascaraRede = new ArrayList<String>();
		public List<Integer> numeroSalto = new ArrayList<Integer>(); 
		public List<Node> nextHop = new ArrayList<Node>();
		public List<Node> vizinhos = new ArrayList<Node>();
		public List<Integer> timersVizinho = new ArrayList<Integer>();
		 

		public RoutingTable(Connections outgoingConnections) {
			for (Edge edge : outgoingConnections) {
				this.noDestino.add(edge.endNode);
				this.vizinhos.add(edge.endNode);
				this.ipDestino.add(((FNode) edge.endNode).ip);
				this.mascaraRede.add(((FNode) edge.endNode).mascaraRede);
				this.numeroSalto.add(1);
				this.nextHop.add(edge.endNode);
				this.timersVizinho.add(180);
			}
		}
		public RoutingTable() {

		}
		public void updateNumeroSalto(Node no, int numSalto) {
			int index = noDestino.indexOf(no);
			if(index != -1) {
				numeroSalto.set(index, numSalto);
			}
		} 

		public void updateNextHop(Node no, Node newNextHop) {
			int index = noDestino.indexOf(no);
			if(index != -1) {
				nextHop.set(index, newNextHop);
			}
		} 

		public String getIpDestino(Node no) {
			int index = noDestino.indexOf(no);
			if(index != -1) {
				return ipDestino.get(index);
			} else {
				return null;
			}
		}

		public String getMascaraRede(Node no) {
			int index = noDestino.indexOf(no);
			if(index != -1) {
				return mascaraRede.get(index);
			} else {
				return null;
			}
		}

		public Node getNextHop(Node no) {
			int index = noDestino.indexOf(no);
			if(index != -1) {
				return nextHop.get(index);
			} else {
				return null;
			}
		}
		
		public int getNumeroSalto(Node no) {
			int index = noDestino.indexOf(no);
			if(index != -1) {
				return numeroSalto.get(index);
			} else {
				return 0;
			}
		}

		public void resetTimer(Node no) {
			int index = vizinhos.indexOf(no);
			if(index != -1) {
				timersVizinho.set(index, 180);
			}
		}
		
	}
	
	public int seqID = 0; 
	public static int cont = 1;
	public static int timer = 0;
	public String ip = "10.0.0."+String.valueOf(cont++);
	public String mascaraRede = "255.255.255.0";
	public RoutingTable routingTable = new RoutingTable(this.outgoingConnections);

	public void clearRoutingTable() {
		routingTable = new RoutingTable();
	}

	public void initRouter() {
		routingTable = new RoutingTable(this.outgoingConnections);
		RetryFloodingTimer r = new RetryFloodingTimer();
		r.startRelative(30+(timer+=1), this);
		InactiveTimer in = new InactiveTimer(this);
		in.startRelative(1,this);
	}

	@NodePopupMethod(menuText = "Initialize Node")
	public void initNode() {
		routingTable = new RoutingTable(this.outgoingConnections);
		RetryFloodingTimer r = new RetryFloodingTimer();
		ConnectMsg msg = new ConnectMsg(this);
		SendConnect sc = new SendConnect(msg, this);
		sc.startRelative(1,this);
		r.startRelative(30+(timer+=1), this);
		InactiveTimer in = new InactiveTimer(this);
		in.startRelative(1,this);
	}
	
	@Override
	public void checkRequirements() throws WrongConfigurationException {
		if(!(Tools.getMessageTransmissionModel() instanceof ConstantTime)) {
			Tools.fatalError("This project requires that messages are sent with the ConstantTime MessageTransmissionModel.");
		}
	}

	@Override
	public void handleMessages(Inbox inbox) {
		while(inbox.hasNext()) {
			Message msg = inbox.next();

			if(msg instanceof ConnectMsg) {
				ConnectMsg m = (ConnectMsg) msg;
				if(this.routingTable.vizinhos.indexOf(m.sender) == -1 ) {
					if(this.routingTable.noDestino.indexOf(m.sender) == -1){
						this.routingTable.noDestino.add(m.sender);
						this.routingTable.vizinhos.add(m.sender);
						this.routingTable.ipDestino.add(((FNode)m.sender).ip);
						this.routingTable.mascaraRede.add(((FNode)m.sender).mascaraRede);
						this.routingTable.numeroSalto.add(1);
						this.routingTable.nextHop.add(m.sender);
						this.routingTable.timersVizinho.add(180);
					} else {
						this.routingTable.vizinhos.add(m.sender);
						this.routingTable.numeroSalto.set(this.routingTable.noDestino.indexOf(m.sender),1);
						this.routingTable.updateNextHop(m.sender,m.sender);
						this.routingTable.timersVizinho.add(180);

					}
				}
				TableMsg tm = new TableMsg(m.sender, this, this.routingTable);
				send(tm, m.sender);
			}
			
			if(msg instanceof TableMsg) {
				TableMsg m = (TableMsg) msg;
				if(this.routingTable.vizinhos.indexOf(m.sender) == -1) {
					if(this.routingTable.noDestino.indexOf(m.sender) == -1){
						this.routingTable.noDestino.add(m.sender);
						this.routingTable.vizinhos.add(m.sender);
						this.routingTable.ipDestino.add(((FNode)m.sender).ip);
						this.routingTable.mascaraRede.add(((FNode)m.sender).mascaraRede);
						this.routingTable.numeroSalto.add(1);
						this.routingTable.nextHop.add(m.sender);
						this.routingTable.timersVizinho.add(180);
					} else {
						this.routingTable.vizinhos.add(m.sender);
						this.routingTable.numeroSalto.set(this.routingTable.noDestino.indexOf(m.sender),1);
						this.routingTable.updateNextHop(m.sender,m.sender);
						this.routingTable.timersVizinho.add(180);

					}
				} 
				RoutingTable rt = m.routingTable;
				for(Node n : rt.noDestino) {
					int index = this.routingTable.noDestino.indexOf(n);
					if(index == -1 && !this.equals(n)) {
						this.routingTable.noDestino.add(n);
						this.routingTable.ipDestino.add(rt.getIpDestino(n));
						this.routingTable.mascaraRede.add(rt.getMascaraRede(n));
						this.routingTable.numeroSalto.add(rt.getNumeroSalto(n) + 1);
						this.routingTable.nextHop.add(m.sender);

					} else if(rt.getNumeroSalto(n) < this.routingTable.getNumeroSalto(n)) { 
						this.routingTable.updateNumeroSalto(n, rt.getNumeroSalto(n) + 1);
						this.routingTable.updateNextHop(n, m.sender);
					}
				}
				FloodFindMsg b = new FloodFindMsg(++this.seqID, FNode.this, m.sender, this.routingTable);
				this.broadcast(b);

			}
			// ---------------------------------------------------------------
			if(msg instanceof FloodFindMsg) { // This node received a flooding message. 
				// ---------------------------------------------------------------
				FloodFindMsg m = (FloodFindMsg) msg;
				if(this.routingTable.vizinhos.indexOf(m.sender) == -1) {
					this.routingTable.noDestino.add(m.sender);
					this.routingTable.vizinhos.add(m.sender);
					this.routingTable.ipDestino.add(((FNode)m.sender).ip);
					this.routingTable.mascaraRede.add(((FNode)m.sender).mascaraRede);
					this.routingTable.numeroSalto.add(1);
					this.routingTable.nextHop.add(m.sender);
					this.routingTable.timersVizinho.add(180);
				} else {
					this.routingTable.resetTimer(m.sender);
				}
				boolean forward = false;
				if(m.origem.equals(this)) {
					forward = false; 
				} else { 
					RoutingTable rt = m.routingTable;
					for(Node n : rt.noDestino) {
						int index = this.routingTable.noDestino.indexOf(n);
						if(index == -1 && !this.equals(n)) { 
							this.routingTable.noDestino.add(n);
							this.routingTable.ipDestino.add(rt.getIpDestino(n));
							this.routingTable.mascaraRede.add(rt.getMascaraRede(n));
							this.routingTable.numeroSalto.add(rt.getNumeroSalto(n) + 1);
							this.routingTable.nextHop.add(m.sender);
							forward = true;
						} else if(rt.getNumeroSalto(n) < this.routingTable.getNumeroSalto(n) || this.routingTable.vizinhos.indexOf(this.routingTable.getNextHop(n)) == -1) { // update the existing entry 
							this.routingTable.updateNumeroSalto(n, rt.getNumeroSalto(n) + 1);
							this.routingTable.updateNextHop(n, m.sender);
							forward = true;
						}
						if(m.att && rt.getNumeroSalto(n) == 16 && this.routingTable.vizinhos.indexOf(n) == -1) {
							this.routingTable.updateNumeroSalto(n,16);
							this.routingTable.updateNextHop(n, m.sender);
							forward = true;

						}
					}
				}


				if(forward) { 
					FloodFindMsg b = new FloodFindMsg(++this.seqID, FNode.this, m.sender, this.routingTable);
					if(m.att){ b.att = true;}
					this.broadcast(b);
				}
			} 
			// ---------------------------------------------------------------
			if(msg instanceof PayloadMsg) {
				PayloadMsg m = (PayloadMsg) msg;
				if(m.destination.equals(this)) { 
					this.setColor(Color.YELLOW);
					System.out.println(this.ip);			
				} else {
					if(m.ttl < 15) {
						m.ttl++;
						this.setColor(Color.GREEN);
						System.out.println(this.ip);
						sendPayloadMessage(m);
					}
				}
			} 
		}
	}

	
	@NodePopupMethod(menuText = "Send Message To...")
	public void sendMessageTo() {
		Tools.getNodeSelectedByUser(new NodeSelectionHandler() {
			public void handleNodeSelectedEvent(Node n) {
				if(n == null) {
					return; // aborted
				}
				PayloadMsg msg = new PayloadMsg(n, FNode.this);
				//msg.requireACK = false;
				msg.sequenceNumber = ++FNode.this.seqID;
				PayloadMessageTimer t = new PayloadMessageTimer(msg);
				t.startRelative(1, FNode.this);
				FNode.this.setColor(Color.GREEN);
				System.out.println();
			}
		}, "Select a node to send a message to...");
	}


	/**
	 * Tries to send a message if there is a routing entry. 
	 * If there is no routing entry, a search is started, and the
	 * message is put in a buffer of messages on hold.
	 * @param msg
	 * @param to
	 */
	public void sendPayloadMessage(PayloadMsg msg) {
		Node nh = this.routingTable.getNextHop(msg.destination);
		if(nh != null) {
			if(this.routingTable.getNumeroSalto(msg.destination) - 15 <= msg.ttl) {
				send(msg, nh);
			} else {
				System.out.println("Nó inalcançável");
			}
			return ;
		} else {
			System.out.println("Nó inalcançável");
		}
	}
	
	/**
	 * Starts a search for a given node with the given TTL
	 * @param destination
	 * @param ttl
	 */
	public void lookForNode() {
		FloodFindMsg m = new FloodFindMsg(++this.seqID, this, this, this.routingTable);
		RetryFloodingTimer rft = new RetryFloodingTimer();
		rft.startRelative(30, this); 
		m.retryTimer = rft;
		this.broadcast(m);
	}

	public void shareInactiveNode(Node node) {
		for(Node n : this.routingTable.noDestino) {
			if(this.routingTable.getNextHop(n).equals(node)) {
				this.routingTable.updateNumeroSalto(n, 16);
				this.routingTable.updateNextHop(n, null);				
			}
		}
		this.routingTable.updateNumeroSalto(node, 16);
		this.routingTable.vizinhos.set(this.routingTable.vizinhos.indexOf(node), null);
		this.routingTable.updateNextHop(node, null);
		FloodFindMsg m = new FloodFindMsg(++this.seqID, this, this, this.routingTable);
		m.att = true;
		this.broadcast(m);
	}
	
	@Override
	public void init() {
	}

	@Override
	public void neighborhoodChange() {
		// we could remove routing-table entries that use this neighbor
	}

	@Override
	public void preStep() {
	}

	@Override
	public void postStep() {
	}
	
	/* (non-Javadoc)
	 * @see sinalgo.nodes.Node#toString()
	 */
	@NodePopupMethod(menuText = "Print Table")
	public void tprintTable() {
		System.out.println();
		System.out.println("Node: "+this+"       IP: "+this.ip);
		System.out.println("IP           | Hop              | NextHop");
		for(Node n : this.routingTable.noDestino) {
			System.out.println(this.routingTable.getIpDestino(n)+"....| "+this.routingTable.getNumeroSalto(n)+"....|"+this.routingTable.getNextHop(n));
		}
		System.out.println();
	}

	@Override
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		// overwrite the draw method to change how the GUI represents this node
		if(true) {
			super.drawNodeAsDiskWithText(g, pt, highlight, this.ip+"/"+this.mascaraRede, 16, Color.WHITE);
		} else {
			super.drawNodeAsSquareWithText(g, pt, highlight, this.ip+"/"+this.mascaraRede, 16, Color.WHITE);
		}
		
	}
	
	public void drawToPostScript(EPSOutputPrintStream pw, PositionTransformation pt) {
		if(true) {
			super.drawToPostScriptAsDisk(pw, pt, drawingSizeInPixels/2, getColor());
		} else {
			super.drawToPostscriptAsSquare(pw, pt, drawingSizeInPixels, getColor());
		}
	}
}
