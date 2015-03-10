/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatclient;

import java.awt.Dimension;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;

/**
 *
 * @author Aantokhin
 */
public class RoomObject {

    private String name;
    public JTextArea incoming;
    public JList userList;
    public DefaultListModel usersModel = new DefaultListModel();
    public JScrollPane userScroller;
    public JScrollPane qScroller;

    public RoomObject(String name) {
        this.name = name;
                
        userList = new JList(usersModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        userList.setLayoutOrientation(JList.VERTICAL);
        userList.setVisibleRowCount(-1);

        userScroller = new JScrollPane(userList);
        userScroller.setPreferredSize(new Dimension(100, 500));

        incoming = new JTextArea(15, 10);
        incoming.setLineWrap(true);
        incoming.setEditable(false);

        qScroller = new JScrollPane(incoming);
        qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}
