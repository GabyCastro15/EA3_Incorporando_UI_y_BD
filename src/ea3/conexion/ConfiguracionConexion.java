package ea3.conexion;

import java.util.Arrays;

/**
 * Datos de conexión ingresados por el usuario, validados al construirse.
 * La contraseña se guarda como char[] (no como String) para poder borrarla de memoria después de usarla.
 */
public final class ConfiguracionConexion {

    private static final String PREFIJO_JDBC = "jdbc:";

    private final String url;
    private final String usuario;
    private final char[] contrasena;

    public ConfiguracionConexion(String url, String usuario, char[] contrasena) {
        this.url = url == null ? "" : url.trim();
        this.usuario = usuario == null ? "" : usuario.trim();
        this.contrasena = contrasena == null ? new char[0] : contrasena;
        validar();
    }

    private void validar() {
        if (url.isEmpty()) {
            throw new IllegalArgumentException("La URL de conexión es obligatoria.");
        }
        if (!url.toLowerCase().startsWith(PREFIJO_JDBC)) {
            throw new IllegalArgumentException("La URL debe iniciar con '" + PREFIJO_JDBC
                    + "' (ejemplo: jdbc:sqlite:inventario.db).");
        }
    }

    public String getUrl() {
        return url;
    }

    public String getUsuario() {
        return usuario;
    }

    /** Solo visible dentro del paquete: la contraseña no se expone a la interfaz. */
    char[] getContrasena() {
        return contrasena;
    }

    public boolean tieneCredenciales() {
        return !usuario.isEmpty() || contrasena.length > 0;
    }

    /** Sobrescribe la contraseña en memoria una vez usada. */
    public void limpiarContrasena() {
        Arrays.fill(contrasena, '\0');
    }
}
