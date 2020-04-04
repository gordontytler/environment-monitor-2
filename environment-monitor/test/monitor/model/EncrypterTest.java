package monitor.model;

import java.util.logging.Logger;

import junit.framework.Assert;
import org.junit.Test;


public class EncrypterTest {

	static Logger logger = Logger.getLogger(EncrypterTest.class.getName());	
	public static final String password = "you will never guess";
	
	@Test
	public void testEncryptAndDecrypt() throws Exception {
		Encrypter encrypter = new Encrypter();
		String encodedAndEncrypted = encrypter.encrypt(password); // <- to encrypt passwords set it here and paste from log output to config.properties
		logger.info(password + " -> " + encodedAndEncrypted);
		String decoded = encrypter.decrypt(encodedAndEncrypted);
		Assert.assertEquals(password, decoded);
	}
	
}
