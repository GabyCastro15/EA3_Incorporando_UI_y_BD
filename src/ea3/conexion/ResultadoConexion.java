package ea3.conexion;

/**
 * Resultado de una prueba de conexión: lo que la interfaz necesita para mostrar al usuario.
 *
 * @param exitoso        true si la conexión se estableció correctamente
 * @param mensaje        mensaje principal para el usuario
 * @param detalleTecnico detalle técnico (metadatos de la BD o e.getMessage() del error)
 */
public record ResultadoConexion(boolean exitoso, String mensaje, String detalleTecnico) {

    public static ResultadoConexion exito(String detalleTecnico) {
        return new ResultadoConexion(true, "Conexión exitosa", detalleTecnico);
    }

    public static ResultadoConexion falloDriver(String detalleTecnico) {
        return new ResultadoConexion(false, "Fallo del driver JDBC", detalleTecnico);
    }

    public static ResultadoConexion falloConexion(String detalleTecnico) {
        return new ResultadoConexion(false, "Fallo de conexión", detalleTecnico);
    }

    public static ResultadoConexion datosInvalidos(String detalleTecnico) {
        return new ResultadoConexion(false, "Datos de conexión inválidos", detalleTecnico);
    }
}
