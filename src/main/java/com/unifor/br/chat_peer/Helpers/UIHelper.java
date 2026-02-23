package com.unifor.br.chat_peer.Helpers;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper para formatação de UI no terminal usando ANSI codes.
 * Gerencia cores de peers, painel inicial e formatação de mensagens estilo IRC.
 */
public class UIHelper {

    public static final String RESET = "\033[0m";
    public static final String GRAY = "\033[90m";
    public static final String BOLD = "\033[1m";

    private static final String[] PEER_COLORS = {
        "\033[31m", // RED
        "\033[32m", // GREEN
        "\033[33m", // YELLOW
        "\033[34m", // BLUE
        "\033[35m", // MAGENTA
        "\033[36m", // CYAN
    };

    // Mapeamento de peers para cores
    private final Map<String, String> peerColors = new HashMap<>();
    private int colorIndex = 0;

    /**
     * Limpa a tela do terminal
     */
    public static void clearScreen() {
        System.out.print("\033[2J\033[H");
        System.out.flush();
    }

    /**
     * Exibe o painel inicial após conectar
     * @param userName Nome do usuário
     * @param port Porta de escuta
     */
    public void showWelcomePanel(String userName, int port) {
        clearScreen();

        System.out.println(BOLD + "╔════════════════════════════════════════╗" + RESET);
        System.out.println(BOLD + "║         CHAT P2P - BEM-VINDO!          ║" + RESET);
        System.out.println(BOLD + "╚════════════════════════════════════════╝" + RESET);
        System.out.println();
        System.out.println("  Usuário: " + BOLD + userName + RESET);
        System.out.println("  Porta:   " + BOLD + port + RESET);
        System.out.println();
        System.out.println("─────────────────────────────────────────");
        System.out.println();

        System.out.println(CommandEnum.generateHelp());
        System.out.println();
        System.out.println("─────────────────────────────────────────");
        System.out.println();
    }

    /**
     * Atribui uma cor a um peer. Se já tiver cor, retorna a existente.
     * @param peerName Nome do peer
     * @return Código ANSI da cor
     */
    public String getColorForPeer(String peerName) {
        return peerColors.computeIfAbsent(peerName, k -> {
            String color = PEER_COLORS[colorIndex % PEER_COLORS.length];
            colorIndex++;
            return color;
        });
    }

    /**
     * Formata uma mensagem recebida de um peer remoto
     * @param peerName Nome do peer que enviou
     * @param message Conteúdo da mensagem
     * @return String formatada com cor
     */
    public String formatRemoteMessage(String peerName, String message) {
        String color = getColorForPeer(peerName);
        return color + peerName + RESET + ": " + message;
    }

    /**
     * Formata uma mensagem local (enviada pelo próprio usuário)
     * @param message Conteúdo da mensagem
     * @return String formatada em cinza com "VOCÊ: "
     */
    public String formatLocalMessage(String message) {
        return GRAY + "VOCÊ: " + message + RESET;
    }

    /**
     * Formata uma mensagem do sistema
     * @param message Mensagem do sistema
     * @return String formatada
     */
    public String formatSystemMessage(String message) {
        return BOLD + message + RESET;
    }

    /**
     * Exibe uma mensagem de conexão de peer
     * @param host Host conectado
     * @param port Porta conectada
     */
    public void showConnectionMessage(String host, int port) {
        System.out.println(BOLD + "[SISTEMA]" + RESET + " Conectado a " +
                          BOLD + host + ":" + port + RESET);
    }

    /**
     * Exibe uma mensagem de erro de conexão
     * @param errorMessage Mensagem de erro
     */
    public void showConnectionError(String errorMessage) {
        System.out.println(BOLD + "[ERRO]" + RESET + " " + errorMessage);
    }
}