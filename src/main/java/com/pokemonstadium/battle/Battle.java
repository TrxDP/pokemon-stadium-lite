package com.pokemonstadium.battle;

import com.pokemonstadium.model.Pokemon;

import java.util.Random;

/**
 *
 * Uso: start() una vez y luego nextTurn() repetidamente hasta que isFinished() sea true.
 * Depende de: Pokemon y BattleListener.
 *
 * Fórmula de daño:
 *   base   = ATK * r1 - 0.5 * DEF * r2        (r1, r2 aleatorios en [0,1))
 *   daño   = max(1, base) * efectividad * (crítico ? 1.5 : 1)
 *   final  = max(1, round(daño))
 * Efectividad: usa el PRIMER tipo del atacante contra el PRIMER tipo del defensor.
 */
public class Battle {

    private static final double CRITICAL_CHANCE = 0.10;
    private static final double CRITICAL_MULTIPLIER = 1.5;
    private static final double DEFENSE_WEIGHT = 0.5;
    private static final double SUPER_EFFECTIVE = 1.3;
    private static final double NOT_EFFECTIVE = 0.7;
    private static final double NEUTRAL = 1.0;

    private final Pokemon[] fighters = new Pokemon[2];
    private final String[] labels = new String[2];
    private final BattleListener listener;
    private final Random random;

    private int current;
    private boolean started;
    private boolean finished;

    public Battle(Pokemon first, Pokemon second, BattleListener listener) {
        this(first, second, listener, new Random());
    }

    public Battle(Pokemon first, Pokemon second, BattleListener listener, Random random) {
        if (first == null || second == null || listener == null || random == null) {
            throw new IllegalArgumentException("Pokémon, listener y random son obligatorios.");
        }
        fighters[0] = first;
        fighters[1] = second;
        this.listener = listener;
        this.random = random;

        // Si ambos se llaman igual, se diferencian para que los eventos no sean ambiguos.
        if (first.getDisplayName().equals(second.getDisplayName())) {
            labels[0] = first.getDisplayName() + " (J1)";
            labels[1] = second.getDisplayName() + " (J2)";
        } else {
            labels[0] = first.getDisplayName();
            labels[1] = second.getDisplayName();
        }
    }

    /** Reinicia el HP, decide quién empieza según Speed (empate al azar) y notifica. */
    public void start() {
        if (started) {
            throw new IllegalStateException("La batalla ya fue iniciada.");
        }
        started = true;
        fighters[0].restoreHp();
        fighters[1].restoreHp();

        int speed0 = fighters[0].getSpeed();
        int speed1 = fighters[1].getSpeed();
        String reason;
        if (speed0 > speed1) {
            current = 0;
            reason = labels[0] + " tiene mayor Speed (" + speed0 + " vs " + speed1 + ").";
        } else if (speed1 > speed0) {
            current = 1;
            reason = labels[1] + " tiene mayor Speed (" + speed1 + " vs " + speed0 + ").";
        } else {
            current = random.nextInt(2);
            reason = "Ambos tienen la misma Speed (" + speed0 + "). El primer atacante se eligió al azar.";
        }

        listener.onBattleStarted(labels[0], labels[1], labels[current], reason);
        listener.onHpChanged(labels[0], fighters[0].getCurrentHp());
        listener.onHpChanged(labels[1], fighters[1].getCurrentHp());
    }

    /** Ejecuta un ataque. No hace nada si la batalla no empezó o ya terminó. */
    public void nextTurn() {
        if (!started || finished) {
            return;
        }
        int defenderIndex = 1 - current;
        Pokemon attacker = fighters[current];
        Pokemon defender = fighters[defenderIndex];

        double modifier = effectiveness(attacker.getPrimaryType(), defender.getPrimaryType());
        boolean critical = random.nextDouble() < CRITICAL_CHANCE;
        int damage = calculateDamage(attacker, defender, modifier, critical);

        defender.takeDamage(damage);

        listener.onTurn(labels[current], labels[defenderIndex], damage, critical, modifier);
        listener.onHpChanged(labels[defenderIndex], defender.getCurrentHp());

        if (defender.isFainted()) {
            finished = true;
            listener.onBattleEnded(labels[current]);
        } else {
            current = defenderIndex;
        }
    }

    public boolean isFinished() {
        return finished;
    }

    private int calculateDamage(Pokemon attacker, Pokemon defender, double modifier, boolean critical) {
        double base = attacker.getAttack() * random.nextDouble()
                - DEFENSE_WEIGHT * defender.getDefense() * random.nextDouble();
        double damage = Math.max(1.0, base) * modifier;
        if (critical) {
            damage *= CRITICAL_MULTIPLIER;
        }
        return Math.max(1, (int) Math.round(damage));
    }

    /** agua > fuego > planta > agua = x1.3; la relación inversa = x0.7; el resto = x1.0. */
    static double effectiveness(String attackerType, String defenderType) {
        if (beats(attackerType, defenderType)) {
            return SUPER_EFFECTIVE;
        }
        if (beats(defenderType, attackerType)) {
            return NOT_EFFECTIVE;
        }
        return NEUTRAL;
    }

    private static boolean beats(String attacker, String defender) {
        return (attacker.equals("water") && defender.equals("fire"))
                || (attacker.equals("fire") && defender.equals("grass"))
                || (attacker.equals("grass") && defender.equals("water"));
    }
}