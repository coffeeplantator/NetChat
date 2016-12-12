package ru.coffeeplantator.netchat.server;

import javax.swing.SwingUtilities;

public class Server {
    public static void main(String[] args) {
    	SQLHandler.connect();
    	SwingUtilities.invokeLater(() -> new ServerWindow() );
		new ServerApplication();
        SQLHandler.disconnect();
    }
}
