package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import utils.Generators;
import utils.Parser;

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
    	
        BaseDatos.userSign("Paco Flores", "paquito33",Generators.salt(Main.SALT_BYTES));
        BaseDatos.userSign("Alberto Chicote", "PesadillaCocina78",Generators.salt(Main.SALT_BYTES));
        BaseDatos.userSign("David Bisbal", "AveMaria45",Generators.salt(Main.SALT_BYTES));
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
    public static boolean userSign(String username, String password, byte[] salt) {
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

        System.out.println("Contraseña:  " + password);
        
        String insert = "INSERT INTO usuarios(username, password_hash, salt) VALUES(?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insert)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, Parser.bytesToHex(salt));
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

                // comparación segura
                boolean equals = utils.Parser.equalsHex(storedHash, password);
                
                System.out.println(String.format("Credenciales del usuario: \t%s", username));
                System.out.println(String.format("Comparación de claves base de datos e input: %n%s %n%s", storedHash, password));
                System.err.println("Son iguales: " + equals);
                
                return equals;
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
        System.out.printf("============Intento de login============%n user: %s%npwd: %s%n", user, passw);
        boolean res = user(user, passw);
        
        if (res) {
            System.out.println("Login ok");
        } else {
            System.err.println("Las credenciales no son correctas o el usuario no existe");
        }
        return res;
    }
    
    public static boolean userExist(String user) {
        if (conn == null) init(); // inicializa la BD si aún no está

        String sql = "SELECT 1 FROM usuarios WHERE username = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user);
            try (ResultSet rs = ps.executeQuery()) {
                // Si rs.next() devuelve true, el usuario existe
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error verificando existencia de usuario: " + e.getMessage());
            return false;
        }
    }

    
    /**
     * Devuelve el salt del usuario en bytes.
     * Retorna null si el usuario no existe o hay un error.
     */
    public static byte[] getUserSalt(String username) {
        if (conn == null) init(); // inicializa la BD si es necesario
        
        String sql = "SELECT salt FROM usuarios WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null; // usuario no encontrado
                String saltHex = rs.getString("salt");
                return utils.Parser.hexToBytes(saltHex); // convertir de hex a byte[]
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo salt del usuario: " + e.getMessage());
            return null;
        }
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
