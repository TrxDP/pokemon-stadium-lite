package com.pokemonstadium.battle;

/**
 * Contrato entre la lógica de combate y quien quiera observarla (la UI).
 * Battle solo conoce esta interfaz, nunca componentes Swing.
 */
public interface BattleListener {

    /** Se emite una vez al empezar: nombres de los combatientes, quién ataca primero y el motivo. */
    void onBattleStarted(String pokemon1, String pokemon2, String firstAttacker, String reason);

    /** Se emite en cada ataque. */
    void onTurn(String attacker, String defender, int damage, boolean critical, double modifier);

    /** Se emite cuando cambia el HP de un Pokémon. */
    void onHpChanged(String pokemon, int hpActual);

    /** Se emite cuando un Pokémon llega a 0 HP. */
    void onBattleEnded(String winner);
}