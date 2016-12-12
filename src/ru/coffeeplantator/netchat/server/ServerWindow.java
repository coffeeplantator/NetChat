package ru.coffeeplantator.netchat.server;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

@SuppressWarnings("serial")
public class ServerWindow extends JFrame {
	
	public ServerWindow() {
		setBounds(500, 500, 200, 200);
    	setTitle("Сервер чата");
    	setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    	setLocationRelativeTo(null);
    	setResizable(false);
    	setLayout(new BorderLayout());
    	JLabel serverRunning = new JLabel("<html><font color='red'>Сервер запущен</font></html>", SwingConstants.CENTER);
    	serverRunning.setFont(new Font(serverRunning.getFont().getFontName(), Font.PLAIN, (int) (serverRunning.getFont().getSize() * 0.7)));
    	add(new JLabel("Ожидание клиентов...",  SwingConstants.CENTER), BorderLayout.CENTER);
    	add(serverRunning, BorderLayout.SOUTH);
    	setVisible(true);
	}
	
}
