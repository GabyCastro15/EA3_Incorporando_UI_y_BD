package ea3.ui;

import ea3.conexion.ConfiguracionConexion;
import ea3.conexion.ResultadoConexion;
import ea3.conexion.ValidadorConexion;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

/**
 * Ventana principal (JFrame) para probar una conexión JDBC.
 * Construye la interfaz, valida los datos ingresados y delega la conexión en {@link ValidadorConexion}.
 */
public final class ConexionTester extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final String URL_EJEMPLO = "jdbc:sqlite:inventario.db";
    private static final Color COLOR_EXITO = new Color(0, 128, 0);
    private static final Color COLOR_ERROR = new Color(200, 0, 0);
    private static final Color COLOR_INFO = Color.DARK_GRAY;
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final transient ValidadorConexion validadorConexion;

    private final JTextField campoUrl = new JTextField(URL_EJEMPLO, 30);
    private final JTextField campoUsuario = new JTextField(30);
    private final JPasswordField campoContrasena = new JPasswordField(30);
    private final JButton botonProbar = new JButton("Probar conexión");
    private final JTextArea areaResultado = new JTextArea(10, 50);

    public ConexionTester(ValidadorConexion validadorConexion) {
        super("EA3 - Probador de conexión JDBC");
        this.validadorConexion = validadorConexion;
        configurarVentana();
        configurarEventos();
    }

    private void configurarVentana() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panelPrincipal.add(construirPanelFormulario(), BorderLayout.NORTH);
        panelPrincipal.add(construirPanelResultado(), BorderLayout.CENTER);
        panelPrincipal.add(construirPanelAcciones(), BorderLayout.SOUTH);

        setContentPane(panelPrincipal);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(560, 420));
        pack();
        setLocationRelativeTo(null);
        getRootPane().setDefaultButton(botonProbar);
    }

    private JPanel construirPanelFormulario() {
        JPanel panelFormulario = new JPanel(new GridLayout(3, 2, 8, 8));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Configuración de la base de datos"));

        campoUsuario.setToolTipText("Dejar vacío si es SQLite");
        campoContrasena.setToolTipText("Dejar vacío si es SQLite");

        panelFormulario.add(new JLabel("URL de conexión:"));
        panelFormulario.add(campoUrl);
        panelFormulario.add(new JLabel("Usuario (vacío en SQLite):"));
        panelFormulario.add(campoUsuario);
        panelFormulario.add(new JLabel("Contraseña (vacío en SQLite):"));
        panelFormulario.add(campoContrasena);
        return panelFormulario;
    }

    private JScrollPane construirPanelResultado() {
        areaResultado.setEditable(false);
        areaResultado.setLineWrap(true);
        areaResultado.setWrapStyleWord(true);
        areaResultado.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
        mostrarMensaje("Ingrese los datos y presione \"Probar conexión\".", COLOR_INFO);

        JScrollPane panelDesplazable = new JScrollPane(areaResultado);
        panelDesplazable.setBorder(BorderFactory.createTitledBorder("Resultado de la prueba"));
        return panelDesplazable;
    }

    private JPanel construirPanelAcciones() {
        JPanel panelAcciones = new JPanel(new BorderLayout());
        panelAcciones.add(botonProbar, BorderLayout.EAST);
        return panelAcciones;
    }

    private void configurarEventos() {
        botonProbar.addActionListener(this::alPresionarProbarConexion);
    }

    /** ActionListener del botón: solo coordina; la lógica JDBC vive en ValidadorConexion. */
    private void alPresionarProbarConexion(ActionEvent evento) {
        ConfiguracionConexion configuracion;
        try {
            configuracion = leerConfiguracion();
        } catch (IllegalArgumentException e) {
            mostrarResultado(ResultadoConexion.datosInvalidos(e.getMessage()));
            return;
        }
        ejecutarPruebaEnSegundoPlano(configuracion);
    }

    private ConfiguracionConexion leerConfiguracion() {
        return new ConfiguracionConexion(campoUrl.getText(), campoUsuario.getText(),
                campoContrasena.getPassword());
    }

    /**
     * La conexión puede tardar (red, timeout), por eso se ejecuta fuera del hilo de eventos
     * de Swing (EDT) con SwingWorker, evitando que la ventana se congele.
     */
    private void ejecutarPruebaEnSegundoPlano(ConfiguracionConexion configuracion) {
        botonProbar.setEnabled(false);
        mostrarMensaje("Probando conexión con " + configuracion.getUrl() + " ...", COLOR_INFO);

        new SwingWorker<ResultadoConexion, Void>() {
            @Override
            protected ResultadoConexion doInBackground() {
                return validadorConexion.probar(configuracion);
            }

            @Override
            protected void done() {
                try {
                    mostrarResultado(get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    mostrarResultado(ResultadoConexion.falloConexion("La prueba fue interrumpida."));
                } catch (ExecutionException e) {
                    mostrarResultado(ResultadoConexion.falloConexion(e.getCause().toString()));
                } finally {
                    botonProbar.setEnabled(true);
                }
            }
        }.execute();
    }

    private void mostrarResultado(ResultadoConexion resultado) {
        String texto = "[" + LocalTime.now().format(FORMATO_HORA) + "] " + resultado.mensaje()
                + System.lineSeparator() + System.lineSeparator() + resultado.detalleTecnico();
        mostrarMensaje(texto, resultado.exitoso() ? COLOR_EXITO : COLOR_ERROR);
    }

    private void mostrarMensaje(String texto, Color color) {
        areaResultado.setForeground(color);
        areaResultado.setText(texto);
        areaResultado.setCaretPosition(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            aplicarAparienciaDelSistema();
            new ConexionTester(new ValidadorConexion()).setVisible(true);
        });
    }

    private static void aplicarAparienciaDelSistema() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException e) {
            // Si falla se mantiene la apariencia por defecto de Swing (Metal); no es un error crítico.
        }
    }
}
