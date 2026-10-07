package com.pokemonstadium.api;

import com.pokemonstadium.model.Pokemon;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Consume PokeAPI con java.net.http.HttpClient y convierte el JSON en un {@link Pokemon}.
 * No conoce Swing. Sus métodos son BLOQUEANTES: deben llamarse fuera del EDT (SwingWorker).
 */
public class PokeApiClient {

    private static final String BASE_URL = "https://pokeapi.co/api/v2/pokemon/";
    private static final int MAX_POKEMON_ID = 1025;
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private static final String MSG_NETWORK =
            "Error de conexión con PokeAPI.\nVerifica tu conexión a Internet.";
    private static final String MSG_NOT_FOUND = "Pokémon no encontrado.";
    private static final String MSG_INVALID_DATA = "PokeAPI devolvió datos inválidos o incompletos.";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    /** Consulta GET /pokemon/{name}. El nombre se limpia y se pasa a minúsculas. */
    public Pokemon fetchByName(String name) throws PokeApiException {
        String normalized = name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            throw new PokeApiException("Ingresa el nombre de un Pokémon.");
        }
        String url = BASE_URL + URLEncoder.encode(normalized, StandardCharsets.UTF_8);
        byte[] body = download(url, true);
        return parsePokemon(body);
    }

    /**
     * Pokémon aleatorio. PokeAPI acepta el id numérico en el mismo endpoint
     * /pokemon/{name}, así que se sortea un id entre 1 y 1025.
     */
    public Pokemon fetchRandom() throws PokeApiException {
        int id = ThreadLocalRandom.current().nextInt(1, MAX_POKEMON_ID + 1);
        return fetchByName(String.valueOf(id));
    }

    private Pokemon parsePokemon(byte[] body) throws PokeApiException {
        try {
            JSONObject json = new JSONObject(new String(body, StandardCharsets.UTF_8));
            String name = json.getString("name");

            List<String> types = new ArrayList<>();
            JSONArray typeArray = json.getJSONArray("types");
            for (int i = 0; i < typeArray.length(); i++) {
                types.add(typeArray.getJSONObject(i).getJSONObject("type").getString("name"));
            }
            if (types.isEmpty()) {
                throw new PokeApiException(MSG_INVALID_DATA);
            }

            Map<String, Integer> stats = new HashMap<>();
            JSONArray statArray = json.getJSONArray("stats");
            for (int i = 0; i < statArray.length(); i++) {
                JSONObject entry = statArray.getJSONObject(i);
                stats.put(entry.getJSONObject("stat").getString("name"), entry.getInt("base_stat"));
            }

            // Sprite: sprites.front_default es una URL; se descarga como bytes.
            String spriteUrl = null;
            JSONObject sprites = json.getJSONObject("sprites");
            if (!sprites.isNull("front_default")) {
                spriteUrl = sprites.getString("front_default");
            }
            byte[] spriteData = spriteUrl == null ? null : download(spriteUrl, false);

            return new Pokemon(name, spriteUrl, spriteData, types,
                    requireStat(stats, "hp"), requireStat(stats, "attack"),
                    requireStat(stats, "defense"), requireStat(stats, "speed"));
        } catch (JSONException e) {
            throw new PokeApiException(MSG_INVALID_DATA, e);
        }
    }

    private int requireStat(Map<String, Integer> stats, String key) throws PokeApiException {
        Integer value = stats.get(key);
        if (value == null) {
            throw new PokeApiException(MSG_INVALID_DATA);
        }
        return value;
    }

    /** GET bloqueante que devuelve el cuerpo en bytes y traduce los errores a mensajes claros. */
    private byte[] download(String url, boolean notFoundMeansMissingPokemon) throws PokeApiException {
        HttpRequest request;
        try {
            request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(TIMEOUT)
                    .GET()
                    .build();
        } catch (IllegalArgumentException e) {
            throw new PokeApiException("Nombre de Pokémon inválido.", e);
        }

        try {
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            int status = response.statusCode();
            if (status == 200) {
                return response.body();
            }
            if (status == 404 && notFoundMeansMissingPokemon) {
                throw new PokeApiException(MSG_NOT_FOUND);
            }
            throw new PokeApiException("PokeAPI respondió con un error HTTP " + status + ".");
        } catch (IOException e) { // incluye timeouts y fallos de conexión
            throw new PokeApiException(MSG_NETWORK, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PokeApiException("La petición fue interrumpida.", e);
        }
    }
}