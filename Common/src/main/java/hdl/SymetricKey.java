package hdl;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class SymetricKey {
    public static SecretKey createKey() throws Exception {
        SecureRandom securerandom = new SecureRandom();
        KeyGenerator keygenerator = KeyGenerator.getInstance("AES");
        keygenerator.init(256, securerandom);
        SecretKey key = keygenerator.generateKey();
        return key;
    }

    public static byte[] encrypt(byte[] message, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher= Cipher.getInstance("AES/CBC/PKCS5PADDING"); 
        IvParameterSpec ivP = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE,key,ivP);
        return cipher.doFinal(message);
    }

    public static byte[] decrypt(byte[] cipherText, SecretKey key, byte[] iv) throws Exception{
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        IvParameterSpec ivP = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE,key,ivP);
        byte[] result = cipher.doFinal(cipherText);
        return result;
    } 
    public static byte[] createIV(){
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[16]; 
        random.nextBytes(iv);
        return iv;
    }

}
