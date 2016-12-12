package ru.coffeeplantator.netchat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {
	
	private Socket s;
    private DataInputStream in;
    private DataOutputStream out;
    private String name;
    private ServerApplication owner;
    private String login;

    public String getName() {
        return name;
    }

    public ClientHandler(Socket s, ServerApplication owner) {
        this.s = s;
        name = "";
        this.owner = owner;
        try {
            in = new DataInputStream(s.getInputStream());
            out = new DataOutputStream(s.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	@Override
	public void run() {
		try {

			while (true) {
				String str = in.readUTF();
				System.out.println("auth " + str);

				if (str != null && name.isEmpty()) {
					String[] x = str.split(" ");
					if (x.length == 3) {
						if (x[0].equals("/auth")) {
							String login2 = x[1];
							String pass = x[2];
							String nick = SQLHandler.getNickByLoginAndPassword(login2, pass);
							System.out.println("nick " + nick);
							if (nick != null) {
								if (!owner.isNickBusy(nick)) {
									name = nick;
									this.login = login2;
									sendMsg("/authorized");
									owner.addClient(this);
									break;
								} else {
									sendMsg("Login already in use\n");

								}

							} else {
								sendMsg("Wrong login/pass \n");
							}
						}
					}
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			while (true) {
				String str = in.readUTF();
				System.out.println("read " + str);
				if (str != null && !name.isEmpty()) {
					if (str.startsWith("/")) {
						if (str.startsWith("/pm")) {
							String[] w = str.split(" ");
							if (w.length > 1) {
								String receiver = w[1];
								String msg = str.substring(new String("/pm " + receiver).length());
								owner.sendPrivateMessage(receiver, msg, name);
								sendMsg("/pm you sent private message to " + receiver + " : " + msg);
							}
						}

						if (str.startsWith("/changenick")) {
							String[] w = str.split(" ");
							if (w.length > 1) {
								String newnick = w[1];
								name = newnick;
								String msg = str.substring(new String("/changenick " + newnick).length());
								SQLHandler.changeNick(login, newnick);
								owner.sendPrivateMessage(name, msg, name);
							}
						}
						continue;
					}
					if (str.equalsIgnoreCase("end"))
						break;
					owner.broadcastMsg(name + ": " + str);
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			System.out.println("IO ERROR");
		} finally {
			owner.removeFromClients(this);
			try {
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
