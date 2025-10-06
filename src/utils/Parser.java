package utils;

import java.security.MessageDigest;

public class Parser {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    
    /**
     * Parsea el dato de bytes a hexadecimal
     * @param bytes
     * @return
     */
	public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

	/**
	 * Parsea de hexadecimal a bytes
	 * @param hex
	 * @return
	 */
	public static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] out = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            out[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return out;
    }
	
	/**
	 * Comparación segura (tiempo constante) de MACs o hashes representados como hex.
	 * @param aHex
	 * @param bHex
	 * @return
	 */
    public static boolean equalsHex(String aHex, String bHex) {
        if (aHex == null || bHex == null) return false;
        byte[] a = utils.Parser.hexToBytes(aHex);
        byte[] b = utils.Parser.hexToBytes(bHex);
        return MessageDigest.isEqual(a, b);
    }    

}
