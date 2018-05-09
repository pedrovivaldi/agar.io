/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pedro Vivaldi
 */
public class Server extends Thread {

    private List<ServerClient> clients;
    private ServerSocket server;

    public Server(int port) {
        clients = new ArrayList<>();
        try {
            server = new ServerSocket(port);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        for (;;) {
            Socket s;
            ServerClient novoCliente = null;
            try {
                System.out.println("Esperando conexão");
                s = server.accept();
                novoCliente = new ServerClient(s);
                clients.add(novoCliente);

                System.out.println("Novo cliente conectado. " + clients.size() + "clientes conectados.");
                clients.get(clients.size() - 1).start();
            } catch (Exception ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public class ServerClient extends Thread {

        private final ObjectOutputStream saida;
        private final ObjectInputStream entrada;
        private final Ball ball;
        //private final Receptor receptor;

        public ServerClient(Socket s) throws Exception {
            System.out.println("Cliente conectado");
            entrada = new ObjectInputStream(s.getInputStream());
            saida = new ObjectOutputStream(s.getOutputStream());
            ball = new Ball(50, 50, 10);
            //receptor = new Receptor();
            //receptor.start();
        }

        private void send(Message msg) {
            try {
                saida.writeObject(msg);
                saida.reset();
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        //private class Receptor extends Thread {
        @Override
        public void run() {
            Message newMessage;
            for (;;) {
                try {
                    newMessage = (Message) entrada.readObject();
                    switch (newMessage.getType()) {
                        case LOGIN_REQUEST:
                            send(login(newMessage));
                            break;
                        case MOVE_REQUEST:
                            move(newMessage);
                            break;
                    }
                } catch (IOException | ClassNotFoundException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        //}

        private void move(Message message) {
            int dx = (Integer) message.getContent();
            int dy = (Integer) message.getContent2();
            this.ball.move(dx, dy);

            updateAllClients();
        }

        private void updateAllClients() {
            List<Ball> balls = new ArrayList<>();
            for (ServerClient client : clients) {
                balls.add(client.ball);
            }

            Message returnMessage = new Message(balls, null, MessageType.MOVED);

            for (ServerClient client : clients) {
                client.send(returnMessage);
            }
        }

        private Message login(Message message) {
            String name = (String) message.getContent();
            Color color = (Color) message.getContent2();
            Message returnMessage = null;

            if (isUniqueName(name)) {
                this.ball.setName(name);
                if (color != null) {
                    this.ball.setColor(color);
                } else {
                    this.ball.setColor(Color.BLACK);
                }
                System.out.println("O usuário " + name + " realizou o login");
                returnMessage = new Message("O usuário " + name + " realizou o login", null, MessageType.LOGIN_CONFIRMED);
            } else {
                returnMessage = new Message("Já existe um login online", null, MessageType.ERROR);
            }

            return returnMessage;
        }

        private boolean isUniqueName(String name) {
            for (ServerClient client : clients) {
                if (client.ball != null && client.ball.getName() != null) {
                    if (client.ball.getName().equals(name)) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

}
