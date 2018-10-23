/**
 * Section 4.1.5, Page 57
 *
 * definition of the BookBuyerGui interface 
 **/

package Messenger;

import jade.lang.acl.ACLMessage;

public interface MsgGui {
  void show();
  void hide();
  void dispose();
  void showReceivedMsg(ACLMessage msgACL);
  void showSentMsg (ACLMessage msgACL);
}