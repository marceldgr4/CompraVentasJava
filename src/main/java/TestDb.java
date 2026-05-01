import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestDb {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://aws-1-us-west-2.pooler.supabase.com:6543/postgres?sslmode=require&prepareThreshold=0";
        String user = "postgres.vnalpnittampdhjajkbm";
        String pass = "CompraVentas2";

        System.out.println("Probando conexión a: " + url + " con usuario " + user);
        try (Connection con = DriverManager.getConnection(url, user, pass)) {
            System.out.println("¡ÉXITO! Conexión establecida en el puerto 6543.");
            boolean isValid = con.isValid(5);
            System.out.println("Conexión válida: " + isValid);
        } catch (SQLException e) {
            System.err.println("¡FALLÓ! Detalles del error:");
            e.printStackTrace();
        }
    }
}
