package com.pokemonstadium.ui;

import com.pokemonstadium.model.Pokemon;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.util.stream.Collectors;

/**
 * Panel visual de un jugador: sprite, datos, barra de HP, campo de texto y botones Load/Random.
 * Solo muestra información; no contiene lógica de red ni de combate.
 * Todos sus métodos deben llamarse desde el EDT.
 */
public class PokemonPanel extends JPanel {

    private static final Color HP_GREEN = new Color(43, 220, 85);
    private static final Color HP_ORANGE = new Color(255, 181, 45);
    private static final Color HP_RED = new Color(255, 65, 65);

    private static final Color BLUE_BORDER = new Color(30, 151, 255);
    private static final Color RED_BORDER = new Color(255, 55, 75);

    private final JLabel headerLabel = new JLabel("JUGADOR", SwingConstants.CENTER);
    private final JLabel spriteLabel = new JLabel("Sin Pokémon", SwingConstants.CENTER);
    private final JLabel nameLabel = new JLabel("-", SwingConstants.CENTER);
    private final JLabel typesLabel = new JLabel("Tipos: -", SwingConstants.CENTER);
    private final JProgressBar hpBar = new JProgressBar(0, 1);
    private final JLabel attackLabel = new JLabel("Attack: -", SwingConstants.CENTER);
    private final JLabel defenseLabel = new JLabel("Defense: -", SwingConstants.CENTER);
    private final JLabel speedLabel = new JLabel("Speed: -", SwingConstants.CENTER);
    private final JTextField nameField = new JTextField(12);
    private final JButton loadButton = new JButton("Load");
    private final JButton randomButton = new JButton("Random");

    private final Color accentColor;
    private Pokemon pokemon;
    private boolean loading;

    public PokemonPanel(String title) {
        accentColor = title.toUpperCase().contains("2") ? RED_BORDER : BLUE_BORDER;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
        setPreferredSize(new Dimension(430, 500));

        headerLabel.setText(title);
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setOpaque(true);
        headerLabel.setBackground(accentColor);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 12));
        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        Dimension spriteSize = new Dimension(150, 130);
        spriteLabel.setPreferredSize(spriteSize);
        spriteLabel.setMinimumSize(spriteSize);
        spriteLabel.setMaximumSize(spriteSize);
        spriteLabel.setForeground(new Color(205, 220, 240));
        spriteLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 25));
        nameLabel.setForeground(Color.WHITE);

        typesLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        typesLabel.setForeground(new Color(215, 225, 240));

        hpBar.setStringPainted(true);
        hpBar.setString("HP: -");
        hpBar.setFont(new Font("SansSerif", Font.BOLD, 13));
        hpBar.setForeground(HP_GREEN);
        hpBar.setBackground(new Color(15, 25, 42));
        hpBar.setBorder(BorderFactory.createLineBorder(new Color(110, 160, 210), 1));
        hpBar.setPreferredSize(new Dimension(350, 28));
        hpBar.setMaximumSize(new Dimension(350, 28));

        styleStatLabel(attackLabel);
        styleStatLabel(defenseLabel);
        styleStatLabel(speedLabel);

        nameField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        nameField.setForeground(new Color(25, 35, 55));
        nameField.setBackground(Color.WHITE);
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(105, 160, 220), 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        nameField.setPreferredSize(new Dimension(190, 34));

        styleButton(loadButton, accentColor);
        styleButton(randomButton, accentColor);

        JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        inputRow.setOpaque(false);
        inputRow.add(nameField);
        inputRow.add(loadButton);
        fixSize(inputRow);

        JPanel randomRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        randomRow.setOpaque(false);
        randomRow.add(randomButton);
        fixSize(randomRow);

        add(Box.createVerticalStrut(4));
        add(headerLabel);
        add(Box.createVerticalStrut(10));
        add(centered(spriteLabel));
        add(Box.createVerticalStrut(2));
        add(centered(nameLabel));
        add(Box.createVerticalStrut(2));
        add(centered(typesLabel));
        add(Box.createVerticalStrut(12));
        add(centered(hpBar));
        add(Box.createVerticalStrut(12));
        add(centered(attackLabel));
        add(centered(defenseLabel));
        add(centered(speedLabel));
        add(Box.createVerticalStrut(14));
        add(centered(inputRow));
        add(Box.createVerticalStrut(8));
        add(centered(randomRow));
        add(Box.createVerticalGlue());
    }

    private void styleStatLabel(JLabel label) {
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(new Color(230, 238, 250));
    }

    private void styleButton(JButton button, Color color) {
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(color.darker());
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.brighter(), 1),
                BorderFactory.createEmptyBorder(7, 16, 7, 16)));
        button.setOpaque(true);
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }

    private JComponent centered(JComponent component) {
        component.setAlignmentX(Component.CENTER_ALIGNMENT);
        return component;
    }

    private void fixSize(JComponent component) {
        component.setMaximumSize(component.getPreferredSize());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth() - 10;
        int h = getHeight() - 8;
        int x = 5;
        int y = 2;

        g2.setPaint(new GradientPaint(0, y, new Color(7, 28, 58, 245), 0, h, new Color(8, 16, 34, 245)));
        g2.fillRoundRect(x, y, w, h, 24, 24);

        g2.setColor(accentColor);
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawRoundRect(x, y, w, h, 24, 24);

        g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 55));
        g2.fillOval(w - 100, 65, 125, 125);
        g2.fillOval(30, h - 105, 90, 90);

        g2.dispose();
    }

    /** Muestra todos los datos del Pokémon (ya descargados fuera del EDT). */
    public void showPokemon(Pokemon newPokemon) {
        this.pokemon = newPokemon;

        byte[] data = newPokemon.getSpriteData();
        ImageIcon icon = data == null ? null : new ImageIcon(data);
        if (icon != null && icon.getIconWidth() > 0) {
            spriteLabel.setIcon(icon);
            spriteLabel.setText("");
        } else {
            spriteLabel.setIcon(null);
            spriteLabel.setText("Sin sprite");
        }

        nameLabel.setText(newPokemon.getDisplayName());
        typesLabel.setText("Tipos: " + newPokemon.getTypes().stream()
                .map(Pokemon::capitalize).collect(Collectors.joining(" / ")));
        attackLabel.setText("Attack: " + newPokemon.getAttack());
        defenseLabel.setText("Defense: " + newPokemon.getDefense());
        speedLabel.setText("Speed: " + newPokemon.getSpeed());

        hpBar.setMaximum(newPokemon.getMaxHp());
        updateHp(newPokemon.getCurrentHp());
    }

    /** Actualiza barra y texto de HP. */
    public void updateHp(int hp) {
        if (pokemon == null) {
            return;
        }
        hpBar.setValue(hp);
        hpBar.setString("HP " + hp + " / " + pokemon.getMaxHp());
        double ratio = (double) hp / pokemon.getMaxHp();
        hpBar.setForeground(ratio > 0.5 ? HP_GREEN : ratio > 0.2 ? HP_ORANGE : HP_RED);
    }

    /** Restaura el HP del Pokémon cargado y lo refleja en la barra. */
    public void resetHp() {
        if (pokemon != null) {
            pokemon.restoreHp();
            updateHp(pokemon.getCurrentHp());
        }
    }

    public Pokemon getPokemon() {
        return pokemon;
    }

    public String getRequestedName() {
        return nameField.getText();
    }

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
        loadButton.setText(loading ? "Cargando..." : "Load");
    }

    public void setControlsEnabled(boolean enabled) {
        nameField.setEnabled(enabled);
        loadButton.setEnabled(enabled);
        randomButton.setEnabled(enabled);
    }

    /** Se asocia al botón Load y a la tecla Enter del campo de texto. */
    public void addLoadListener(ActionListener listener) {
        loadButton.addActionListener(listener);
        nameField.addActionListener(listener);
    }

    public void addRandomListener(ActionListener listener) {
        randomButton.addActionListener(listener);
    }
}
