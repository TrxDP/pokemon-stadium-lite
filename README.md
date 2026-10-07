# Pokémon Stadium Lite

Mini-aplicación de escritorio en Java Swing que simula un combate por turnos entre dos Pokémon obtenidos en tiempo real desde [PokeAPI](https://pokeapi.co/).
Proyecto de **Desarrollo de Software III**

**Autor:** Kevin Castillo Perez

## Tecnologías
Java 11+, Swing, `java.net.http.HttpClient`, `org.json`, Maven, PokeAPI.

## Requisitos
- JDK 11 o superior
- Maven (incluido en IntelliJ IDEA) o instalado por separado
- Conexión a Internet

## Instrucciones de ejecución

### Opción A — IntelliJ IDEA
1. Clonar el repositorio: `https://github.com/TrxDP/pokemon-stadium-lite.git`
2. En IntelliJ: **File → Open** y seleccionar el archivo `pom.xml` (Open as Project).
3. Esperar a que Maven descargue la dependencia `org.json` (o pulsar **Reload All Maven Projects**).
4. Abrir `src/main/java/com/pokemonstadium/Main.java` y ejecutar el método `main` (▶).

### Opción B — Terminal
```bash
git clone https://github.com/TrxDP/pokemon-stadium-lite.git
cd pokemon-stadium-lite
mvn compile exec:java
```

### Uso
1. En cada jugador, pulsar **Random** o escribir un nombre (ej. `pikachu`) y pulsar **Load**.
2. Cuando ambos Pokémon estén cargados, se habilita **FIGHT!**.
3. Observar el combate en el **Battle Log** hasta que se anuncie al ganador.
4. Para una nueva batalla, cargar un Pokémon nuevo en cualquiera de los jugadores.

## Explicación de diseño

La aplicación separa responsabilidades en cuatro paquetes. `model` contiene `Pokemon` (datos y HP, sin código gráfico); `api` contiene `PokeApiClient`, que consume `https://pokeapi.co/api/v2/pokemon/{name}` con `HttpClient`, parsea el JSON y devuelve un `Pokemon`; `battle` contiene `Battle`, que implementa el orden por Speed (empate al azar), el daño, el crítico del 10 % (x1.5) y la efectividad por primer tipo (Agua > Fuego > Planta > Agua = x1.3, inversa = x0.7, resto = x1.0); y `ui` contiene la interfaz Swing. `Battle` no conoce Swing: se comunica únicamente mediante la interfaz propia `BattleListener` (`onTurn`, `onHpChanged`, `onBattleEnded`, más `onBattleStarted`), y la UI actualiza barras y log solo a partir de esos eventos. Los botones **Load**, **Random** y **Fight!** usan `ActionListener`, cada uno con una clase de responsabilidad única.

Las peticiones HTTP y la descarga del sprite se ejecutan en un `SwingWorker` (`doInBackground()` en un hilo secundario) y los componentes se actualizan en `done()`, que corre en el EDT, por lo que la interfaz nunca se congela. El combate avanza un turno por tick de un `javax.swing.Timer`, que también corre en el EDT. Los errores (campo vacío, Pokémon inexistente, fallo de red, JSON inválido) se muestran al usuario con mensajes claros.

**Fórmula de daño:**
```
base  = ATK × r1 − 0.5 × DEF × r2     (r1, r2 aleatorios en [0,1))
daño  = max(1, base) × efectividad × (crítico ? 1.5 : 1)
final = max(1, round(daño))           (entero, nunca negativo)
```
El HP se actualiza con `HP = max(0, HP − daño)`.

