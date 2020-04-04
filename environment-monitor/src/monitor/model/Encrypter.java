package monitor.model;

import java.security.Key;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

   public class Encrypter {
	   
		static Logger logger = Logger.getLogger(Encrypter.class.getName());  

        private static final String algorithm = "DESede";
        private Key key;
        private Cipher cipher;

        boolean generateNewKey = false;
        String base64EncodedKey = "BN/g73PH3NPVV39MT1LaJbkV8dpSXnky";
        
        public Encrypter() {
			try {
				if (generateNewKey) {
					key = KeyGenerator.getInstance(algorithm).generateKey();
					BASE64Encoder base64Encoder = new BASE64Encoder();				
					System.out.println(base64Encoder.encode(key.getEncoded()));
				} else {
				    BASE64Decoder base64Decoder = new BASE64Decoder();
				    byte[] keyBytes = base64Decoder.decodeBuffer(base64EncodedKey);
				    key = new SecretKeySpec(keyBytes, algorithm);
				}
				cipher = Cipher.getInstance(algorithm);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Exception initialising Encrypter.", e);
			}
        }

        public static void main(String[] args) throws Exception {
        	Encrypter encrypter = new Encrypter();
            String input = "my test text";            
            System.out.println("Entered: " + input);
            byte[] encrypted = encrypter.encryptToByteArray(input);
            System.out.println("Recovered: " + encrypter.decryptFromByteArray(encrypted));
        }

        public String encrypt(String input) {
        	String result = null;
        	try {
        		result =  new BASE64Encoder().encode(encryptToByteArray(input));
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Cound not encrypt: " + input, e);			
			}
			return result;
        }

        public String decrypt(String base64Encoded) {
        	if (base64Encoded == null) {
        		return "null";
        	}
        	String result = null;
        	try {
        		byte[] encryptionBytes = new BASE64Decoder().decodeBuffer(base64Encoded);
        		result =  decryptFromByteArray(encryptionBytes);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Cound not decrypt: " + base64Encoded, e);			
			}
			return result;
        }
        
        
        private byte[] encryptToByteArray(String input) throws Exception  {
	        cipher.init(Cipher.ENCRYPT_MODE, key);
	        byte[] inputBytes = input.getBytes();
	        return cipher.doFinal(inputBytes);
        }

        private String decryptFromByteArray(byte[] encryptionBytes) throws Exception {
	        cipher.init(Cipher.DECRYPT_MODE, key);
	        byte[] recoveredBytes = cipher.doFinal(encryptionBytes);
	        String recovered = new String(recoveredBytes);
	        return recovered;
      }        
        
   }