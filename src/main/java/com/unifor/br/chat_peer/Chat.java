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

/**
 * Sistema de chat P2P (peer-to-peer) descentralizado.
 * Permite comunicação entre múltiplos usuários sem servidor central.
 */
public class Chat {

    private String userName;
    private ServerSocket serverSocket;
    private List<Socket> connections = new ArrayList<>();
    private CommandHandler cmdHandler;
    private UIHelper uiHelper;
    private MessageLogger logger;
    private volatile boolean running = true;

    /**
     * Construtor do chat P2P.
     *
     * @param userName Nome do usuário desta sessão
     * @param port Porta para escutar conexões de entrada
     * @throws RuntimeException se não conseguir criar o servidor
     */
    public Chat(String userName, int port){
        this.userName = userName;
        this.uiHelper = new UIHelper();
        this.cmdHandler = new CommandHandler(this);

        try {
            this.serverSocket = new ServerSocket(port);
            this.logger = new MessageLogger(userName, port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retorna a porta em que o servidor está escutando.
     *
     * @return Porta do servidor
     */
    public int getPort() {
        return serverSocket.getLocalPort();
    }

    /**
     * Inicia o chat, exibindo painel inicial e iniciando threads de comunicação.
     */
    public void start(){
        uiHelper.showWelcomePanel(userName, getPort());

        new Thread(this::listenForConnections, "ConnectionListener").start();
        new Thread(this::listenForUserInput, "UserInputListener").start();
    }

    /**
     * Thread que escuta entrada do usuário (mensagens e comandos).
     */
    private void listenForUserInput() {
        try {
            BufferedReader userInput = new BufferedReader(
                    new InputStreamReader(System.in));

            while (running){
                String mensagem = userInput.readLine();
                if (mensagem == null) break;

                clearEchoLine();

                if (mensagem.startsWith("/")) {
                    processCommand(mensagem);
                } else {
                    sendMessage(mensagem);
                }
            }
        } catch (IOException e) {
            if (running) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Remove eco da linha digitada usando códigos ANSI.
     */
    private void clearEchoLine() {
        System.out.print("\033[1A\033[2K\r");
        System.out.flush();
    }

    /**
     * Processa comando digitado pelo usuário.
     *
     * @param command Comando a processar (ex: "/help")
     */
    private void processCommand(String command) {
        String feedback = cmdHandler.handleCommand(command);
        if (feedback != null) {
            System.out.println(uiHelper.formatSystemMessage(feedback));
            logger.logMessage("SISTEMA", feedback);
        }
    }

    /**
     * Envia mensagem para todos os peers conectados.
     *
     * @param mensagem Conteúdo da mensagem
     */
    private void sendMessage(String mensagem) {
        System.out.println(uiHelper.formatLocalMessage(mensagem));
        logger.logMessage(userName, mensagem);
        broadcastMessage(mensagem);
    }

    /**
     * Transmite mensagem para todos os peers conectados.
     *
     * @param mensagem Mensagem a enviar
     */
    private void broadcastMessage(String mensagem) {
        for (Socket socket: connections){
            try {
                PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
                out.println(userName +" :" +mensagem);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Thread que escuta novas conexões de entrada.
     */
    private void listenForConnections() {
        while (running){
            try {
                Socket socket = serverSocket.accept();
                connections.add(socket);
                new Thread(() -> handleConnection(socket), "PeerHandler").start();
            } catch (IOException e) {
                if (running) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Gerencia comunicação com um peer específico.
     *
     * @param socket Conexão com o peer
     */
    private void handleConnection(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            String mensagem;

            while (running && (mensagem = in.readLine())!=null){
                String[] parts = mensagem.split(" :", 2);

                if (parts.length == 2) {
                    String peerName = parts[0];
                    String content = parts[1];
                    System.out.println(uiHelper.formatRemoteMessage(peerName, content));
                    logger.logMessage(peerName, content);
                } else {
                    System.out.println(mensagem);
                    logger.logMessage("DESCONHECIDO", mensagem);
                }
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("[ERRO] Conexão perdida: " + e.getMessage());
            }
        }
    }

    /**
     * Conecta a outro peer da rede.
     *
     * @param host Endereço IP ou hostname do peer
     * @param port Porta do peer
     */
    public void connectionToPeer(String host, int port){
        System.out.println(uiHelper.formatSystemMessage(
            String.format("[SISTEMA] Conectando a %s:%d...", host, port)));

        try {
            Socket socket = new Socket(host,port);
            connections.add(socket);
            new Thread(() -> handleConnection(socket), "PeerHandler").start();

            uiHelper.showConnectionMessage(host, port);
        } catch (IOException e) {
            uiHelper.showConnectionError(e.getMessage());
        }
    }

    /**
     * Encerra o chat de forma segura.
     * Fecha todas as conexões, salva logs e termina o programa.
     */
    public void shutdown() {
        System.out.println(uiHelper.formatSystemMessage("\n[SISTEMA] Encerrando chat..."));

        running = false;

        try {
            broadcastMessage("*** Saiu do chat ***");
        } catch (Exception e) {
            // Ignora erros
        }

        for (Socket socket : connections) {
            try {
                socket.close();
            } catch (IOException e) {
                // Ignora erros
            }
        }

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            // Ignora erros
        }

        if (logger != null) {
            logger.close();
        }

        System.out.println(uiHelper.formatSystemMessage("[SISTEMA] Encerrado com sucesso. Até logo!"));
        System.exit(0);
    }

    /**
     * Ponto de entrada do programa.
     *
     * @param args Argumentos da linha de comando (não utilizados)
     */
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