/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatframework;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author Aantokhin
 */
public class User {
    private String username;
    public ObjectOutputStream outputStream;
    public ObjectInputStream inputStream;
    public Socket socket;
    public ArrayList<String> rooms = new ArrayList<>();

    public User(String username, ObjectOutputStream outputStream, ObjectInputStream inputStream, Socket socket) {
        this.username = username;
        this.outputStream = outputStream;
        this.inputStream = inputStream;
        this.socket = socket;
        this.rooms.add("testroom");
    }
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
}
