package com.unifor.br.chat_peer.Helpers;

import com.unifor.br.chat_peer.Chat;

/**
 * Processa comandos do usuário no formato "/comando [args]".
 * Comandos são executados localmente e não enviados como mensagens.
 *
 * @author Eduardo Barroso
 * @version 1.0
 */
public class CommandHandler {

    private final Chat chat;

    /**
     * Construtor do handler de comandos.
     *
     * @param chat Instância do chat para executar ações
     */
    public CommandHandler(Chat chat) {
        this.chat = chat;
    }

    /**
     * Processa um comando e retorna feedback.
     *
     * @param command Comando completo (ex: "/connect localhost 5000")
     * @return Mensagem de feedback, ou null se não houver
     */
    public String handleCommand(String command) {
        if (command == null || command.isEmpty()) {
            return "[SISTEMA] Comando vazio";
        }

        String[] parts = command.substring(1).split(" ");
        String commandName = parts[0];

        CommandEnum cmd = CommandEnum.fromString(commandName);

        if (cmd == null) {
            return String.format("[SISTEMA] Comando '/%s' desconhecido. Digite /help para ver os comandos disponíveis.", commandName);
        }

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
     * Conecta a outro peer.
     *
     * @param parts Argumentos do comando [comando, host, porta]
     * @return Mensagem de erro, ou null se sucesso
     */
    private String handleConnect(String[] parts) {
        if (parts.length != 3) {
            return "[SISTEMA] Uso: /connect [host] [porta]";
        }

        try {
            String host = parts[1];
            int port = Integer.parseInt(parts[2]);

            chat.connectionToPeer(host, port);
            return null;

        } catch (NumberFormatException e) {
            return "[SISTEMA] Erro: a porta deve ser um número válido";
        }
    }

    /**
     * Encerra o chat de forma segura.
     *
     * @return null (o programa termina antes de retornar)
     */
    private String handleExit() {
        chat.shutdown();
        return null;
    }
}