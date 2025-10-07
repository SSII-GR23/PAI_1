package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import auth.Login;
import auth.Signin;
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
                "password_hash TEXT NOT NULL" +
                ");";
        String createTrans = "CREATE TABLE IF NOT EXISTS transacciones (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "hash TEXT," +
                "nonce TEXT," +
                "mac TEXT," +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ");";
        
        String createNonceTable = "CREATE TABLE IF NOT EXISTS nonces (" +
                "nonce TEXT PRIMARY KEY" +
                ");";

        
        try (Statement st = conn.createStatement()) {
            st.execute(createUsuarios);
            st.execute(createTrans);
            st.execute(createNonceTable);
            System.out.println("Base de datos inicializada correctamente.");
        } catch (SQLException e) {
            throw new RuntimeException("Error creando tablas: " + e.getMessage(), e);
        }
    }
    
    public static void initDemo() {
    	reset();

    	Signin user1 = new Signin("prueba1", "prueba");
    	Signin user2 = new Signin("prueba2", "prueba");
    	Signin user3 = new Signin("prueba3", "prueba");
    	
        BaseDatos.userSign(user1.user, user1.hashpassword);
        BaseDatos.userSign(user2.user, user2.hashpassword);
        BaseDatos.userSign(user3.user, user3.hashpassword);
        System.out.println("==============================");
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

        System.out.println("================");
        System.out.println("Usuario:  " + username);
        System.out.println("Contraseña:  " + password);
        
        String insert = "INSERT INTO usuarios(username, password_hash) VALUES(?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insert)) {
            ps.setString(1, username);
            ps.setString(2, password);
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
        String sql = "SELECT password_hash FROM usuarios WHERE username = ?";
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
    public static void transaccion(String message, String nonce, String mac) {
        if (conn == null) init();
        String insert = "INSERT INTO transacciones(hash, nonce, mac) VALUES(?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insert)) {
        	ps.setString(1, message);
        	ps.setString(2, nonce);
            ps.setString(3, mac);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al registrar transacción: " + e.getMessage());
        }
    }
    
    public static void reset() {
        try {
            // Cierra conexión si está abierta
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
            
            // Borra el archivo físico
            java.io.File file = new java.io.File(DB_FILE);
            if (file.exists() && file.delete()) {
                System.out.println("Archivo de base de datos eliminado correctamente.");
            } else {
                System.err.println("No se pudo eliminar el archivo o no existía.");
            }

            // Vuelve a inicializar
            init();
            
        } catch (SQLException e) {
            System.err.println("Error cerrando conexión: " + e.getMessage());
        }
    }
    
    /**
     * Verifica si un NONCE ya existe en la tabla nonces.
     * Devuelve true si existe, false si no.
     */
    public static boolean nonceExiste(String nonce) {
        if (conn == null) init(); // Asegura que la conexión esté abierta
        
        String sql = "SELECT 1 FROM nonces WHERE nonce = ? LIMIT 1";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nonce);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // true si encontró una fila, false si no
            }
        } catch (SQLException e) {
            System.err.println("Error verificando nonce: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Inserta un NONCE en la base de datos si no existe.
     * Devuelve true si fue insertado, false si ya estaba.
     */
    public static boolean registrarNonce(String nonce) {
        if (conn == null) init();
        
        if (nonceExiste(nonce)) {
            System.out.println("El nonce ya existe en la base de datos.");
            return false;
        }
        
        String insert = "INSERT INTO nonces(nonce) VALUES(?)";
        
        try (PreparedStatement ps = conn.prepareStatement(insert)) {
            ps.setString(1, nonce);
            ps.executeUpdate();
            System.out.println("Nonce registrado correctamente.");
            return true;
        } catch (SQLException e) {
            System.err.println("Error insertando nonce: " + e.getMessage());
            return false;
        }
    }


}
