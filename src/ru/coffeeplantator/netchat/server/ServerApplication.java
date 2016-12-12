package ru.coffeeplantator.netchat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ServerApplication {
	
	private ArrayList<ClientHandler> clients;

    public ServerApplication() {
        ServerSocket server = null;
        SQLHandler.connect();
        clients = new ArrayList<>();
        try {
            server = new ServerSocket(60000);
            while (true) {
                System.out.println("Waiting for clients...");
                Socket s = server.accept();
                System.out.println("Client connected");
                ClientHandler h = new ClientHandler(s, this);
                new Thread(h).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                server.close();
                SQLHandler.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcastMsg(String msg) {
    	String str = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
        msg = str + " " + msg;
        for (ClientHandler o : clients) {
            o.sendMsg(msg);
        }
    }

    public synchronized void removeFromClients(ClientHandler o) {
        clients.remove(o);
        if (!o.getName().isEmpty())
            broadcastMsg(o.getName() + " disconnected from chatroom");
    }

    public synchronized void addClient(ClientHandler o) {
        if (!clients.contains(o)) {
            clients.add(o);
            broadcastMsg(o.getName() + " connected to chatroom");
        }
    }
    
    public boolean isNickBusy(String nick) {
    	for (ClientHandler ch:
            clients) {
           if(ch.getName().equals(nick)){
               return true;
           }
       }
       return false;
    }
    
    public synchronized void sendPrivateMessage(String nick, String msg, String from){
        for (ClientHandler ch : clients) {
            if(ch.getName().equals(nick)) {
            	if (nick.equals(from)) {
            		ch.sendMsg("/changenick Ваш ник изменён, новый ник: " + nick);
            	}
            	else {
            		ch.sendMsg("/pm private message from " + from + " " + msg);
            	}
            }
        }
    }
    
    public synchronized void updateClientsList(){
        StringBuilder sb = new StringBuilder("/updateclients ");
        for (ClientHandler ch:
             clients) {
            sb.append(ch.getName());
            sb.append(",");
        }
        String updateCommand = sb.toString();
        for (ClientHandler ch:
             clients) {
            ch.sendMsg(updateCommand);
        }

    }

}
