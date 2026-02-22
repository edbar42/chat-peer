package com.unifor.br.chat_peer.Helpers;

/**
 * Enum que define todos os comandos disponíveis no chat.
 * Cada comando tem: nome, sintaxe de uso e descrição.
 */
public enum CommandEnum {

    CONNECT("connect", "/connect [host] [porta]", "Conectar a um peer"),
    HELP("help", "/help", "Exibir esta ajuda"),
    EXIT("exit", "/exit", "Encerrar o programa");

    private final String name;
    private final String usage;
    private final String description;

    /**
     * Construtor da enum
     */
    CommandEnum(String name, String usage, String description) {
        this.name = name;
        this.usage = usage;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getUsage() {
        return usage;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Busca um comando pelo nome (case-insensitive).
     * @param commandName Nome do comando (ex: "connect")
     * @return O CommandEnum correspondente, ou null se não encontrado
     */
    public static CommandEnum fromString(String commandName) {
        if (commandName == null) {
            return null;
        }
        for (CommandEnum cmd : values()) {
            if (cmd.name.equalsIgnoreCase(commandName)) {
                return cmd;
            }
        }
        return null;
    }

    /**
     * Gera automaticamente a mensagem de ajuda com todos os comandos.
     * @return String formatada com todos os comandos disponíveis
     */
    public static String generateHelp() {
        StringBuilder help = new StringBuilder();
        help.append("\n=== COMANDOS DISPONÍVEIS ===\n");
        for (CommandEnum cmd : values()) {
            help.append(String.format("  %-28s - %s\n", cmd.usage, cmd.description));
        }
        help.append("============================");
        return help.toString();
    }
}