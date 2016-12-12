package ru.coffeeplantator.netchat.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLHandler {
	
	private static Connection c;
    private static PreparedStatement ps;

    public static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:usersdb.db");
        } catch (Exception e) {
        	System.err.println("Ошибка соединения с базой данных.");
            e.printStackTrace();
        }
    }

    public static void disconnect() {
        try {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        } catch (SQLException e) {
        	System.err.println("Ошибка разъединения с базой данных.");
            e.printStackTrace();
        }
    }

    public static String getNickByLoginAndPassword(String login, String password) {
        ResultSet rs;
        String str = null;
        try {
            ps = c.prepareStatement("SELECT Nickname FROM Users WHERE Login = ? AND Password = ?");
            ps.setString(1, login);
            ps.setString(2, password);
            rs = ps.executeQuery();
            while (rs.next()) {
                str = rs.getString(1);
            }
        } catch (SQLException e) {
        	System.err.println("Ошибка получения ника из базы данных.");
            e.printStackTrace();
        }
        return str;
    }

    public static void addEntry(String login, String pass, String nick) {
        try {
            ps = c.prepareStatement("INSERT INTO Users (Login, Password, Nickname) VALUES (?, ?, ?);");
            ps.setString(1, login);
            ps.setString(2, pass);
            ps.setString(3, nick);
            ps.execute();
        } catch (SQLException e) {
        	System.err.println("Ошибка добавления записи в базу данных.");
            e.printStackTrace();
        }
    }
    
    public static void changeNick(String login, String newnick) {
        try {
            ps = c.prepareStatement("UPDATE Users SET Nickname = ? WHERE Login = ?");
            ps.setString(1, newnick);
            ps.setString(2, login);
            ps.execute();
        } catch (SQLException e) {
        	System.err.println("Ошибка изменения записи в базе данных.");
            e.printStackTrace();
        }
    }

}
