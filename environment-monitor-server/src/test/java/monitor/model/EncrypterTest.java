package monitor.model;

import java.util.logging.Logger;

import junit.framework.Assert;
import org.junit.Test;


public class EncrypterTest {

	static Logger logger = Logger.getLogger(EncrypterTest.class.getName());
	// note: the tests actually log on so config.properties needs the encrypted password for
	// user.default.password and use.auto.password
	public static final String password = "justask";
	
	@Test
	public void testEncryptAndDecrypt() throws Exception {
		Encrypter encrypter = new Encrypter();
		String encodedAndEncrypted = encrypter.encrypt(password); // <- to encrypt passwords set it here and paste from log output to config.properties
		logger.info(password + " -> " + encodedAndEncrypted);
		String decoded = encrypter.decrypt(encodedAndEncrypted);
		Assert.assertEquals(password, decoded);
	}
	
}
