/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatclient;

import java.awt.*;
import javax.swing.*;

/**
 *
 * @author Aantokhin
 */
public class RoomEntry extends JFrame{
    JTextField roomName;
    JButton joinButton;
    JPanel panel;
    
    public RoomEntry() {
        super("Join new room");
        
        roomName = new JTextField(15);
        joinButton = new JButton("Join");
        joinButton.addActionListener(null);
        
        panel = new JPanel();
        panel.add(roomName);
        panel.add(joinButton);
        
        this.getContentPane().add(BorderLayout.CENTER, panel);
        this.setSize(300,100);
        this.setVisible(true);
    }
    
}
