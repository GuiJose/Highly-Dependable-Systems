package hdl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.PrivateKey;
import java.util.Arrays;

public class ByteArraysOperations {
    public static byte[] SerializeObject(Object obj) throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        return baos.toByteArray();
    }

    public static byte[] signMessage(byte[] message, PrivateKey privKey) throws Exception{
        byte[] signature = DigitalSignature.CreateSignature(message, privKey);
        byte[] signedMessage = Arrays.copyOf(message, message.length + signature.length);
        System.arraycopy(signature, 0, signedMessage, message.length, signature.length);
        return signedMessage;
    }
}
