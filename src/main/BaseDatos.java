package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// =================== BBDD (SQLite) ==============================
public class BaseDatos {
	
	// =================== CONSTANTES / ESTADO =========================
    private static final String DB_FILE = "bbdd.db";
    private static Connection conn; // conexión a la base de datos
    
    /**
     * Inicializa la BBDD si no existe: tablas usuarios y transacciones
     */
    public static void init() {
        conn = conexion(DB_FILE);
        if (conn == null) {
            throw new IllegalStateException("No se pudo abrir la BBDD");
        }
        String createUsuarios = "CREATE TABLE IF NOT EXISTS usuarios (" +
                "username TEXT PRIMARY KEY," +
                "password_hash TEXT NOT NULL," +
                "salt TEXT NOT NULL" +
                ");";
        String createTrans = "CREATE TABLE IF NOT EXISTS transacciones (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "origen TEXT," +
                "destino TEXT," +
                "cantidad TEXT," +
                "nonce TEXT," +
                "mac TEXT," +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ");";
        try (Statement st = conn.createStatement()) {
            st.execute(createUsuarios);
            st.execute(createTrans);
            System.out.println("Base de datos inicializada correctamente.");
        } catch (SQLException e) {
            throw new RuntimeException("Error creando tablas: " + e.getMessage(), e);
        }
    }
    
    public static void initDemo() {
    	init();
    	
        BaseDatos.userSign("Paco Flores", "paquito33");
        BaseDatos.userSign("Alberto Chicote", "PesadillaCocina78");
        BaseDatos.userSign("David Bisbal", "AveMaria45");
    }

    /**
     * Crea/abre la conexión SQLite
     */
    public static Connection conexion(String dbFile) {
        try {
            String url = "jdbc:sqlite:" + dbFile;
            Connection c = DriverManager.getConnection(url);
            return c;
        } catch (SQLException e) {
            System.err.println("Error creando conexión: " + e.getMessage());
            System.out.println("¿Has añadido la dependencia de SQLite en el pom.xml?");
            return null;
        }
    }
    
    
    /**
     * Registra un usuario con salt+hash. Devuelve true si creado, false si ya existe.
     */
    public static boolean userSign(String username, String password) {
        if (conn == null) init();

        String check = "SELECT username FROM usuarios WHERE username = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error consultando usuario: " + e.getMessage());
            return false;
        }

        // generar salt y hash
        byte[] salt = utils.Generators.salt(Main.SALT_BYTES);
        String saltHex = utils.Parser.bytesToHex(salt);
        String hash = utils.Generators.hashWithSalt(password, salt);

        String insert = "INSERT INTO usuarios(username, password_hash, salt) VALUES(?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insert)) {
            ps.setString(1, username);
            ps.setString(2, hash);
            ps.setString(3, saltHex);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error insertando usuario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica credenciales contra la BBDD. Devuelve true si coincide.
     */
    public static boolean user(String username, String password) {
        if (conn == null) init();
        String sql = "SELECT password_hash, salt FROM usuarios WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                String storedHash = rs.getString("password_hash");
                String saltHex = rs.getString("salt");
                byte[] salt = utils.Parser.hexToBytes(saltHex);
                String computed = utils.Generators.hashWithSalt(password, salt);
                // comparación segura
                return utils.Parser.equalsHex(storedHash, computed);
            }
        } catch (SQLException e) {
            System.err.println("Error verificando usuario: " + e.getMessage());
            return false;
        }
    }
    
    // =================== LOGIN / LOGOUT =========================

    /**
     * Manejo de credenciales: intenta login. Devuelve true si ok.
     */
    public static boolean userLogin(String user, String passw) {
        System.out.printf("Intento de login -> user: %s%n", user);
        boolean ok = user(user, passw);
        if (ok) {
            System.out.println("Login ok");
        } else {
            System.out.println("Las credenciales no son correctas");
        }
        return ok;
    }
    
    // =================== TRANSFERENCIAS SEGURAS =====================
    /**
     * Verifica MAC de forma segura.
     */
    public static boolean mac(String transferenciaCompleta, String macRecibido) {
        // transferenciaCompleta debe ser "origen:destino:cantidad:nonce"
        String macCalculado = utils.Generators.mac(transferenciaCompleta, Main.SECRET_KEY);
        return utils.Parser.equalsHex(macCalculado, macRecibido);
    }

    /**
     * Registra una transacción en la BBDD (sin validar cuentas/cantidades).
     */
    public static void transaccion(String origen, String destino, String cantidad, String nonce, String mac) {
        if (conn == null) init();
        String insert = "INSERT INTO transacciones(origen, destino, cantidad, nonce, mac) VALUES(?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insert)) {
            ps.setString(1, origen);
            ps.setString(2, destino);
            ps.setString(3, cantidad);
            ps.setString(4, nonce);
            ps.setString(5, mac);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al registrar transacción: " + e.getMessage());
        }
    }
}
