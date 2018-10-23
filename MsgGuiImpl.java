package Messenger;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.gui.TimeChooser;
import jade.lang.acl.ACLMessage;

import java.awt.*;
import java.awt.event.*;

import javax.crypto.CipherInputStream;
import javax.swing.*;
import javax.swing.SpringLayout.Constraints;
import javax.swing.border.*;
import javax.swing.text.StyleConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * J2SE (Swing-based) implementation of the GUI of the agent that 
 * acts as a messenger to communicate with other active agents
 */
public class MsgGuiImpl extends JFrame implements MsgGui {
	private MsgAgent myAgent;
	
	private JFrame mainFrame;
	private JPanel settingsPanel,
					performativePanel, 
					receiverNamePanel, 
					platformNamePanel,
					addressPanel,
					msgPanel, 
					convPanel, 
					sentPanel, 
					receivedPanel; 
	
	private JTabbedPane tabbedPane;
	
	private JButton 	sendB;
	private JLabel 		msgPerformativeL, 
						recevierL, 
						conversationL, 
						platformL, 
						addressL;
	private JComboBox<String> 	msgPerformativeCB;

	private JComboBox<String> receiverCB;
	private JTextField 	msgContentTF, platformTF, addressTF;
	private JTextArea 	conversationTA, sentTA, receivedTA;
	
	private Font labelFont    	= new Font("Sans", Font.PLAIN, 34/2);
	private Font contentFont 	= new Font("Sans", Font.PLAIN, 32/2);
	Dimension labelDimension 	= new Dimension(400/2,50/2);
	Dimension toFillDimension 	= new Dimension(600/2,50/2);
	Dimension conversationDim 	= new Dimension(900/2,500/2);
	
	private String[] mPerformativeNames;
	private ArrayList<String> receiverNames;
 	
	
	public MsgGuiImpl(MsgAgent a) throws FIPAException {
		
		super(a.getLocalName());
		
		myAgent = a;
		
		// Delete agent when pressing window closing button
		addWindowListener(new	WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				myAgent.doDelete();
			}
		} );

		String[] mp = ACLMessage.getAllPerformativeNames();
		mPerformativeNames = Arrays.asList(mp).
				stream().map(String::toLowerCase).toArray(String[]::new);
		
		receiverNames = new ArrayList<>();
	
    
    settingsPanel = new JPanel();
	settingsPanel.setLayout(new GridBagLayout());
    settingsPanel.setPreferredSize(new Dimension(1000/2, 400/2));
    GridBagConstraints constraints = new GridBagConstraints();
	constraints.insets = new Insets(10/2, 10/2, 10/2, 50/2);
	///////////
	// Line 0
	///////////
	recevierL = new JLabel("To");
    recevierL.setHorizontalAlignment(SwingConstants.RIGHT);
    recevierL.setFont(labelFont);
    constraints.gridx = 0;
    constraints.gridy = 1;
    constraints.anchor = GridBagConstraints.EAST;
    settingsPanel.add(recevierL, constraints);
    
    receiverCB = new JComboBox(receiverNames.toArray());
	receiverCB.setPreferredSize(toFillDimension);
	receiverCB.setFont(contentFont);
	receiverCB.setEditable(true);
	receiverCB.setBackground(Color.white);
	
	constraints.gridx = 1;
	constraints.gridy = 1;
	settingsPanel.add(receiverCB, constraints);
	
	///////////
	// Line 1
	///////////
	JButton updateRcvListB = new JButton("Update receivers list");
	updateRcvListB.setFont(labelFont);
	updateRcvListB.setPreferredSize(toFillDimension);
	constraints.gridx = 1;
	constraints.gridy = 2;
//	constraints.gridwidth = 2;
	updateRcvListB.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			myAgent.updateReceiverAgents();
			updateReceiverNames();
		}
	});
	settingsPanel.add(updateRcvListB, constraints);
    
	
	///////////
	// Line 2
	///////////
	msgPerformativeL = new JLabel("Message type");
    msgPerformativeL.setHorizontalAlignment(SwingConstants.RIGHT);
	msgPerformativeL.setFont(labelFont);
	constraints.gridx = 0;
	constraints.gridy = 3;
    settingsPanel.add(msgPerformativeL, constraints);

    
    msgPerformativeCB = new JComboBox(mPerformativeNames);
    msgPerformativeCB.setPreferredSize(toFillDimension);
    msgPerformativeCB.setFont(contentFont);
    msgPerformativeCB.setBackground(Color.white);
    constraints.gridx = 1;
    constraints.gridy = 3;
    settingsPanel.add(msgPerformativeCB, constraints);
    
    
	///////////
	// Line 3
	///////////
    platformL = new JLabel("Platform name");
	platformL.setHorizontalAlignment(SwingConstants.RIGHT);
	platformL.setFont(labelFont);
	constraints.gridx = 0;
	constraints.gridy = 4;
	settingsPanel.add(platformL, constraints);
	
	platformTF = new JTextField();
	platformTF.setEditable(true);
	platformTF.setPreferredSize(toFillDimension);
	platformTF.setFont(contentFont);
	platformTF.setText(myAgent.getName().split("@")[1]);
	
	constraints.gridx = 1;
	constraints.gridy = 4;
	settingsPanel.add(platformTF, constraints);
	
	///////////
	// Line 5
	///////////
	addressL = new JLabel("Host address");
	addressL.setHorizontalAlignment(SwingConstants.RIGHT);
	addressL.setFont(labelFont);
	constraints.gridx = 0;
	constraints.gridy = 5;
	settingsPanel.add(addressL, constraints);
	
	addressTF = new JTextField();
	addressTF.setEditable(true);
	addressTF.setPreferredSize(toFillDimension);
	addressTF.setFont(contentFont);
	String localAddress = myAgent.getAID().getAddressesArray()[0];
//	addressTF.setText("http://localhost:7778/acc");
	addressTF.setText(localAddress);
	constraints.gridx = 1;
	constraints.gridy = 5;
	settingsPanel.add(addressTF, constraints);
    
	///////////
	// Line 4
	///////////
	
	JPanel conversationPanel = new JPanel();
	BoxLayout convBoxlayout = new BoxLayout(conversationPanel, BoxLayout.Y_AXIS);
	conversationPanel.setLayout(convBoxlayout);
	conversationPanel.setPreferredSize(new Dimension(400/2, 500/2));
	
	conversationL = new JLabel("Conversation");
	conversationL.setHorizontalAlignment(SwingConstants.LEFT);
	conversationL.setFont( new Font("Sans", Font.BOLD, 36));
	conversationPanel.add(conversationL);
		
	///////////
	// Line 5
	///////////
	conversationTA = new JTextArea();
	conversationTA.setPreferredSize(conversationDim);
	conversationTA.setEnabled(false);
	conversationTA.setAlignmentY(SwingConstants.LEFT);
	conversationTA.setFont(new Font("Sans", Font.PLAIN, 24));
	JScrollPane scrollPaneConversation = new JScrollPane(conversationTA);
	scrollPaneConversation.setPreferredSize(new Dimension(1000/2,300/2));
	scrollPaneConversation.setMinimumSize(new Dimension(1000/2,300/2));
	conversationPanel.add(scrollPaneConversation);
	conversationPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
	
	constraints.weightx = 0.0;
	constraints.weighty = 0.0;
	
	
	///////////
	// Line 6
	///////////
	JPanel msgPanel = new JPanel();
	msgPanel.setLayout(new GridBagLayout());
	msgPanel.setPreferredSize(new Dimension(1000/2, 200/2));
	constraints = new GridBagConstraints();
	constraints.insets = new Insets(0,0,0,0);
	constraints.anchor = GridBagConstraints.EAST;
	msgContentTF = new JTextField();
	msgContentTF.setPreferredSize(new Dimension (700/2, 100/2));
	msgContentTF.setEnabled(true);
	msgContentTF.setFont(contentFont);
	msgContentTF.setBackground(Color.WHITE);
	msgContentTF.setText("Write a message...");
	msgContentTF.addMouseListener(cleanTF);
	msgContentTF.setHorizontalAlignment(SwingConstants.LEFT);
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.gridwidth = 2;
	msgPanel.add(msgContentTF, constraints);
	
	sendB = new JButton("Send");
	sendB.setFont(labelFont);
	sendB.setPreferredSize(new Dimension(200/2, 100/2));
	constraints.gridx = 2;
	constraints.gridy = 0;
	msgPanel.add(sendB, constraints);
	
	sendB.addActionListener(onSendBClicked);
	
	
	Dimension scrollDim = new Dimension(800/2, 700/2);
	Dimension taDim = new Dimension(800/2, 800/2);
	
	convPanel = new JPanel();
	conversationTA = new JTextArea();
	conversationTA.setEnabled(false);
//	conversationTA.setFont(contentFont);
	JScrollPane allMsgsScroll = new JScrollPane(conversationTA);
//	allMsgsScroll.setMinimumSize(scrollDim);
//	allMsgsScroll.setMaximumSize(scrollDim);
	allMsgsScroll.setPreferredSize(scrollDim);
	conversationTA.setPreferredSize(taDim);
	conversationPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
	convPanel.add(allMsgsScroll);
	
	sentPanel = new JPanel();
	sentTA = new JTextArea();
	sentTA.setEnabled(false);
//	sentTA.setFont(contentFont);
	JScrollPane sentMsgsScroll = new JScrollPane(sentTA);
	sentMsgsScroll.setPreferredSize(scrollDim);
//	sentTA.setPreferredSize(taDim);
	sentMsgsScroll.setMinimumSize(scrollDim);
	sentPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
	sentPanel.add(sentMsgsScroll);
	
	receivedPanel = new JPanel();
	receivedTA = new JTextArea();
	receivedTA.setEnabled(false);
//	receivedTA.setFont(contentFont);
	JScrollPane receivedMsgsScroll = new JScrollPane(receivedTA);	
	receivedMsgsScroll.setPreferredSize(scrollDim);
	receivedMsgsScroll.setMinimumSize(scrollDim);
//	receivedTA.setPreferredSize(taDim);
	receivedPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
	receivedPanel.add(receivedMsgsScroll);
	
	tabbedPane=new JTabbedPane();
	tabbedPane.setPreferredSize(new Dimension(900/2, 900/2));
	tabbedPane.add("Messages", convPanel);
	tabbedPane.setFont(contentFont);
	tabbedPane.add("Sent", sentPanel);
	tabbedPane.add("Received", receivedPanel);
	
	msgPanel.setBorder(BorderFactory.createEmptyBorder(50/2, 10/2, 10/2, 10/2));
	tabbedPane.setBorder(BorderFactory.createEmptyBorder(100/2, 10/2, 10/2, 10/2));
	
	
	JPanel upperPanel = new JPanel();
	BoxLayout upperBoxlayout = new BoxLayout(upperPanel, BoxLayout.Y_AXIS);
	upperPanel.setLayout(upperBoxlayout);
	
	
	upperPanel.add(settingsPanel);
	upperPanel.add(msgPanel);

	
	JPanel aggrPanel = new JPanel();
	BoxLayout aggrBoxlayout = new BoxLayout(aggrPanel, BoxLayout.Y_AXIS);
	aggrPanel.setLayout(aggrBoxlayout);
	
	aggrPanel.add(upperPanel);
	aggrPanel.add(tabbedPane);

	TitledBorder border = BorderFactory.createTitledBorder("Chat between agents");
	border.setTitleFont(new Font("Sans", Font.BOLD, 42/2));
	border.setTitleJustification(TitledBorder.CENTER);
	aggrPanel.setBorder(border);
	
	getContentPane().add(aggrPanel, BorderLayout.NORTH);
	
    pack();
    
    setResizable(true);
    
    
	}
	private ActionListener onSendBClicked = new ActionListener(){
	  	public void actionPerformed(ActionEvent e) {
	  		try {
		  		String rcvName 		= receiverCB.getSelectedItem().toString();
		  		String mp 			= msgPerformativeCB.getSelectedItem().toString();
		  		String platformName = platformTF.getText();
		  		String rcvAddress	= addressTF.getText();
		  		String msgContent	= msgContentTF.getText();
		  		if (msgContent != null && 
		  				msgContent.length() > 0 && 
		  				!msgContent.equals("Write a message...")) {
		  			if (platformName != null && platformName.length() > 0) {
		  				if (rcvAddress != null && rcvAddress.length() > 0) {
		  					String rcvFullName = rcvName + "@" + platformName;
		  					myAgent.send(msgContent, rcvFullName, rcvAddress, mp);
		  					msgContentTF.setText("Write a message...");
		  				} else {
			  				// No address specified
				  			JOptionPane.showMessageDialog(MsgGuiImpl.this, "Please, provide address", "WARNING", JOptionPane.WARNING_MESSAGE);
			  			}
		  			} else {
		  				// No platform name specified
			  			JOptionPane.showMessageDialog(MsgGuiImpl.this, "Please, provide platform name", "WARNING", JOptionPane.WARNING_MESSAGE);
		  			}
		  		} else {
	  				// No content specified
		  			JOptionPane.showMessageDialog(MsgGuiImpl.this, "Please, write a message", "WARNING", JOptionPane.WARNING_MESSAGE);
	  			}
	  		} catch (Exception ex) {
	  			JOptionPane.showMessageDialog(MsgGuiImpl.this, "Invalid parameters. " + ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
	  		}
	  	}
  	};
	
  	public void showReceivedMsg (ACLMessage msgACL) {
		if(msgACL != null) {
			String mp = msgACL.getPerformative(msgACL.getPerformative());
			String msgContent = msgACL.getContent();
			String SenderName = msgACL.getSender().getLocalName();
			Date date = new Date();
			String currDate = date.toString();
			String toDisplay = "";
			toDisplay = toDisplay + SenderName + ":\n" 
					+ msgContent + "\n" 
					+ currDate 
					+ "\nMessage type: " + mp.toLowerCase()
					+ "\n\n";
			conversationTA.append(toDisplay);
			receivedTA.append(toDisplay);
		}
	}
  	
  	public void showSentMsg (ACLMessage msgACL) {
		if(msgACL != null) {
			String mp = msgACL.getPerformative(msgACL.getPerformative());
			String msgContent = msgACL.getContent();
			String SenderName = "You";
//					myAgent.getLocalName();
			Date date = new Date();
			String currDate = date.toString();
			String toDisplay = "";
			toDisplay = toDisplay + SenderName + ":\n" 
					+ msgContent + "\n" 
					+ currDate 
					+ "\nMessage type: " + mp.toLowerCase()
					+ "\n\n";
			conversationTA.append(toDisplay);
			sentTA.append(toDisplay);
		}
	}
  	
  	
	private MouseListener cleanTF = new MouseListener() {
		@Override
		public void mousePressed(MouseEvent e) {
			msgContentTF.setText("");			
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			msgContentTF.setText("");				
		} 
    };
	
	public void updateReceiverNames () {
		if (receiverCB.getItemAt(0) != null) {
			receiverCB.removeAllItems();
		} 
//		myAgent.updateReceiverAgents();
		
		System.out.println("In updateReceiverNames");
		
		String myName = myAgent.getLocalName();
		
		for (String agentName : myAgent.receiverAgents) {
			if (!(agentName.equals(myName) || receiverNames.contains(agentName))) {
				receiverCB.addItem(agentName);
			}
		}
	}
}