package com.unifor.br.chat_peer.Helpers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Logger que persiste mensagens do chat em arquivo.
 * Cada sessão gera um arquivo com timestamp único.
 */
public class MessageLogger {

    private static final DateTimeFormatter FILE_TIMESTAMP =
        DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private static final DateTimeFormatter MESSAGE_TIMESTAMP =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String sessionFile;
    private BufferedWriter writer;
    private boolean closed = false;

    /**
     * Cria um novo logger de sessão.
     * @param userName Nome do usuário desta sessão
     * @param port Porta de escuta
     * @throws IOException Se não conseguir criar o arquivo
     */
    public MessageLogger(String userName, int port) throws IOException {
        // Criar pasta se não existir
        File dir = new File("chat-sessions");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Nome do arquivo com timestamp do início da sessão
        String timestamp = LocalDateTime.now().format(FILE_TIMESTAMP);
        this.sessionFile = "chat-sessions/session_" + timestamp + ".log";

        // Abrir arquivo para escrita (append mode)
        this.writer = new BufferedWriter(new FileWriter(sessionFile, true));

        // Log de início de sessão
        logSessionStart(userName, port);
    }

    /**
     * Registra o início da sessão
     */
    private void logSessionStart(String userName, int port) throws IOException {
        writer.write("=".repeat(60));
        writer.newLine();
        writer.write("NOVA SESSÃO INICIADA");
        writer.newLine();
        writer.write("Usuário: " + userName);
        writer.newLine();
        writer.write("Porta: " + port);
        writer.newLine();
        writer.write("Início: " + LocalDateTime.now().format(MESSAGE_TIMESTAMP));
        writer.newLine();
        writer.write("=".repeat(60));
        writer.newLine();
        writer.newLine();
        writer.flush();
    }

    /**
     * Registra uma mensagem no arquivo.
     * @param sender Nome do remetente (pode ser "VOCÊ", "Alice", "SISTEMA")
     * @param content Conteúdo da mensagem
     */
    public void logMessage(String sender, String content) {
        try {
            String timestamp = LocalDateTime.now().format(MESSAGE_TIMESTAMP);
            String line = String.format("[%s] %s: %s", timestamp, sender, content);
            writer.write(line);
            writer.newLine();
            writer.flush(); // Garante que escreve imediatamente no disco
        } catch (IOException e) {
            System.err.println("[ERRO] Falha ao salvar mensagem no log: " + e.getMessage());
        }
    }

    /**
     * Registra o fim da sessão e fecha o arquivo.
     */
    public void close() {
        try {
            if (writer != null) {
                writer.newLine();
                writer.write("=".repeat(60));
                writer.newLine();
                writer.write("SESSÃO ENCERRADA: " + LocalDateTime.now().format(MESSAGE_TIMESTAMP));
                writer.newLine();
                writer.write("=".repeat(60));
                writer.newLine();
                writer.close();
            }
        } catch (IOException e) {
            System.err.println("[ERRO] Falha ao fechar logger: " + e.getMessage());
        }
    }

    /**
     * Retorna o caminho do arquivo de log desta sessão.
     */
    public String getSessionFile() {
        return sessionFile;
    }
}