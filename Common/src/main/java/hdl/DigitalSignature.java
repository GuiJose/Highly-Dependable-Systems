package hdl;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

public class DigitalSignature {

    public static byte[] CreateSignature( byte[] message, PrivateKey Key) throws Exception{
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(Key);
        signature.update(message);
        return signature.sign();
    }
    

    public static boolean VerifySignature(byte[] message,byte[] signatureToVerify, PublicKey key) throws Exception{
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(key);
        signature.update(message);
        return signature.verify(signatureToVerify);
    } 
}
