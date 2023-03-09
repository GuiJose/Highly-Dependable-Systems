package hdl;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HMAC {
    private static final String key = "KeyToHMAC";
    public static String createHMAC(String message) throws Exception{
            // convert the key string to a byte array
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            // create a SecretKeySpec object from the key byte array and SHA-256 algorithm
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "HmacSHA256");
            // create an HMAC instance and initialize it with the key spec
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(keySpec);
            byte[] hmacBytes = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            String HMACString = Base64.getEncoder().encodeToString(hmacBytes);
            return HMACString; 
        
    }
}
