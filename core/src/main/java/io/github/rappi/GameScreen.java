package io.github.rappi;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameScreen implements Screen {

    private final Main juego;

    private SpriteBatch loteDibujo;
    private Player repartidor;
    private Patrullero patrullero;
    private Array<ParallaxLayer> capasFondo;
    private Piso piso;

    // --- GESTIÓN DE OBSTÁCULOS ---
    private Array<Obstaculos> obstaculos;
    private float tiempoInicial;
    private float distanciaHastaObstaculo;

    // Cámara y Viewport para escalado automático
    private OrthographicCamera camara;
    private Viewport vista;
    private static final float ANCHO_MUNDO = 1280f;
    private static final float ALTO_MUNDO = 720f;

    // Gestor de puntaje y fuente bitmap
    private ScoreManager gestorPuntaje;
    private BitmapFont fuente;
    private GlyphLayout medidasTexto;
    private final GlyphLayout textoMarcador = new GlyphLayout();
    private int puntajeMostrado = -1;

    // --- AUDIO DE LA PARTIDA ---
    private Music musicaPartida;
    private Sound sonidoCienPuntos;
    private Sound sonidoAuto;
    private Sound sonidoPedido;
    private int ultimoHitoPuntaje = 0;

    // --- SISTEMA DE ESTADOS DEL JUEGO ---
    private enum EstadoJuego {
        JUGANDO,
        FIN_PARTIDA
    }
    private EstadoJuego estadoActual = EstadoJuego.JUGANDO;

    private ShapeRenderer dibujanteFormas;
    private Rectangle botonReiniciar;
    private Vector3 puntoPulsado;

    private final Array<Pedido> pedidos = new Array<>();
    private Texture texturaPedido;
    private Texture[] texturasObstaculos;
    private static final int INTERVALO_PEDIDOS = 100;
    private int siguientePedido = INTERVALO_PEDIDOS;
    private boolean hayPrimerObstaculo;
    private Preferences preferencias;
    private int record;
    private Rectangle botonMenu;
    private static final float PRIMER_INTERVALO = 3f;

    // --- SISTEMA DE CICLO DÍA / TARDE / NOCHE ---
    private static final int INTERVALO_CAMBIO = 700;
    private static final float RANGO_TRANSICION = 100f;

    private final Color fondoDia = new Color(0.5f, 0.8f, 1.0f, 1f);
    private final Color fondoTarde = new Color(0.85f, 0.45f, 0.25f, 1f);
    private final Color fondoNoche = new Color(0.05f, 0.05f, 0.2f, 1f);

    private final Color tinteDia = new Color(1.0f, 1.0f, 1.0f, 1f);
    private final Color tinteTarde = new Color(1.0f, 0.7f, 0.5f, 1f);
    private final Color tinteNoche = new Color(0.35f, 0.35f, 0.65f, 1f);

    private final Color fondoActual = new Color();
    private final Color tinteActual = new Color();

    public GameScreen(Main juego) {
        this.juego = juego;
    }

    @Override
    public void show() {
        loteDibujo = new SpriteBatch();
        dibujanteFormas = new ShapeRenderer();
        puntoPulsado = new Vector3();
        botonReiniciar = new Rectangle((ANCHO_MUNDO - 300f) / 2f, ALTO_MUNDO / 2f - 110f, 300f, 60f);
        botonMenu = new Rectangle(botonReiniciar.x, botonReiniciar.y - 74f, 300f, 60f);
        preferencias = Gdx.app.getPreferences("JuegoRappi");
        record = preferencias.getInteger("record", 0);
        texturasObstaculos = Obstaculos.cargarTexturas();
        texturaPedido = new Texture(Gdx.files.internal("Obstaculos/pedido.png"));

        camara = new OrthographicCamera();
        vista = new FitViewport(ANCHO_MUNDO, ALTO_MUNDO, camara);

        gestorPuntaje = new ScoreManager(ANCHO_MUNDO);
        piso = new Piso(40f, 4.0f);
        repartidor = new Player(piso, juego.getSelectedCharacter());

        patrullero = new Patrullero(piso, ANCHO_MUNDO);
        obstaculos = new Array<>();

        capasFondo = new Array<>();
        capasFondo.add(new ParallaxLayer("Fondo/1.png", 0.05f));
        capasFondo.add(new ParallaxLayer("Fondo/2.png", 0.15f));
        capasFondo.add(new ParallaxLayer("Fondo/3.png", 0.35f));
        capasFondo.add(new ParallaxLayer("Fondo/4.png", 0.70f));
        capasFondo.add(new ParallaxLayer("Fondo/5.png", 0.80f));

        if (Gdx.files.internal("8bitOperatorPlus-Regular.fnt").exists()) {
            fuente = new BitmapFont(Gdx.files.internal("8bitOperatorPlus-Regular.fnt"));
        } else {
            fuente = new BitmapFont();
        }
        fuente.setColor(Color.WHITE);

        medidasTexto = new GlyphLayout();

        if (Gdx.files.internal("Sonidos/score_100.mp3").exists()) {
            sonidoCienPuntos = Gdx.audio.newSound(Gdx.files.internal("Sonidos/score_100.mp3"));
        }

        sonidoAuto = Gdx.audio.newSound(Gdx.files.internal("Sonidos/auto_pasa.wav"));
        sonidoPedido = Gdx.audio.newSound(Gdx.files.internal("Sonidos/ñamñamñam.mp3"));
        iniciarMusicaJuego();
    }

    private void iniciarMusicaJuego() {
        if (Gdx.files.internal("Sonidos/game_music.mp3").exists()) {
            musicaPartida = Gdx.audio.newMusic(Gdx.files.internal("Sonidos/game_music.mp3"));
            musicaPartida.setLooping(true);
            musicaPartida.setVolume(0.4f);
            musicaPartida.play();
        }
    }

    @Override
    public void render(float segundosTranscurridos) {
        float avance = 0f;
        // --- 1. LÓGICA DE JUEGO ---
        if (estadoActual == EstadoJuego.JUGANDO) {
            avance = actualizarPartida(Math.min(segundosTranscurridos, 1f / 20f));

        } else {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                reiniciarJuego();
            } else if (Gdx.input.justTouched()) {
                puntoPulsado.set(Gdx.input.getX(), Gdx.input.getY(), 0);
                vista.unproject(puntoPulsado);

                if (botonReiniciar.contains(puntoPulsado.x, puntoPulsado.y)) {
                    reiniciarJuego();
                } else if (botonMenu.contains(puntoPulsado.x, puntoPulsado.y)) {
                    juego.setScreen(new MainMenu(juego));
                    return;
                }
            }
        }

        // --- 2. RENDERIZADO ---
        loteDibujo.setProjectionMatrix(camara.combined);

        Gdx.gl.glClearColor(fondoActual.r, fondoActual.g, fondoActual.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        loteDibujo.begin();

        for (ParallaxLayer capa : capasFondo) {
            capa.updateAndDraw(
                loteDibujo,
                avance,
                vista.getWorldWidth(),
                vista.getWorldHeight(),
                tinteActual
            );
        }

        piso.draw(loteDibujo, vista.getWorldWidth());

        // El carril determina la profundidad incluso durante el salto.
        for (int carril = 2; carril >= 0; carril--) {
            for (Obstaculos obstaculo : obstaculos) {
                if (obstaculo.getCarril() == carril) obstaculo.draw(loteDibujo);
            }
            for (Pedido pedido : pedidos) {
                if (pedido.carril == carril) pedido.draw(loteDibujo, texturaPedido);
            }
            patrullero.dibujarCarril(loteDibujo, carril);
            if (repartidor.getNivelActual() == carril) repartidor.draw(loteDibujo);
        }

        if (estadoActual == EstadoJuego.JUGANDO) {
            if (patrullero.obtenerFase() == Patrullero.Fase.ALERTA) {
                patrullero.dibujarAlerta(loteDibujo, ALTO_MUNDO);
                fuente.getData().setScale(2.5f);
                dibujarCentrado("PELIGRO", ALTO_MUNDO / 2f);
                fuente.getData().setScale(1f);
            } else if (patrullero.obtenerFase() == Patrullero.Fase.PERSECUCION) {
                dibujarCentrado("ESQUIVA   " + patrullero.segundosRestantes() + " s", ALTO_MUNDO - 40f);
            }
        }

        // Dibujar el puntaje
        int puntaje = gestorPuntaje.getScore();
        if (puntaje != puntajeMostrado) {
            textoMarcador.setText(fuente, "SCORE " + gestorPuntaje.getScoreFormateado());
            puntajeMostrado = puntaje;
        }
        fuente.draw(loteDibujo, textoMarcador, ANCHO_MUNDO - textoMarcador.width - 30f, ALTO_MUNDO - 30f);

        loteDibujo.end();

        // --- 3. OVERLAY DE GAME OVER ---
        if (estadoActual == EstadoJuego.FIN_PARTIDA) {
            dibujanteFormas.setProjectionMatrix(camara.combined);

            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            dibujanteFormas.begin(ShapeRenderer.ShapeType.Filled);
            dibujanteFormas.setColor(0.6f, 0.0f, 0.0f, 0.5f);
            dibujanteFormas.rect(0, 0, vista.getWorldWidth(), vista.getWorldHeight());

            dibujanteFormas.setColor(0.2f, 0.7f, 0.3f, 1f);
            dibujanteFormas.rect(botonReiniciar.x, botonReiniciar.y, botonReiniciar.width, botonReiniciar.height);
            dibujanteFormas.setColor(0.22f, 0.32f, 0.49f, 1f);
            dibujanteFormas.rect(botonMenu.x, botonMenu.y, botonMenu.width, botonMenu.height);
            dibujanteFormas.end();

            Gdx.gl.glDisable(GL20.GL_BLEND);

            loteDibujo.begin();
            // Medimos la fila con la fuente activa para respetar los márgenes.
            fuente.getData().setScale(1.2f);
            String resultados = "PUNTAJE " + gestorPuntaje.getScore() + "    RECORD " + record;
            medidasTexto.setText(fuente, resultados);
            float baseResultados = botonReiniciar.y + botonReiniciar.height + 40f;
            float topeResultados = baseResultados + medidasTexto.height;
            fuente.draw(loteDibujo, medidasTexto, (ANCHO_MUNDO - medidasTexto.width) / 2f, topeResultados);

            fuente.getData().setScale(2.5f);
            medidasTexto.setText(fuente, "GAME OVER");
            float topeTitulo = topeResultados + 80f + medidasTexto.height;
            fuente.draw(loteDibujo, medidasTexto, (ANCHO_MUNDO - medidasTexto.width) / 2f, topeTitulo);

            fuente.getData().setScale(1.2f);
            dibujarCentrado("REINTENTAR", botonReiniciar.y + botonReiniciar.height / 2f);
            dibujarCentrado("MENU PRINCIPAL", botonMenu.y + botonMenu.height / 2f);
            fuente.getData().setScale(1.0f);
            loteDibujo.end();
        }
    }

    private float actualizarPartida(float segundosTranscurridos) {
        float avance = gestorPuntaje.update(segundosTranscurridos);

        int puntajeActual = gestorPuntaje.getScore();
        if (puntajeActual >= ultimoHitoPuntaje + 100) {
            ultimoHitoPuntaje = (puntajeActual / 100) * 100;
            if (sonidoCienPuntos != null) {
                sonidoCienPuntos.play(0.7f);
            }
        }

        repartidor.update(segundosTranscurridos, patrullero.destinoJugador());
        piso.avanzar(avance, ANCHO_MUNDO);

        generarPedidoSiCorresponde();

        // Actualizar obstáculos y comprobar colisión por carril
        int carrilJugador = repartidor.getNivelActual();
        Rectangle zonaJugador = repartidor.getBounds();
        boolean pasoUnAuto = false;
        for (int i = obstaculos.size - 1; i >= 0; i--) {
            Obstaculos obstaculo = obstaculos.get(i);
            obstaculo.avanzar(avance);

            if (!patrullero.estaEmbistiendo(obstaculo) && carrilJugador == obstaculo.getCarril() && zonaJugador.overlaps(obstaculo.getBounds())) {
                activarGameOver();
            } else if (obstaculo.acabaDePasarCerca(zonaJugador.x, carrilJugador)) {
                pasoUnAuto = true;
            }

            if (obstaculo.estaFueraDePantalla()) {
                obstaculos.removeIndex(i);
            }
        }

        if (pasoUnAuto && estadoActual == EstadoJuego.JUGANDO) {
            sonidoAuto.play(0.55f);
        }

        for (int i = pedidos.size - 1; i >= 0; i--) {
            Pedido pedido = pedidos.get(i);
            pedido.zonaColision.x -= avance;
            if (estadoActual == EstadoJuego.JUGANDO && carrilJugador == pedido.carril
                    && zonaJugador.overlaps(pedido.zonaColision)) {
                gestorPuntaje.sumarPedido();
                sonidoPedido.play(0.8f);
                pedidos.removeIndex(i);
            } else if (pedido.zonaColision.x + pedido.zonaColision.width < 0f) {
                pedidos.removeIndex(i);
            }
        }
        if (estadoActual == EstadoJuego.JUGANDO) {
            Patrullero.Fase anterior = patrullero.obtenerFase();
            if (patrullero.actualizar(segundosTranscurridos, avance, gestorPuntaje.getScore(), repartidor, obstaculos)) {
                activarGameOver();
            } else if (musicaPartida != null && anterior != patrullero.obtenerFase()) {
                if (patrullero.obtenerFase() == Patrullero.Fase.ALERTA) musicaPartida.pause();
                if (patrullero.obtenerFase() == Patrullero.Fase.AUSENTE) musicaPartida.play();
            }
        }
        // El pedido puede activar la alerta en este mismo fotograma.
        if (estadoActual == EstadoJuego.JUGANDO && patrullero.obtenerFase() != Patrullero.Fase.ALERTA) {
            generarObstaculoSiCorresponde(segundosTranscurridos, avance);
        }
        actualizarAmbienteClimatico();
        return avance;
    }

    private void generarObstaculoSiCorresponde(float segundosTranscurridos, float avance) {
        // --- GENERACIÓN PROCEDURAL DE OBSTÁCULOS ---
        tiempoInicial += segundosTranscurridos;
        if (hayPrimerObstaculo) distanciaHastaObstaculo -= avance;

        if (tiempoInicial >= PRIMER_INTERVALO && distanciaHastaObstaculo <= 0f) {
            int carrilAleatorio = MathUtils.random(0, 2);
            if (pedidos.size > 0) {
                Pedido ultimo = pedidos.peek();
                if (ultimo.zonaColision.x + ultimo.zonaColision.width >= ANCHO_MUNDO && carrilAleatorio == ultimo.carril) {
                    carrilAleatorio = (carrilAleatorio + MathUtils.random(1, 2)) % 3;
                }
            }
            int tipo = MathUtils.random(texturasObstaculos.length - 1);
            Obstaculos nuevo = new Obstaculos(piso, carrilAleatorio, ANCHO_MUNDO, tipo, texturasObstaculos[tipo]);
            obstaculos.add(nuevo);
            hayPrimerObstaculo = true;
            distanciaHastaObstaculo += gestorPuntaje.distanciaEntreObstaculos(
                    nuevo.getAncho(), MathUtils.random(1f, 1.5f));
        }
    }

    private void generarPedidoSiCorresponde() {
        if (gestorPuntaje.getPuntosPorDistancia() >= siguientePedido) {
            int carrilPedido = MathUtils.random(0, 2);
            if (obstaculos.size > 0) {
                int carrilUltimo = obstaculos.peek().getCarril();
                carrilPedido = (carrilUltimo + MathUtils.random(1, 2)) % 3;
            }
            pedidos.add(new Pedido(piso, carrilPedido, ANCHO_MUNDO));
            siguientePedido += INTERVALO_PEDIDOS;
        }
    }

    private void dibujarCentrado(String texto, float centroY) {
        medidasTexto.setText(fuente, texto);
        fuente.draw(loteDibujo, medidasTexto, (ANCHO_MUNDO - medidasTexto.width) / 2f,
                centroY + medidasTexto.height / 2f);
    }

    private void activarGameOver() {
        if (estadoActual == EstadoJuego.JUGANDO) {
            estadoActual = EstadoJuego.FIN_PARTIDA;
            patrullero.detenerAudio();
            if (gestorPuntaje.getScore() > record) {
                record = gestorPuntaje.getScore();
                preferencias.putInteger("record", record);
                preferencias.flush();
            }
            if (musicaPartida != null) {
                musicaPartida.stop();
            }
        }
    }

    private void reiniciarJuego() {
        obstaculos.clear();
        pedidos.clear();
        hayPrimerObstaculo = false;
        siguientePedido = INTERVALO_PEDIDOS;
        patrullero.reiniciar();
        gestorPuntaje.reset();
        piso.reset();
        repartidor.reset();
        for (ParallaxLayer capa : capasFondo) capa.reset();
        distanciaHastaObstaculo = 0f;
        actualizarAmbienteClimatico();
        ultimoHitoPuntaje = 0;
        tiempoInicial = 0f;
        estadoActual = EstadoJuego.JUGANDO;

        if (musicaPartida != null) {
            musicaPartida.play();
        }
    }

    private void actualizarAmbienteClimatico() {
        int puntaje = gestorPuntaje.getScore();

        int faseActual = (puntaje / INTERVALO_CAMBIO) % 3;
        int siguienteFase = (faseActual + 1) % 3;

        float puntosEnFase = puntaje % INTERVALO_CAMBIO;

        Color colorFondoOrigen = getBgColor(faseActual);
        Color colorFondoDestino = getBgColor(siguienteFase);

        Color tinteOrigen = getTintColor(faseActual);
        Color tinteDestino = getTintColor(siguienteFase);

        if (puntosEnFase >= (INTERVALO_CAMBIO - RANGO_TRANSICION)) {
            float progreso = (puntosEnFase - (INTERVALO_CAMBIO - RANGO_TRANSICION)) / RANGO_TRANSICION;
            progreso = MathUtils.clamp(progreso, 0f, 1f);

            fondoActual.set(colorFondoOrigen).lerp(colorFondoDestino, progreso);
            tinteActual.set(tinteOrigen).lerp(tinteDestino, progreso);
        } else {
            fondoActual.set(colorFondoOrigen);
            tinteActual.set(tinteOrigen);
        }
    }

    private Color getBgColor(int fase) {
        switch (fase) {
            case 1: return fondoTarde;
            case 2: return fondoNoche;
            default: return fondoDia;
        }
    }

    private Color getTintColor(int fase) {
        switch (fase) {
            case 1: return tinteTarde;
            case 2: return tinteNoche;
            default: return tinteDia;
        }
    }

    @Override
    public void resize(int ancho, int alto) {
        vista.update(ancho, alto, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        patrullero.detenerAudio();
        if (musicaPartida != null) {
            musicaPartida.stop();
        }
    }

    @Override
    public void dispose() {
        loteDibujo.dispose();
        fuente.dispose();
        patrullero.liberar();
        repartidor.dispose();
        piso.dispose();
        dibujanteFormas.dispose();

        for (Texture textura : texturasObstaculos) textura.dispose();
        texturaPedido.dispose();

        for (ParallaxLayer capa : capasFondo) {
            capa.dispose();
        }
        sonidoAuto.dispose();
        sonidoPedido.dispose();
        if (sonidoCienPuntos != null) sonidoCienPuntos.dispose();
        if (musicaPartida != null) musicaPartida.dispose();
    }
}
