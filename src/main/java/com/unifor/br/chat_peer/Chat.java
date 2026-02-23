package com.unifor.br.chat_peer;

import com.unifor.br.chat_peer.Helpers.CommandHandler;
import com.unifor.br.chat_peer.Helpers.MessageLogger;
import com.unifor.br.chat_peer.Helpers.UIHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Chat {

    private String userName;
    private ServerSocket serverSocket;
    private List<Socket> connections = new ArrayList<>();
    private CommandHandler cmdHandler;
    private UIHelper uiHelper;
    private MessageLogger logger;
    private volatile boolean running = true; // Flag para controlar threads

    public Chat(String userName, int port){
        this.userName = userName;
        this.uiHelper = new UIHelper();
        this.cmdHandler = new CommandHandler(this); // Passa a referência do Chat

        try {
            this.serverSocket = new ServerSocket(port);
            this.logger = new MessageLogger(userName, port);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

    public void start(){
        // Mostrar painel inicial
        uiHelper.showWelcomePanel(userName, getPort());

        new  Thread(this::listenForConnections).start();
        new Thread(this::listenForUserinput).start();
    }

    private void listenForUserinput() {
        try {
            BufferedReader userInput = new BufferedReader(
                    new InputStreamReader(System.in));
            while (running){

                    String mensagem = userInput.readLine();
                    if (mensagem == null) break; // EOF ou stream fechado

                    // Limpar a linha anterior (remove o eco do que foi digitado)
                    System.out.print("\033[1A"); // Move cursor 1 linha para cima
                    System.out.print("\033[2K"); // Limpa a linha
                    System.out.print("\r");      // Move cursor para o início

                    if (mensagem.startsWith("/")) {
                        // Processa comando e exibe feedback
                        String feedback = cmdHandler.handleCommand(mensagem);
                        if (feedback != null) {
                            System.out.println(uiHelper.formatSystemMessage(feedback));
                            // Log mensagens de sistema
                            logger.logMessage("SISTEMA", feedback);
                        }
                    } else {
                        // Mostrar mensagem localmente (VOCÊ: ...)
                        System.out.println(uiHelper.formatLocalMessage(mensagem));
                        // Log da mensagem enviada (com nome real do usuário)
                        logger.logMessage(userName, mensagem);
                        // Enviar para peers
                        broadcastMessage(mensagem);
                    }
            }
        } catch (IOException e) {
            if (running) { // Só mostra erro se não for shutdown intencional
                throw new RuntimeException(e);
            }
        }
    }

    private void broadcastMessage(String mensagem) {
        for (Socket socket: connections){
            try {
                PrintWriter out =
                        new PrintWriter(socket.getOutputStream(),true);
                out.println(userName +" :" +mensagem);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void listenForConnections() {
        while (running){
            try {
                Socket socket = serverSocket.accept();
                connections.add(socket);
                new Thread(()-> handleConection(socket)).start();
            } catch (IOException e) {
                if (running) { // Só mostra erro se não for shutdown intencional
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void handleConection(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            String mensagem;
            while (running && (mensagem = in.readLine())!=null){
                // Parsear "Nome :mensagem" e formatar com cor
                String[] parts = mensagem.split(" :", 2);
                if (parts.length == 2) {
                    String peerName = parts[0];
                    String content = parts[1];
                    System.out.println(uiHelper.formatRemoteMessage(peerName, content));
                    // Log da mensagem recebida
                    logger.logMessage(peerName, content);
                } else {
                    // Fallback se formato inesperado
                    System.out.println(mensagem);
                    logger.logMessage("DESCONHECIDO", mensagem);
                }
            }
        } catch (IOException e) {
            if (running) { // Só mostra erro se não for shutdown intencional
                System.err.println("[ERRO] Conexão perdida: " + e.getMessage());
            }
        }
    }


    public void connectionToPeer(String host, int port){
        // Mostrar "Conectando..." ANTES de tentar
        System.out.println(uiHelper.formatSystemMessage(
            String.format("[SISTEMA] Conectando a %s:%d...", host, port)));

        try {
            Socket socket = new Socket(host,port);
            connections.add(socket);
            new Thread(() -> handleConection(socket)).start();

            // Mostrar "Conectado" DEPOIS de conectar
            uiHelper.showConnectionMessage(host, port);
        } catch (IOException e) {
            uiHelper.showConnectionError(e.getMessage());
        }
    }

    /**
     * Encerra o chat de forma segura.
     * Fecha todas as conexões, logger e termina o programa.
     */
    public void shutdown() {
        System.out.println(uiHelper.formatSystemMessage("\n[SISTEMA] Encerrando chat..."));

        // Para todas as threads
        running = false;

        // Envia mensagem de despedida para todos os peers
        try {
            broadcastMessage("*** Saiu do chat ***");
        } catch (Exception e) {
            // Ignora erros ao enviar despedida
        }

        // Fecha todas as conexões com peers
        for (Socket socket : connections) {
            try {
                socket.close();
            } catch (IOException e) {
                // Ignora erros ao fechar
            }
        }

        // Fecha o servidor
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            // Ignora erros ao fechar
        }

        // Fecha o logger (já tem log de encerramento no close())
        if (logger != null) {
            logger.close();
        }

        System.out.println(uiHelper.formatSystemMessage("[SISTEMA] Encerrado com sucesso. Até logo!"));

        // Termina o programa
        System.exit(0);
    }

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("Digite o nome do usuario: ");
        String userName = scanner.nextLine();

        System.out.println("Digite a porta para escutar: ");
        int port = scanner.nextInt();
        scanner.nextLine();

        Chat peer = new Chat(userName,port);
        peer.start();
    }
}
