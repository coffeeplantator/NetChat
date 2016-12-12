package ru.coffeeplantator.netchat.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ClientWindow {
	TextField tfLogin = new TextField();
	PasswordField pfPass = new PasswordField();
	Button btnLogin = new Button("Войти");
	TextArea taChat = new TextArea();
	TextArea taClients = new TextArea();
	TextField tfMessage = new TextField();
	Button btnSend = new Button("Отправить");
	Socket socket;
    DataInputStream in;
    DataOutputStream out;
    Thread threadRead;
    Boolean isAuthorized = false;
	
	public ClientWindow(Stage primaryStage) {
		makeGUI(primaryStage);
	}
	
	private void makeGUI(Stage primaryStage) {
		try {
			BorderPane root = new BorderPane();
			Scene scene = new Scene(root,500,400);
			primaryStage.centerOnScreen();
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.setMinWidth(300);
			primaryStage.setMinHeight(300);
			
			// Верхняя панель
			BorderPane topNode = new BorderPane();
			GridPane topLeftNode = new GridPane();
			ColumnConstraints column1 = new ColumnConstraints();
			column1.setPercentWidth(50);
			ColumnConstraints column2 = new ColumnConstraints();
			column2.setPercentWidth(50);
			topLeftNode.getColumnConstraints().addAll(column1, column2);
			topLeftNode.add(tfLogin, 0, 0);
			tfLogin.setPromptText("Логин");
			topLeftNode.add(pfPass, 1, 0);
			pfPass.setPromptText("Пароль");
			pfPass.setOnAction((ae) -> {
				if (!isAuthorized) {
					connect();
				}
				else {
					disconnect();
				}
			});
			topNode.setCenter(topLeftNode);
			topNode.setRight(btnLogin);
			btnLogin.setOnAction((ae) -> {
				if (!isAuthorized) {
					connect();
				}
				else {
					disconnect();
				}
			});
			root.setTop(topNode);
			
			// Чат
			taChat.setStyle("-fx-text-fill: white; -fx-border-color: black; -fx-border-width: 0px;");
			taChat.setEditable(false);
			taChat.setWrapText(true);
			root.setCenter(taChat);
//			taClients.setStyle("-fx-text-fill: white; -fx-border-color: black; -fx-border-width: 0px;");
//			taClients.setEditable(false);
//			taClients.setWrapText(true);
//			taClients.setPrefWidth(150);
//			root.setRight(taClients);
			
			// Нижняя панель
			BorderPane bottomNode = new BorderPane();
			bottomNode.setCenter(tfMessage);
			tfMessage.setOnAction((ae) -> sendMessage());
			tfMessage.setPromptText("Введите своё сообщение");
			bottomNode.setRight(btnSend);
			btnSend.setOnAction((ae) -> sendMessage());
			root.setBottom(bottomNode);
			
			primaryStage.show();
			primaryStage.setOnCloseRequest((we) -> {
				try {
					if (socket != null) {
						socket.close();
					}
				} catch (IOException e) {
					System.err.println("Ошибка закрытия соединения.");
					e.printStackTrace();
				}
			});
		} catch(Exception e) {
			System.err.println("Ошибка создания интерфейса.");
			e.printStackTrace();
		}
	}
	
	private void disconnect() {
		try {
			if (socket != null) {
				socket.close();
			}
			if (threadRead != null) {
				threadRead.interrupt();
				threadRead = null;
			}
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
			isAuthorized = false;
        	tfLogin.setDisable(false);
        	pfPass.setDisable(false);
        	btnLogin.setText("Войти");
        	tfLogin.requestFocus();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void connect() {
		try {
			if ((socket == null) || (socket != null && socket.isClosed())) {
				socket = new Socket("localhost", 60000);
				in = new DataInputStream(socket.getInputStream());
				out = new DataOutputStream(socket.getOutputStream());

				out.writeUTF("/auth " + tfLogin.getText() + " " + pfPass.getText());

				if (threadRead == null) {
					threadRead = new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								String str = null;
								while (true) {
									if ((socket != null) || (socket.isClosed())) {
										try {
											str = in.readUTF();
										} catch (Exception e) {
											socket.close();
										}
										if (!isAuthorized) {
											if (str.equals("/authorized")) {
												setAuthorized(true);
												tfLogin.setDisable(true);
												pfPass.setDisable(true);
												Platform.runLater(() -> btnLogin.setText("Выйти"));
												continue;
											}
											taChat.appendText(str + '\n');
											tfLogin.setText("");
											pfPass.setText("");
											taChat.selectPositionCaret(taChat.getLength());
										}
										if (str != null && isAuthorized) {
											if (str.startsWith("/")) {
												if (str.startsWith("/updateclients")) {
													String w = str.substring("/updateclients ".length());
													String[] x = w.split(",");
													taClients.setText("");
													for (String s : x) {
														taClients.appendText(s);
														taClients.appendText("\n");
													}
												}
												if (str.startsWith("/pm")) {
													str = str.substring("/pm ".length());
													taChat.appendText(str + '\n');
												}
												if (str.startsWith("/changenick")) {
													str = str.substring("/changenick ".length());
													taChat.appendText(str + '\n');
												}
												continue;
											}
											taChat.appendText(str + '\n');
											taChat.selectPositionCaret(taChat.getLength());
											Platform.runLater(() -> tfMessage.requestFocus());
										}
										try {
											Thread.sleep(100);
										} catch (InterruptedException e) {
											Thread.currentThread().interrupt();
											e.printStackTrace();
										}
										str = null;
									}
								}

							} catch (IOException e) {
								try {
									socket.close();
								} catch (IOException e1) {
									System.err.println("Ошибка ввода-вывода.");
									e1.printStackTrace();
								}
								taChat.appendText("Connection lost ...\n");
								tfLogin.setText("");
								pfPass.setText("");
								threadRead = null;
								setAuthorized(false);
								System.err.println("Ошибка ввода-вывода.");
								e.printStackTrace();
							}
						}
					});
					threadRead.start();
				}
			}
		} catch (IOException e) {
			System.err.println("Ошибка ввода-вывода.");
			e.printStackTrace();
		}
	}

	private void sendMessage() {
		try {
            out.writeUTF(tfMessage.getText());
            out.flush();
            tfMessage.setText("");
            tfMessage.requestFocus();
        } catch (IOException e) {
            System.out.println("IO Error");
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            taChat.appendText("Connection lost ...\n");
            tfLogin.setText("");
            pfPass.setText("");
            setAuthorized(false);
            threadRead = null;
            System.out.println("IO Error");
        }
	}
	
	public void setAuthorized(boolean authorized) {
        isAuthorized = authorized;
    }

	
}
