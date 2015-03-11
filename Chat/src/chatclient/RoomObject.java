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
    private String text = "";
    private DefaultListModel usersModel = new DefaultListModel();


    public RoomObject(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public DefaultListModel getUsersModel() {
        return usersModel;
    }

    public void setUsersModel(DefaultListModel usersModel) {
        this.usersModel = usersModel;
    }

    
}
