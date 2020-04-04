package monitor.dao;

import junit.framework.Assert;

import org.junit.Test;

public class FileNameHelperTest {


	@Test
	public void toFileName() {
		Assert.assertEquals("some-text-with-hyphens-", FileDAOHelper.toFileName(" Some Text - with hyphens -"));
		Assert.assertEquals("some-text-with-hyphens-", FileDAOHelper.toFileName("-Some ( %~@# Text - with hyphens -"));
		Assert.assertEquals("", FileDAOHelper.toFileName(""));		
	}
	
}
