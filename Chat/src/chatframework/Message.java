/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatframework;

/**
 *
 * @author Aantokhin
 */
public class Message implements java.io.Serializable{
    private String user;
    private String content;

    public Message() {
    }

    public Message( String message, String user) {
        this.user = user;
        this.content = message;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    
    
}
