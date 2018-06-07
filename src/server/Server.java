/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.awt.Color;
import java.awt.geom.Area;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pedro Vivaldi
 */
public class Server extends Thread {

    private List<ServerClient> clients;
    private ServerSocket server;
    private Ball food;

    public Server(int port) {
        clients = new CopyOnWriteArrayList<>();
        food = new Ball(100, 100, 5);
        food.setColor(Color.RED);
        food.setName("");
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
            ball = new Ball((int) Math.round(Math.random() * 200) + 1, (int) Math.round(Math.random() * 200) + 1, 10);
            //receptor = new Receptor();
            //receptor.start();
        }

        private void checkCollisions() {
            Area ballArea = new Area(ball.toEllipse2D());
            ballArea.intersect(new Area(food.toEllipse2D()));
            if (!ballArea.isEmpty()) {
                ball.setRadius(ball.getRadius() + 10);
                food.setX((int) Math.round(Math.random() * 200) + 1);
                food.setY((int) Math.round(Math.random() * 200) + 1);
            }
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
                        case LOGOFF:
                            removeClient();
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
            checkCollisions();
            checkCollisionsBetweenPlayers();

            updateAllClients();
        }

        private void checkCollisionsBetweenPlayers() {
            Area ballArea = new Area(ball.toEllipse2D());

            for (ServerClient client : clients) {
                if (!client.equals(this)) {
                    Area ball2Area = new Area(client.ball.toEllipse2D());
                    if (ball.getRadius() > client.ball.getRadius()) {
                        ballArea.intersect(ball2Area);
                        if (ballArea.contains(client.ball.toEllipse2D().getCenterX(), client.ball.toEllipse2D().getCenterY())) {
                            ball.setRadius(ball.getRadius() + client.ball.getRadius());

                            Message loseMessage = new Message(null, null, MessageType.LOSE);
                            try {
                                client.saida.writeObject(loseMessage);
                                client.saida.reset();
                            } catch (IOException ex) {
                                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            client.removeClient();
                        }
                    } else if (ball.getRadius() < client.ball.getRadius()) {
                        ball2Area.intersect(ballArea);
                        if (ball2Area.contains(ball.toEllipse2D().getCenterX(), ball.toEllipse2D().getCenterY())) {
                            client.ball.setRadius(ball.getRadius() + client.ball.getRadius());

                            Message loseMessage = new Message(null, null, MessageType.LOSE);
                            try {
                                saida.writeObject(loseMessage);
                                saida.reset();
                            } catch (IOException ex) {
                                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            removeClient();
                        }
                    }
                }
            }
        }

        private void removeClient() {
            this.interrupt();
            clients.remove(this);

            updateAllClients();
        }

        private void updateAllClients() {
            List<Ball> balls = new ArrayList<>();
            for (ServerClient client : clients) {
                balls.add(client.ball);
            }
            balls.add(food);

            Message returnMessage = new Message(balls, null, MessageType.CHANGED);

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

                /*List<Ball> balls = new ArrayList<>();
                for (ServerClient client : clients) {
                    balls.add(client.ball);
                }
                balls.add(food);*/
                updateAllClients();
                //returnMessage = new Message("O usuário " + name + " realizou o login", balls, MessageType.LOGIN_CONFIRMED);
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
