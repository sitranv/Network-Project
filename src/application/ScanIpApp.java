package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class ScanIpApp extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("/application/HomePage.fxml"));
			primaryStage.setTitle("My Scanip Application");
			Image logo  = new Image("file:resources/img/my_scanip_logo.png");
			primaryStage.getIcons().add(logo);
			primaryStage.setScene(new Scene(root));
			primaryStage.show();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
