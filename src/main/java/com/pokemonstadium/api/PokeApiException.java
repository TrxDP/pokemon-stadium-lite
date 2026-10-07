package com.pokemonstadium.api;

/** Error al consumir PokeAPI. Su mensaje ya es apto para mostrarse al usuario. */
public class PokeApiException extends Exception {

    public PokeApiException(String userMessage) {
        super(userMessage);
    }

    public PokeApiException(String userMessage, Throwable cause) {
        super(userMessage, cause);
    }
}