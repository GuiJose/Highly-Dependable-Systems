package hdl;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

public class RSAKeyGenerator {

    public static void write(int id, String mode) throws GeneralSecurityException, IOException {
        String privKeyPath;
        String pubKeyPath;
        if(mode.equals("s")){
            privKeyPath = "../Server/resources/S" + id + "private.key";
            pubKeyPath = "../Common/resources/S" + id + "public.key";
        }
        else{
            privKeyPath = "../User/resources/U" + id + "private.key";
            pubKeyPath = "../Common/resources/U" + id + "public.key";
        } 
      
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        SecureRandom secureRandom  = new SecureRandom();
        keyGen.initialize(4096, secureRandom);
        KeyPair keys = keyGen.generateKeyPair();
        
        PrivateKey privKey = keys.getPrivate();
        byte[] privKeyEncoded = privKey.getEncoded();

        PublicKey pubKey = keys.getPublic();
        byte[] pubKeyEncoded = pubKey.getEncoded();

        try (FileOutputStream privFos = new FileOutputStream(privKeyPath)) {
            privFos.write(privKeyEncoded);
        }

        try (FileOutputStream pubFos = new FileOutputStream(pubKeyPath)) {
            pubFos.write(pubKeyEncoded);
        }
    }

    public static Key read(String keyPath, String type) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        System.out.println("Reading key from file " + keyPath + " ...");
        byte[] encoded;
        try (FileInputStream fis = new FileInputStream(keyPath)) {
            encoded = new byte[fis.available()];
            fis.read(encoded);
        }
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        if (type.equals("pub") ){
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
            return keyFactory.generatePublic(keySpec);
        }

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return keyFactory.generatePrivate(keySpec);
    }


    public static byte[] encrypt(String message, PrivateKey key) throws Exception {
        Cipher cipher= Cipher.getInstance("RSA"); 
        cipher.init(Cipher.ENCRYPT_MODE,key);
        return cipher.doFinal(message.getBytes());
    }


    public static String decrypt(byte[] cipherText, PrivateKey key) throws Exception{
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE,key);
        byte[] result = cipher.doFinal(cipherText);
        return new String(result);
    } 

}