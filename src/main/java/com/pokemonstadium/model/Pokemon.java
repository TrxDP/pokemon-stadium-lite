package com.pokemonstadium.model;

import java.util.List;
import java.util.Locale;

/**
 * Modelo de un Pokémon. Responsabilidad: guardar datos y estado de HP.
 * No depende de Swing ni de la API.
 */
public class Pokemon {

    private final String name;
    private final String spriteUrl;
    private final byte[] spriteData; // bytes de la imagen (puede ser null); el modelo no conoce Swing
    private final List<String> types;
    private final int maxHp;
    private final int attack;
    private final int defense;
    private final int speed;
    private int currentHp;

    public Pokemon(String name, String spriteUrl, byte[] spriteData, List<String> types,
                   int maxHp, int attack, int defense, int speed) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        if (types == null || types.isEmpty()) {
            throw new IllegalArgumentException("El Pokémon debe tener al menos un tipo.");
        }
        if (maxHp <= 0) {
            throw new IllegalArgumentException("El HP máximo debe ser mayor que 0.");
        }
        this.name = name;
        this.spriteUrl = spriteUrl;
        this.spriteData = spriteData;
        this.types = List.copyOf(types);
        this.maxHp = maxHp;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.currentHp = maxHp;
    }

    /** Resta daño sin permitir HP negativo: HP = max(0, HP - daño). */
    public void takeDamage(int damage) {
        currentHp = Math.max(0, currentHp - Math.max(0, damage));
    }

    public void restoreHp() {
        currentHp = maxHp;
    }

    public boolean isFainted() {
        return currentHp == 0;
    }

    /** Convierte la primera letra a mayúscula (pikachu -> Pikachu). */
    public static String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase(Locale.ROOT) + text.substring(1);
    }

    public String getName() { return name; }
    public String getDisplayName() { return capitalize(name); }
    public String getSpriteUrl() { return spriteUrl; }
    public byte[] getSpriteData() { return spriteData == null ? null : spriteData.clone(); }
    public List<String> getTypes() { return types; }
    public String getPrimaryType() { return types.get(0); }
    public int getMaxHp() { return maxHp; }
    public int getCurrentHp() { return currentHp; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getSpeed() { return speed; }
}