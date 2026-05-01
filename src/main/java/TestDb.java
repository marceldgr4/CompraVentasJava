import Infrastructure.logging.LoggerFactory;
import com.app.Config.AppConfig;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestDb {
    private static final Logger log = LoggerFactory.getLogger(TestDb.class);

    public static void main(String[] args) {
        // Obtenemos las credenciales ocultas desde el archivo .env
        String url = AppConfig.get("DB_URL");
        String user = AppConfig.get("DB_USERNAME");
        String pass = AppConfig.get("DB_PASSWORD");

        log.info("Probando conexión a: {} con usuario {}", url, user);
        try (Connection con = DriverManager.getConnection(url, user, pass)) {
            log.info("¡ÉXITO! Conexión establecida correctamente.");
            boolean isValid = con.isValid(5);
            log.info("Conexión válida: {}", isValid);
        } catch (SQLException e) {
            log.error("¡FALLÓ! Detalles del error al conectar a la base de datos", e);
        }
    }
}
