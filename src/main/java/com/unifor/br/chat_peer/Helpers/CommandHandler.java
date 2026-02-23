package com.unifor.br.chat_peer.Helpers;

import com.unifor.br.chat_peer.Chat;

/**
 * Processa comandos do usuário e retorna feedback para o Chat.
 * Comandos começam com "/" e são executados localmente (não são broadcast).
 */
public class CommandHandler {

    private Chat chat;

    public CommandHandler(Chat chat) {
        this.chat = chat;
    }

    /**
     * Processa um comando e retorna mensagem de feedback (ou null se não houver).
     * @param command O comando completo (ex: "/connect localhost 5000")
     * @return Mensagem de feedback para exibir localmente, ou null
     */
    public String handleCommand(String command) {
        if (command == null || command.isEmpty()) {
            return "[SISTEMA] Comando vazio";
        }

        // Remove a barra "/" e divide em partes
        String[] parts = command.substring(1).split(" ");
        String commandName = parts[0];

        // Busca o comando na enum
        CommandEnum cmd = CommandEnum.fromString(commandName);

        if (cmd == null) {
            return String.format("[SISTEMA] Comando '/%s' desconhecido. Digite /help para ver os comandos disponíveis.", commandName);
        }

        // Processa o comando usando a enum
        switch (cmd) {
            case CONNECT:
                return handleConnect(parts);

            case HELP:
                return CommandEnum.generateHelp();

            case EXIT:
                return handleExit();

            default:
                return "[SISTEMA] Comando não implementado";
        }
    }

    /**
     * Processa o comando /connect [host] [porta]
     */
    private String handleConnect(String[] parts) {
        if (parts.length != 3) {
            return "[SISTEMA] Uso: /connect [host] [porta]";
        }

        try {
            String host = parts[1];
            int port = Integer.parseInt(parts[2]);

            // Chama o método público do Chat
            // (connectionToPeer já exibe as mensagens de status)
            chat.connectionToPeer(host, port);

            return null; // Não retorna mensagem (já foi exibida em connectionToPeer)

        } catch (NumberFormatException e) {
            return "[SISTEMA] Erro: a porta deve ser um número válido";
        }
    }

    /**
     * Processa o comando /exit - encerra o chat de forma segura
     */
    private String handleExit() {
        // Chama o método de shutdown do Chat (que cuida de tudo)
        chat.shutdown();
        // Nunca chega aqui (shutdown chama System.exit)
        return null;
    }
}
