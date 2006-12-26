package net.azib.ipscan.feeders;

import java.io.StringReader;

import junit.framework.TestCase;

/**
 * Test of FileFeeder
 *
 * @author anton
 */
public class FileFeederTest extends TestCase {

	public void testHappyPath() throws FeederException {
		StringReader reader = new StringReader("10.11.12.13 10.11.12.14 10.11.12.15");
		FileFeeder fileFeeder = new FileFeeder();
		fileFeeder.initialize(reader);
		assertTrue(fileFeeder.hasNext());
		assertEquals("10.11.12.13", fileFeeder.next().getHostAddress());
		assertTrue(fileFeeder.hasNext());
		assertEquals("10.11.12.14", fileFeeder.next().getHostAddress());
		assertTrue(fileFeeder.hasNext());
		assertEquals("10.11.12.15", fileFeeder.next().getHostAddress());
		assertFalse(fileFeeder.hasNext());
	}
	
	public void testStringParams() {
		try {
			FileFeeder fileFeeder = new FileFeeder();
			assertEquals(1, fileFeeder.initialize(new String[] {"build.xml"}));
		}
		catch (FeederException e) {
			assertEquals("file.nothingFound", e.getMessage());
		}
	}
	
	public void testNoFile() {
		try {
			new FileFeeder().initialize("no_such_file.txt");
			fail();
		}
		catch (FeederException e) {
			FeederTestUtils.assertFeederException("file.notExists", e);
		}		
	}
	
	public void testNothingFound() {
		try {
			StringReader reader = new StringReader("no ip addresses here");			
			new FileFeeder().initialize(reader);
			fail();
		}
		catch (FeederException e) {
			FeederTestUtils.assertFeederException("file.nothingFound", e);
		}
	}
	
	public void testExtractFromDifferentFormats() {
		
		assertAddressCount("The 127.0.0.1 is the localhost IP,\n but 192.168.255.255 is probably a broadcast IP", 2);
		
		assertAddressCount("1.1.1.,1245\n2.2.2.2:123\n3.3.3.3.3.3\n\n\n9.9.9.9999", 2);
		
		assertAddressCount("1.2.3.4", 1);
		
		assertAddressCount("1.2.3.4:125\n2.3.4.255:347", 2);
		
		assertAddressCount("255.255.255.255\n\n\n\t0.0.0.0", 2);

		// This test fails under GCJ, probably it doesn't normalize IP addresses,
		// passed to the InetAddress and throws UnknownHostException because of the leading zero
		// assertAddressCount("09.001.005.006", 1);

		assertAddressCount("999.999.999.999,1.1.01.1", 1);

		assertAddressCount("<xml>66.87.99.128</xml>\n<xml>000.87.99.129</xml>0000.1.1.1", 2);
	}
	
	private void assertAddressCount(String s, int addressCount) {
		StringReader reader = new StringReader(s);			
		FileFeeder feeder = new FileFeeder();
		feeder.initialize(reader);
		int numAddresses = 0;
		while (feeder.hasNext()) {
			feeder.next();
			numAddresses++;
		}
		assertEquals(addressCount, numAddresses);
	}
		
	public void testGetPercentageComplete() throws Exception {
		StringReader reader = new StringReader("1.2.3.4, 2.3.4.5, mega cool 0.0.0.0");
		FileFeeder fileFeeder = new FileFeeder();
		fileFeeder.initialize(reader);
		assertEquals(0, fileFeeder.getPercentageComplete());
		fileFeeder.next();
		assertEquals(33, fileFeeder.getPercentageComplete());
		fileFeeder.next();
		assertEquals(67, fileFeeder.getPercentageComplete());
		fileFeeder.next();
		assertEquals(100, fileFeeder.getPercentageComplete());
		
		reader = new StringReader("255.255.255.255");
		fileFeeder.initialize(reader);
		assertEquals(0, fileFeeder.getPercentageComplete());
		fileFeeder.next();
		assertEquals(100, fileFeeder.getPercentageComplete());
	}	
	
	public void testGetInfo() {
		FileFeeder fileFeeder = new FileFeeder();
		StringReader reader = new StringReader("255.255.255.255, 2.3.4.5, mega cool 0.0.0.0");
		fileFeeder.initialize(reader);
		assertEquals("3", fileFeeder.getInfo());
	}

}
