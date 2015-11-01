package Models;

import java.io.Serializable;

public class ClientUser implements Serializable{

    private String userName;



    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public ClientUser(String userName) {
        super();
        this.userName = userName;
    }

}
