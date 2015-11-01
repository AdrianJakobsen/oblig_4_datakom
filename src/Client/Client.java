package Client;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Client extends Application{
    Stage primaryStage;


    public Client(){

    }

    public void start(Stage primaryStage) throws Exception {

        this.primaryStage = primaryStage;
        initMainView();
    }

    public void initMainView() {
        try {

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("ClientMainView.fxml"));
            Pane rootLayout = loader.load();
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        launch(args);
    }
}