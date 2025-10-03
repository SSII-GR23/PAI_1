package main;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CodigoMuestra {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("=== Inicializando servidor (demo) ===");
        initDatabase();

        // 1) crear usuarios de prueba (si no existen)
        if (registrarUsuario("usuarioPrueba", "password123")) {
            System.out.println("Usuario 'usuarioPrueba' creado para pruebas.");
        } else {
            System.out.println("Usuario 'usuarioPrueba' ya existe o no se pudo crear.");
        }

        // 2) Simular login y transferencias
        boolean acceso = manejoCredenciales("usuarioPrueba", "password123");
        System.out.println("¿Acceso concedido? " + acceso);

        // ---------------- PRUEBA TRANSFERENCIA SEGURA VALIDA ----------------
        String transferenciaBase = "ES8384:ES3476:1000";
        String mensajeSeguro = enviarTransferenciaSegura(transferenciaBase);
        System.out.println("\nMensaje seguro generado: " + mensajeSeguro);

        String[] datosTransferenciaSegura = mensajeSeguro.split(":");

        System.out.println("\n--- PRUEBA 1: Transferencia SEGURA y VÁLIDA ---");
        procesarComando(datosTransferenciaSegura, acceso, "usuarioPrueba");

        // ---------------- PRUEBA REPLAY ----------------
        System.out.println("\n--- PRUEBA 2: Ataque de Repetición (Replay Attack) ---");
        procesarComando(datosTransferenciaSegura, acceso, "usuarioPrueba");

        // ---------------- PRUEBA ALTERACIÓN ----------------
        System.out.println("\n--- PRUEBA 3: Ataque de Alteración de Mensaje (MAC Inválido) ---");
        String nonce = datosTransferenciaSegura[3];
        String mac = datosTransferenciaSegura[4];
        String[] datosAlterados = {"ES8384", "ES3476", "99999", nonce, mac};
        procesarComando(datosAlterados, acceso, "usuarioPrueba");

        System.out.println("\n=== Demo finalizado ===");
    

	}
	
	// =================== CONSTANTES / ESTADO =========================
    private static final String DB_FILE = "bbdd.db";
    private static final int SALT_BYTES = 16; // 128 bits
    private static final int NONCE_BYTES = 16; // 128 bits
	
	// =================== ATRIBUTOS ======================
    private static final byte[] SECRET_KEY = "ClaveSuperSecreta123!".getBytes(StandardCharsets.UTF_8);
    private static final Set<String> noncesEmpleados = new HashSet<>();
    private static Connection conn; // conexión a la base de datos
    


    /**
     * Comparación segura (tiempo constante) de MACs o hashes representados como hex.
     */
    private static boolean secureEqualsHex(String aHex, String bHex) {
        if (aHex == null || bHex == null) return false;
        byte[] a = utils.Parser.hexToBytes(aHex);
        byte[] b = utils.Parser.hexToBytes(bHex);
        return MessageDigest.isEqual(a, b);
    }    
 // =================== BBDD (SQLite) ==============================

    /**
     * Crea/abre la conexión SQLite
     */
    public static Connection createConnection(String dbFile) {
        try {
            String url = "jdbc:sqlite:" + dbFile;
            Connection c = DriverManager.getConnection(url);
            return c;
        } catch (SQLException e) {
            System.err.println("Error creando conexión: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Inicializa la BBDD si no existe: tablas usuarios y transacciones
     * No hace falta en principio 
     */
    public static void initDatabase() {
        conn = createConnection(DB_FILE);
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
        } catch (SQLException e) {
            throw new RuntimeException("Error creando tablas: " + e.getMessage(), e);
        }
    }

    /**
     * Registra un usuario con salt+hash. Devuelve true si creado, false si ya existe.
     */
    public static boolean registrarUsuario(String username, String password) {
        if (conn == null) initDatabase();

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
        byte[] salt = utils.Generators.salt(SALT_BYTES);
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
    public static boolean checkUser(String username, String password) {
        if (conn == null) initDatabase();
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
                return secureEqualsHex(storedHash, computed);
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
    public static boolean manejoCredenciales(String user, String passw) {
        System.out.printf("Intento de login -> user: %s%n", user);
        boolean ok = checkUser(user, passw);
        if (ok) {
            System.out.println("Login ok");
        } else {
            System.out.println("Las credenciales no son correctas");
        }
        return ok;
    }
    
 // =================== TRANSFERENCIAS SEGURAS =====================

    /**
     * Construye y devuelve un mensaje seguro: origen:destino:cantidad:nonce_hex:mac_hex
     * El MAC se calcula sobre "origen:destino:cantidad:nonce_hex".
     */
    public static String enviarTransferenciaSegura(String transferenciaBase) {
        // transferenciaBase ejemplo: "ES8384:ES3476:1000"
        byte[] nonceBytes = utils.Generators.nonce(NONCE_BYTES);
        String nonceHex = utils.Parser.bytesToHex(nonceBytes);

        String mensajeParaMac = transferenciaBase + ":" + nonceHex;
        String mac = utils.Generators.mac(mensajeParaMac, SECRET_KEY);
        if (mac == null) return null;

        String mensajeTotal = mensajeParaMac + ":" + mac;
        return mensajeTotal;
    }

    /**
     * Verifica MAC de forma segura.
     */
    public static boolean verificarMac(String transferenciaCompleta, String macRecibido) {
        // transferenciaCompleta debe ser "origen:destino:cantidad:nonce"
        String macCalculado = utils.Generators.mac(transferenciaCompleta, SECRET_KEY);
        return secureEqualsHex(macCalculado, macRecibido);
    }

    /**
     * Registra una transacción en la BBDD (sin validar cuentas/cantidades).
     */
    private static void registrarTransaccion(String origen, String destino, String cantidad, String nonce, String mac) {
        if (conn == null) initDatabase();
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

    /**
     * Manejo de transferencia (completa) que exige estar logueado y verificar nonce+mac.
     */
    public static void manejoTransferencia(String origen, String destino, String cantidad,
                                           String nonce, String mac, boolean logged) {
        System.out.println("\n*** INICIANDO VERIFICACIÓN DE TRANSFERENCIA ***");
        if (!logged) {
            System.out.println("El usuario no ha conseguido loguearse, no puede realizar esta operación");
            return;
        }

        // 1. comprobar si nonce ya usado (replay)
        if (noncesEmpleados.contains(nonce)) {
            System.err.println("ERROR: Nonce ya utilizado. Posible ataque de repetición.");
            return;
        }

        // 2. verificar mac
        String mensajeBase = String.join(":", origen, destino, cantidad, nonce);
        if (!verificarMac(mensajeBase, mac)) {
            System.err.println("ERROR: MAC inválido. El mensaje ha sido alterado o clave incorrecta.");
            System.out.println("MAC recibido: " + mac);
            String esperado =  utils.Generators.mac(mensajeBase, SECRET_KEY);
            System.out.println("MAC esperado: " + esperado);
            return;
        }

        // 3. éxito: registrar nonce y transacción
        noncesEmpleados.add(nonce);
        System.out.println("ÉXITO: Transferencia verificada y MAC válido.");
        System.out.println("Procesando transferencia de " + cantidad + " de " + origen + " a " + destino);
        registrarTransaccion(origen, destino, cantidad, nonce, mac);
    }

    // =================== PROCESADOR DE COMANDOS =====================

    /**
     * Procesa un comando separado en tokens (dataSplit).
     * Protocolos:
     * - [username, password] => login (len==2)
     * - [origen, destino, cantidad] => transferencia simple (len==3) => requiere logged boolean externo
     * - [origen, destino, cantidad, nonce, mac] => transferencia segura (len==5)
     *
     * Nota: Para el caso de demo, se asume logged==true cuando proceda. En la vida real habría sesión.
     */
    public static void procesarComando(String[] dataSplit, boolean logged, String userValido) {
        int len = dataSplit.length;

        if (len == 2) {
            System.out.println("Se ha recibido un intento de login");
            String user = dataSplit[0];
            String passw = dataSplit[1];
            manejoCredenciales(user, passw);

        } else if (len == 3) {
            System.out.println("Se ha recibido un intento de transferencia (Formato Antiguo)");
            String origen = dataSplit[0];
            String destino = dataSplit[1];
            String cantidad = dataSplit[2];
            if (logged) {
                System.out.println("El usuario está logueado, puede realizar esta operación");
                // No hay nonce/mac en este formato: registramos directamente
                registrarTransaccion(origen, destino, cantidad, null, null);
                System.out.println("Transferencia realizada (sin integridad MAC).");
            } else {
                System.out.println("El usuario no está logueado. No se permite transferencia.");
            }

        } else if (len == 5) {
            System.out.println("Se ha recibido un intento de transferencia SEGURA");
            String origen = dataSplit[0];
            String destino = dataSplit[1];
            String cantidad = dataSplit[2];
            String nonce = dataSplit[3];
            String mac = dataSplit[4];

            System.out.printf("Datos: Origen=%s, Destino=%s, Cantidad=%s, Nonce=%s, MAC=%s%n",
                    origen, destino, cantidad, nonce, mac);

            manejoTransferencia(origen, destino, cantidad, nonce, mac, logged);

        } else {
            System.out.println("Error: formato de comando incorrecto.");
        }
    }
    
    

}
