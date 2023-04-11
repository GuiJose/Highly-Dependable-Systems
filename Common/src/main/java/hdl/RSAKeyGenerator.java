package hdl;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

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

    public static PublicKey readPublic(String keyPath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] encoded;
        try (FileInputStream fis = new FileInputStream(keyPath)) {
            encoded = new byte[fis.available()];
            fis.read(encoded);
        }
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        return keyFactory.generatePublic(keySpec);
    }

    public static PrivateKey readPrivate(String keyPath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] encoded;
        try (FileInputStream fis = new FileInputStream(keyPath)) {
            encoded = new byte[fis.available()];
            fis.read(encoded);
        }
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");  
        
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return keyFactory.generatePrivate(keySpec);
    }
}
