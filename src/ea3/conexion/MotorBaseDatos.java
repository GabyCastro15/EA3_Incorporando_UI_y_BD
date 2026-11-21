package ea3.conexion;

import java.util.Arrays;
import java.util.Optional;

/**
 * Motores de base de datos soportados y la clase de su driver JDBC.
 * Permite deducir el driver a partir del prefijo de la URL de conexión.
 */
public enum MotorBaseDatos {

    SQLITE("jdbc:sqlite:", "org.sqlite.JDBC"),
    MYSQL("jdbc:mysql:", "com.mysql.cj.jdbc.Driver"),
    MARIADB("jdbc:mariadb:", "org.mariadb.jdbc.Driver"),
    POSTGRESQL("jdbc:postgresql:", "org.postgresql.Driver"),
    SQL_SERVER("jdbc:sqlserver:", "com.microsoft.sqlserver.jdbc.SQLServerDriver"),
    ORACLE("jdbc:oracle:", "oracle.jdbc.OracleDriver"),
    H2("jdbc:h2:", "org.h2.Driver");

    private final String prefijoUrl;
    private final String claseDriver;

    MotorBaseDatos(String prefijoUrl, String claseDriver) {
        this.prefijoUrl = prefijoUrl;
        this.claseDriver = claseDriver;
    }

    public String getClaseDriver() {
        return claseDriver;
    }

    public static Optional<MotorBaseDatos> desdeUrl(String url) {
        String urlNormalizada = url.toLowerCase();
        return Arrays.stream(values())
                .filter(motor -> urlNormalizada.startsWith(motor.prefijoUrl))
                .findFirst();
    }
}
