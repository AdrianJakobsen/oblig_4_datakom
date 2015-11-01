package Models;

import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable{
    private ClientUser clientUser;
    private Date messageSentAt;
    private String message;


    public Message(ClientUser clientUser, Date messageSentAt, String message) {
        super();
        this.clientUser = clientUser;
        this.messageSentAt = messageSentAt;
        this.message = message;
    }

    public ClientUser getClientUser() {
        return clientUser;
    }
    public void setClientUser(ClientUser clientUser) {
        this.clientUser = clientUser;
    }
    public Date getMessageSentAt() {
        return messageSentAt;
    }
    public void setMessageSentAt(Date messageSentAt) {
        this.messageSentAt = messageSentAt;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}
