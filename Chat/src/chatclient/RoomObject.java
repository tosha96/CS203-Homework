/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatclient;

import javax.swing.DefaultListModel;

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

    @Override
    public String toString() {
        return this.name;
    }
    
}