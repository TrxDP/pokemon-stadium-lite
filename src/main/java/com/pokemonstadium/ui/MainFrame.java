package com.pokemonstadium.ui;

import com.pokemonstadium.api.PokeApiClient;
import com.pokemonstadium.api.PokeApiException;
import com.pokemonstadium.battle.Battle;
import com.pokemonstadium.battle.BattleListener;
import com.pokemonstadium.model.Pokemon;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * Ventana principal. Coordina paneles, botones (ActionListener), carga con SwingWorker y
 * combate (Battle + BattleListener). La lógica de combate vive en Battle; aquí solo se
 * reacciona a sus eventos.
 */
public class MainFrame extends JFrame {

    private static final int TURN_DELAY_MS = 900;

    private final PokeApiClient apiClient = new PokeApiClient();
    private final PokemonPanel player1Panel = new PokemonPanel("JUGADOR 1");
    private final PokemonPanel player2Panel = new PokemonPanel("JUGADOR 2");
    private final JButton fightButton = new JButton("FIGHT!");
    private final JLabel resultLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JTextArea logArea = new JTextArea(9, 50);

    private Timer turnTimer;
    private boolean battleRunning;
    private boolean battleFinished;
    private boolean hpLogPending;
    private String label1;
    private String label2;

    public MainFrame() {
        super("Pokémon Stadium Lite");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        buildLayout();
        registerListeners();
        refreshControls();
        pack();
        setMinimumSize(new Dimension(1050, 760));
        setLocationRelativeTo(null);
    }

    // ------------------------------------------------------------------ construcción de UI

    private void buildLayout() {
        StadiumBackground background = new StadiumBackground();
        background.setLayout(new BorderLayout(0, 8));
        background.setBorder(BorderFactory.createEmptyBorder(12, 18, 14, 18));

        JLabel title = new JLabel("POKÉMON STADIUM LITE", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(2, 10, 6, 10));

        JLabel subtitle = new JLabel("BATTLE ARENA  •  ONLINE POKÉDEX  •  TURN BASED", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.BOLD, 11));
        subtitle.setForeground(new Color(180, 215, 250));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.CENTER);
        header.add(subtitle, BorderLayout.SOUTH);

        JLabel vs = new JLabel("VS", SwingConstants.CENTER);
        vs.setFont(new Font("SansSerif", Font.BOLD, 34));
        vs.setForeground(Color.WHITE);
        vs.setOpaque(true);
        vs.setBackground(new Color(10, 22, 45, 225));
        vs.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(75, 175, 255), 2),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)));

        JPanel arena = new JPanel(new GridBagLayout());
        arena.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 10, 4, 10);
        c.gridy = 0;
        c.weighty = 1;

        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.BOTH;
        arena.add(player1Panel, c);

        c.gridx = 1;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        arena.add(vs, c);

        c.gridx = 2;
        c.weightx = 1;
        c.fill = GridBagConstraints.BOTH;
        arena.add(player2Panel, c);

        styleFightButton();
        resultLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        resultLabel.setForeground(Color.WHITE);

        JPanel fightButtonRow = new JPanel();
        fightButtonRow.setOpaque(false);
        fightButtonRow.add(fightButton);

        JPanel controls = new JPanel(new BorderLayout(0, 2));
        controls.setOpaque(false);
        controls.add(fightButtonRow, BorderLayout.CENTER);
        controls.add(resultLabel, BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout(0, 5));
        center.setOpaque(false);
        center.add(arena, BorderLayout.CENTER);
        center.add(controls, BorderLayout.SOUTH);

        styleBattleLog();
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setPreferredSize(new Dimension(900, 180));
        logScroll.setBorder(BorderFactory.createLineBorder(new Color(55, 150, 245), 2));
        logScroll.getViewport().setBackground(new Color(4, 12, 26));
        logScroll.setBackground(new Color(4, 12, 26));

        JPanel logContainer = new JPanel(new BorderLayout());
        logContainer.setOpaque(false);
        JLabel logTitle = new JLabel("  BATTLE LOG", SwingConstants.LEFT);
        logTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        logTitle.setForeground(Color.WHITE);
        logTitle.setOpaque(true);
        logTitle.setBackground(new Color(10, 63, 120));
        logTitle.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        logContainer.add(logTitle, BorderLayout.NORTH);
        logContainer.add(logScroll, BorderLayout.CENTER);

        background.add(header, BorderLayout.NORTH);
        background.add(center, BorderLayout.CENTER);
        background.add(logContainer, BorderLayout.SOUTH);

        setContentPane(background);
    }

    private void styleFightButton() {
        fightButton.setFont(new Font("SansSerif", Font.BOLD, 22));
        fightButton.setForeground(Color.WHITE);
        fightButton.setBackground(new Color(8, 104, 190));
        fightButton.setFocusPainted(false);
        fightButton.setOpaque(true);
        fightButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(90, 205, 255), 2),
                BorderFactory.createEmptyBorder(10, 46, 10, 46)));
        fightButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }

    private void styleBattleLog() {
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        logArea.setForeground(new Color(220, 235, 255));
        logArea.setBackground(new Color(4, 12, 26));
        logArea.setCaretColor(Color.WHITE);
        logArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
    }

    private void registerListeners() {
        player1Panel.addLoadListener(new LoadAction(player1Panel));
        player1Panel.addRandomListener(new RandomAction(player1Panel));
        player2Panel.addLoadListener(new LoadAction(player2Panel));
        player2Panel.addRandomListener(new RandomAction(player2Panel));
        fightButton.addActionListener(new FightAction());
    }

    // ------------------------------------------------------------------ ActionListeners

    /** Botón Load: valida el campo y lanza la carga por nombre. */
    private class LoadAction implements ActionListener {
        private final PokemonPanel panel;

        LoadAction(PokemonPanel panel) {
            this.panel = panel;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String name = panel.getRequestedName().trim().toLowerCase(Locale.ROOT);
            if (name.isEmpty()) {
                showError("Ingresa el nombre de un Pokémon.");
                return;
            }
            startLoading(panel, () -> apiClient.fetchByName(name));
        }
    }

    /** Botón Random: lanza la carga de un Pokémon aleatorio. */
    private class RandomAction implements ActionListener {
        private final PokemonPanel panel;

        RandomAction(PokemonPanel panel) {
            this.panel = panel;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            startLoading(panel, apiClient::fetchRandom);
        }
    }

    /** Botón Fight!: crea la batalla y avanza un turno por tick del Timer (que corre en el EDT). */
    private class FightAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Pokemon first = player1Panel.getPokemon();
            Pokemon second = player2Panel.getPokemon();
            if (first == null || second == null || battleRunning) {
                return;
            }
            logArea.setText("");
            resultLabel.setText(" ");
            hpLogPending = false;
            battleRunning = true;
            refreshControls();

            Battle battle = new Battle(first, second, new UiBattleListener());
            turnTimer = new Timer(TURN_DELAY_MS, tick -> battle.nextTurn());
            battle.start();
            turnTimer.start();
        }
    }

    // ------------------------------------------------------------------ carga con SwingWorker

    private void startLoading(PokemonPanel panel, Callable<Pokemon> task) {
        panel.setLoading(true);
        refreshControls();
        new LoadWorker(panel, task).execute();
    }

    /**
     * SwingWorker: doInBackground() corre en un hilo secundario (petición HTTP, parseo JSON y
     * descarga del sprite), así el EDT sigue atendiendo la interfaz. done() se ejecuta de nuevo
     * en el EDT, donde es seguro tocar componentes Swing.
     */
    private class LoadWorker extends SwingWorker<Pokemon, Void> {
        private final PokemonPanel panel;
        private final Callable<Pokemon> task;

        LoadWorker(PokemonPanel panel, Callable<Pokemon> task) {
            this.panel = panel;
            this.task = task;
        }

        @Override
        protected Pokemon doInBackground() throws Exception {
            return task.call();
        }

        @Override
        protected void done() {
            panel.setLoading(false);
            try {
                onPokemonLoaded(panel, get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                showError("La carga fue interrumpida.");
            } catch (ExecutionException e) {
                showError(messageFor(e.getCause()));
            }
            refreshControls();
        }
    }

    private void onPokemonLoaded(PokemonPanel panel, Pokemon pokemon) {
        panel.showPokemon(pokemon);
        if (battleFinished) { // nueva ronda: se restauran ambos Pokémon
            battleFinished = false;
            player1Panel.resetHp();
            player2Panel.resetHp();
            resultLabel.setText(" ");
        }
    }

    private String messageFor(Throwable error) {
        if (error instanceof PokeApiException) {
            if (error.getCause() != null) {
                error.getCause().printStackTrace(); // solo para depuración
            }
            return error.getMessage();
        }
        if (error != null) {
            error.printStackTrace();
        }
        return "Ocurrió un error inesperado. Intenta nuevamente.";
    }

    // ------------------------------------------------------------------ estado de controles

    /** Fight! solo se habilita con dos Pokémon cargados y sin batalla ni carga en curso. */
    private void refreshControls() {
        boolean anyLoading = player1Panel.isLoading() || player2Panel.isLoading();
        player1Panel.setControlsEnabled(!battleRunning && !player1Panel.isLoading());
        player2Panel.setControlsEnabled(!battleRunning && !player2Panel.isLoading());
        fightButton.setEnabled(player1Panel.getPokemon() != null
                && player2Panel.getPokemon() != null
                && !battleRunning && !battleFinished && !anyLoading);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Pokémon Stadium Lite", JOptionPane.WARNING_MESSAGE);
    }

    private void appendLog(String line) {
        logArea.append(line + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    // ------------------------------------------------------------------ eventos de la batalla

    /** La UI se actualiza únicamente a partir de estos eventos. Se invocan desde el EDT (Timer). */
    private class UiBattleListener implements BattleListener {

        @Override
        public void onBattleStarted(String pokemon1, String pokemon2, String firstAttacker, String reason) {
            label1 = pokemon1;
            label2 = pokemon2;
            appendLog("=== BATALLA INICIADA ===");
            appendLog("");
            appendLog(pokemon1 + " VS " + pokemon2);
            appendLog("");
            appendLog(reason);
            appendLog(firstAttacker + " comienza.");
        }

        @Override
        public void onTurn(String attacker, String defender, int damage, boolean critical, double modifier) {
            appendLog("");
            appendLog(attacker + " ataca a " + defender + ".");
            appendLog("Daño: " + damage);
            if (critical) {
                appendLog("Golpe crítico.");
            }
            appendLog(String.format(Locale.US, "Efectividad: x%.1f", modifier));
            hpLogPending = true; // la línea de HP restante se escribe en onHpChanged
        }

        @Override
        public void onHpChanged(String pokemon, int hpActual) {
            panelFor(pokemon).updateHp(hpActual);
            if (hpLogPending) {
                appendLog("HP restante de " + pokemon + ": " + hpActual);
                hpLogPending = false;
            }
        }

        @Override
        public void onBattleEnded(String winner) {
            String loser = winner.equals(label1) ? label2 : label1;
            appendLog("");
            appendLog(loser + " ha sido derrotado.");
            appendLog("");
            appendLog("=== FIN DE BATALLA ===");
            appendLog("Ganador: " + winner);
            resultLabel.setText("Ganador: " + winner);

            turnTimer.stop();
            battleRunning = false;
            battleFinished = true;
            refreshControls();
        }

        private PokemonPanel panelFor(String pokemon) {
            return pokemon.equals(label1) ? player1Panel : player2Panel;
        }
    }

    /**
     * Fondo pintado directamente en Swing para que la interfaz tenga apariencia de estadio sin
     * depender de imágenes externas. No interviene en la lógica del juego.
     */
    private static class StadiumBackground extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Cielo.
            g2.setPaint(new GradientPaint(0, 0, new Color(15, 85, 165), 0, h * 0.52f, new Color(95, 175, 235)));
            g2.fillRect(0, 0, w, h);

            // Nubes luminosas.
            g2.setColor(new Color(255, 255, 255, 55));
            for (int i = 0; i < 8; i++) {
                int x = (i * 173) % Math.max(w, 1);
                int y = 55 + (i % 3) * 32;
                g2.fillOval(x, y, 145, 34);
            }

            // Gradas.
            int standsTop = Math.min(190, Math.max(145, h / 4));
            g2.setPaint(new GradientPaint(0, standsTop, new Color(32, 52, 82), 0, standsTop + 170, new Color(9, 22, 43)));
            g2.fillRect(0, standsTop, w, 205);

            // Filas de público abstractas.
            int peopleRows = 5;
            for (int row = 0; row < peopleRows; row++) {
                int y = standsTop + 20 + row * 32;
                for (int x = 8; x < w; x += 22) {
                    int phase = (x / 22 + row) % 4;
                    Color personColor = phase == 0 ? new Color(255, 214, 75)
                            : phase == 1 ? new Color(245, 90, 90)
                            : phase == 2 ? new Color(95, 190, 255)
                            : new Color(230, 235, 245);
                    g2.setColor(personColor);
                    g2.fillOval(x, y, 9, 9);
                    g2.fillRoundRect(x - 2, y + 8, 13, 11, 4, 4);
                }
            }

            // Reflectores laterales.
            drawLightTower(g2, 26, 40, 1);
            drawLightTower(g2, w - 78, 40, -1);

            // Pista del estadio.
            int fieldTop = standsTop + 145;
            g2.setPaint(new GradientPaint(0, fieldTop, new Color(46, 150, 92), 0, h, new Color(19, 91, 57)));
            g2.fillRect(0, fieldTop, w, h - fieldTop);

            // Líneas de cancha.
            g2.setColor(new Color(235, 255, 240, 190));
            g2.setStroke(new BasicStroke(4f));
            int centerY = fieldTop + (h - fieldTop) / 2;
            g2.drawLine(0, centerY, w, centerY);
            g2.drawOval(w / 2 - 85, centerY - 85, 170, 170);
            g2.drawOval(w / 2 - 9, centerY - 9, 18, 18);
            g2.drawLine(w / 2, fieldTop, w / 2, h);

            // Poké Ball central decorativa.
            int ballX = w / 2 - 43;
            int ballY = fieldTop + 12;
            g2.setColor(new Color(230, 245, 255, 215));
            g2.fillOval(ballX, ballY, 86, 86);
            g2.setColor(new Color(220, 55, 65, 225));
            g2.fillArc(ballX, ballY, 86, 86, 0, 180);
            g2.setColor(new Color(18, 30, 50));
            g2.fillRect(ballX, ballY + 40, 86, 6);
            g2.setColor(Color.WHITE);
            g2.fillOval(ballX + 30, ballY + 29, 26, 26);
            g2.setColor(new Color(18, 30, 50));
            g2.drawOval(ballX + 30, ballY + 29, 26, 26);

            // Oscurecimiento inferior para integrar el log.
            g2.setPaint(new GradientPaint(0, h - 260, new Color(4, 20, 38, 0), 0, h, new Color(2, 10, 22, 180)));
            g2.fillRect(0, h - 260, w, 260);

            g2.dispose();
        }

        private void drawLightTower(Graphics2D g2, int x, int y, int direction) {
            g2.setColor(new Color(15, 25, 45, 230));
            g2.fillRoundRect(x, y, 50, 135, 12, 12);
            g2.setColor(new Color(235, 245, 255));
            for (int row = 0; row < 4; row++) {
                for (int col = 0; col < 3; col++) {
                    g2.fillRoundRect(x + 7 + col * 13, y + 8 + row * 29, 9, 18, 3, 3);
                }
            }
            g2.setColor(new Color(255, 255, 255, 25));
            int beamX = direction > 0 ? x + 25 : x - 120;
            g2.fillPolygon(new int[]{x + 25, x + 25, beamX}, new int[]{y + 130, y + 115, y + 205}, 3);
        }
    }
}
