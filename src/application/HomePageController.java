package application;

//import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
//import javafx.event.ActionEvent;

public class HomePageController implements Initializable {
	@FXML
	private Button click;
	
	@FXML
	private AnchorPane rootPane;
	
	@FXML
	public void openApp(javafx.event.ActionEvent event) throws IOException {
		Parent app_parent = FXMLLoader.load(getClass().getResource("/application/MyScanip.fxml"));
		Scene app_page_scene = new Scene(app_parent);
		Stage app_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		app_stage.hide();
		app_stage.setScene(app_page_scene);
		app_stage.show();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		
	}
	
	
}
