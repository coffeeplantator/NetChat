package ru.coffeeplantator.netchat.client;
	
import javafx.application.Application;
import javafx.stage.Stage;

public class Client extends Application {
	
	@Override
	public void start(Stage primaryStage) {
		new ClientWindow(primaryStage);
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
}
