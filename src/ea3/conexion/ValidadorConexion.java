package ea3.conexion;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Lógica de prueba de conexión JDBC, separada de la interfaz gráfica.
 * No depende de Swing: puede reutilizarse desde consola, pruebas u otra interfaz.
 */
public class ValidadorConexion {

    private static final int TIEMPO_ESPERA_SEGUNDOS = 5;

    public ResultadoConexion probar(ConfiguracionConexion configuracion) {
        try {
            cargarDriver(configuracion.getUrl());
            return abrirYVerificar(configuracion);
        } catch (ClassNotFoundException e) {
            return ResultadoConexion.falloDriver("No se encontró la clase del driver: " + e.getMessage()
                    + System.lineSeparator() + "Agregue el .jar del driver a la carpeta lib/ (classpath).");
        } catch (SQLException e) {
            String estadoSql = e.getSQLState() == null ? "no informado" : e.getSQLState();
            return ResultadoConexion.falloConexion(e.getMessage()
                    + System.lineSeparator() + "SQLState: " + estadoSql
                    + " | Código de error: " + e.getErrorCode());
        } finally {
            configuracion.limpiarContrasena();
        }
    }

    /**
     * Carga explícita del driver con Class.forName: si el .jar no está en el classpath
     * se lanza ClassNotFoundException. Para motores no registrados se delega en DriverManager.
     */
    private void cargarDriver(String url) throws ClassNotFoundException {
        Optional<MotorBaseDatos> motor = MotorBaseDatos.desdeUrl(url);
        if (motor.isPresent()) {
            Class.forName(motor.get().getClaseDriver());
        }
    }

    /** try-with-resources: la conexión se cierra siempre, tanto si tiene éxito como si falla. */
    private ResultadoConexion abrirYVerificar(ConfiguracionConexion configuracion) throws SQLException {
        DriverManager.setLoginTimeout(TIEMPO_ESPERA_SEGUNDOS);
        try (Connection conexion = obtenerConexion(configuracion)) {
            if (!conexion.isValid(TIEMPO_ESPERA_SEGUNDOS)) {
                throw new SQLException("La conexión se abrió pero no respondió a la validación.");
            }
            return ResultadoConexion.exito(describir(conexion.getMetaData()));
        }
    }

    private Connection obtenerConexion(ConfiguracionConexion configuracion) throws SQLException {
        if (!configuracion.tieneCredenciales()) {
            return DriverManager.getConnection(configuracion.getUrl());
        }
        return DriverManager.getConnection(configuracion.getUrl(), configuracion.getUsuario(),
                new String(configuracion.getContrasena()));
    }

    private String describir(DatabaseMetaData metadatos) throws SQLException {
        return String.join(System.lineSeparator(),
                "Motor: " + metadatos.getDatabaseProductName() + " " + metadatos.getDatabaseProductVersion(),
                "Driver: " + metadatos.getDriverName() + " " + metadatos.getDriverVersion(),
                "URL: " + metadatos.getURL());
    }
}
