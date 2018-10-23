/**
 * Section 4.1.5, Page 55

 * skeleton of the Book-BuyerAgent class.
 **/
package Messenger;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;

import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;

import java.util.Vector;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;


public class MsgAgent extends Agent {
	// The list of known receivers agents
	public ArrayList<String> receiverAgents;
	private String msgContent = "";
	private String messagePerformative = "";
	private String converstation = "";
	
	// The GUI to interact with the user
	private MsgGui gui;

	/**
	 * Agent initializations
	 **/
	protected void setup() {
	    // Printout a welcome message
		System.out.println("Messenger-agent "+getAID().getName()+" is ready.");
		
		// Register agent as messaging service provider
		DFAgentDescription dfd = new DFAgentDescription();
	    dfd.setName(getAID());
	    ServiceDescription sd = new ServiceDescription();
	    sd.setType("Messenger-agent");
	    sd.setName(getLocalName()+"-Messenger-agent");
	    dfd.addServices(sd);
	    try {
	    	DFService.register(this, dfd);
	    }
	    catch (FIPAException fe) {
	    	fe.printStackTrace();
	    }
		/** 
		 * Update the list of receiver agents
		**/
	    receiverAgents = new ArrayList<>();
		
	    /*
	    addBehaviour(new CyclicBehaviour(this) {
	    	// Update the list of receiver agents
			@Override
			public void action() {
				DFAgentDescription template = new DFAgentDescription();
		          ServiceDescription sd = new ServiceDescription();
		          sd.setType("Messenger-agent");
		          template.addServices(sd);
		          
		          AID myID = getAID();
		          try {
		            DFAgentDescription[] result = DFService.search(myAgent, template);
		            receiverAgents.clear();
		            
		            for (int i = 0; i < result.length; ++i) {
		            	AID agentID = result[i].getName();
		            	if (!( agentID.equals( myID )
		                		|| agentID.getLocalName().equalsIgnoreCase("ams")
		                		|| agentID.getLocalName().equalsIgnoreCase("df")
		                		|| agentID.getLocalName().equalsIgnoreCase("rma")
		                		|| receiverAgents.contains(agentID))){
		    	            	receiverAgents.add(agentID.getLocalName());
		    	            }
		            }
		         
		          } catch (FIPAException fe) {
			            fe.printStackTrace();
		          }
		          
			}
	    });
	    */
	    
	    
		// Show the GUI to interact with the user
		try {
			gui = new MsgGuiImpl(this);
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		gui.show();
		
		Receive();
	}
	
	
	public void updateReceiverAgents() {
		/*
		AID myID = getAID();
		AMSAgentDescription [] agents = null;
        receiverAgents.clear();
		try {
		    SearchConstraints searchConstraints = new SearchConstraints();
		    searchConstraints.setMaxResults ( new Long(-1) );
		    agents = AMSService.search( this, new AMSAgentDescription (), searchConstraints );
		    System.out.println("");
		}
		catch (Exception e) {  
			e.printStackTrace();
		}
		*/
		
		DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Messenger-agent");
        template.addServices(sd);
        
        AID myID = getAID();
        try {
          DFAgentDescription[] agents = DFService.search(this, template);
          receiverAgents.clear();
          
          for (int i = 0; i < agents.length; ++i) {
          	AID agentID = agents[i].getName();
          	if (!( agentID.equals( myID )
              		|| agentID.getLocalName().equalsIgnoreCase("ams")
              		|| agentID.getLocalName().equalsIgnoreCase("df")
              		|| agentID.getLocalName().equalsIgnoreCase("rma")
              		|| receiverAgents.contains(agentID))){
  	            	receiverAgents.add(agentID.getLocalName());
  	            }
          }
        } catch (FIPAException fe) {
	            fe.printStackTrace();
        }
        
		
		
		
	}
	
	/**
	 * Agent clean-up
	**/
	protected void takeDown() {
		// Dispose the GUI if it is there
		if (gui != null) {
			gui.dispose();
		}
		
		// Deregister agent from the Directory Facilitator 
		// which tracks advertised services
		try {
			DFService.deregister(this);
			System.out.println("Messenger-agent "+getAID().getName()+" has been signed off.");
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		// Printout a dismissal message
		System.out.println("Messenger-agent "+getAID().getName()+"terminated.");
	}

	// Send messages - cyclic and oneshot behaviours
	public void send(
			String msg, 
			String receiverName, 
			String receiverAddress,
			String mp) {
		System.out.println("Sending msg to: " + receiverName);
	    addBehaviour(new SendMsg(this, msg, receiverName, receiverAddress, mp));
	    }
	
	private class SendMsg extends OneShotBehaviour {

		private String receiverName = "";
		private String receiverAddress;
		
		private SendMsg( 
				Agent a, 
				String msg, 
				String rName, 
				String rAddress, 
				String mp) {
			super(a);
			msgContent = msg;
			receiverName = rName;
			receiverAddress = rAddress;
			System.out.println("Receiver Address: " + rAddress);
			messagePerformative = mp;
		}


		@Override
		public void action() {
			AID dest = new AID();
			dest.setName(receiverName);
			dest.addAddresses(receiverAddress);
			ACLMessage msgACL = createMessage(
					messagePerformative, 
					msgContent, 
					dest );
			send(msgACL);
			
			// write updates to gui
			gui.showSentMsg(msgACL);
			
		}
		
		
		private ACLMessage createMessage (String mp, String content, AID dest) {
			ACLMessage msgACL;
			msgACL = new ACLMessage(getPerformative(mp));
			msgACL.setContent(content);
			msgACL.addReceiver(dest);
			
			return msgACL;
		}
	}
	
	// Receive messages
	public void Receive() {
		System.out.println("Receiving a msg ");
	    addBehaviour(new ReceiveMsg());
	    }
	
	private class ReceiveMsg extends CyclicBehaviour {
		@Override
		public void action() {
			ACLMessage msgACL = receive();
			
			if (msgACL != null) {
				// update gui
				gui.showReceivedMsg(msgACL);
				String[] addresses = msgACL.getSender().getAddressesArray();
				for (int i=0; i<addresses.length; i++)
				{
					System.out.println("Sender address " + i + " : " + addresses[i] );
				}
				
				addresses = this.getAgent().getAID().getAddressesArray();
				for (int i=0; i<addresses.length; i++)
				{
					System.out.println("My addresses " + i + " : " + addresses[i] );
				}
			}
		}
		
	}
	
	private int getPerformative (String perf) {
	/** constant identifying the FIPA performative **/
		  final int ACCEPT_PROPOSAL = 0;
		  final int AGREE = 1;
		  final int CANCEL = 2;
		  final int CFP = 3;
		  final int CONFIRM = 4;
		  final int DISCONFIRM = 5;
		  final int FAILURE = 6;
		  final int INFORM = 7;
		  final int INFORM_IF = 8;
		  final int INFORM_REF = 9;
		  final int NOT_UNDERSTOOD = 10;
		  final int PROPOSE = 11;
		  final int QUERY_IF = 12;
		  final int QUERY_REF = 13;
		  final int REFUSE = 14;
		  final int REJECT_PROPOSAL = 15;
		  final int REQUEST = 16;
		  final int REQUEST_WHEN = 17;
		  final int REQUEST_WHENEVER = 18;
		  final int SUBSCRIBE = 19;
		  final int PROXY = 20;
		  final int PROPAGATE = 21;
		  
		  final String[] performatives = new String[22];
		performatives[ACCEPT_PROPOSAL]="ACCEPT-PROPOSAL";
		performatives[AGREE]="AGREE";
		performatives[CANCEL]="CANCEL";
		performatives[CFP]="CFP";
		performatives[CONFIRM]="CONFIRM";
		performatives[DISCONFIRM]="DISCONFIRM";
		performatives[FAILURE]="FAILURE";
		performatives[INFORM]="INFORM";
		performatives[INFORM_IF]="INFORM-IF";
		performatives[INFORM_REF]="INFORM-REF";
		performatives[NOT_UNDERSTOOD]="NOT-UNDERSTOOD";
		performatives[PROPOSE]="PROPOSE";
		performatives[QUERY_IF]="QUERY-IF";
		performatives[QUERY_REF]="QUERY-REF";
		performatives[REFUSE]="REFUSE";
		performatives[REJECT_PROPOSAL]="REJECT-PROPOSAL";
		performatives[REQUEST]="REQUEST";
		performatives[REQUEST_WHEN]="REQUEST-WHEN";
		performatives[REQUEST_WHENEVER]="REQUEST-WHENEVER";
		performatives[SUBSCRIBE]="SUBSCRIBE";
		performatives[PROXY]="PROXY";
		performatives[PROPAGATE]="PROPAGATE";
		
		return Arrays.asList(performatives).indexOf(perf.toUpperCase());
	}
}