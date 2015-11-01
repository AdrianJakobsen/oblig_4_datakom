package Models;

import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class ClientUserContainer{

    private OutputStream output;
    private ObjectOutputStream outputToClient;
    private ClientUser clientUser;

    public ClientUserContainer(OutputStream output, ObjectOutputStream outputToClient, ClientUser clientUser) {
        super();
        this.output = output;
        this.outputToClient = outputToClient;
        this.clientUser=clientUser;
    }

    public OutputStream getOutput() {
        return output;
    }
    public void setOutput(OutputStream output) {
        this.output = output;
    }
    public ObjectOutputStream getoutputToClient() {
        return outputToClient;
    }
    public void setoutputToClient(ObjectOutputStream outputToClient) {
        this.outputToClient = outputToClient;
    }
    public ClientUser getClientUser() {
        return clientUser;
    }

    public void setClientUser(ClientUser clientUser) {
        this.clientUser = clientUser;
    }
}
