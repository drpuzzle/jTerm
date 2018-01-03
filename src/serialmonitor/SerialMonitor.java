/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serialmonitor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author drpuzzle
 */
public class SerialMonitor extends Application {
  String Version = "0.11";
  
  
  @Override
  public void start(Stage stage) throws Exception {
    Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
    
    Scene scene = new Scene(root);
    stage.setTitle("JTerm "+Version);
    scene.getStylesheets().add(SerialMonitor.class.getResource("/stylesheet.css").toExternalForm());
    stage.getIcons().add(new Image(getClass()
            .getResourceAsStream("/puzzle_small.png")
    ));
    stage.setScene(scene);
    stage.show();
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    launch(args);
  }
  
  @Override
public void stop(){
    Settings.save();
}
  
}
