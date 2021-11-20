package de.tum.i13.server.storageManagment;

public interface CMTest {

	public void resetSingleton() throws NoSuchFieldException, IllegalAccessException;

	public void testGetInstanceBeforeInitializeIt();

	public void testInitCacheSize100Strategy();

	public void testGetKeyNotInCacheAndNotInDisk();

	public void testGetKeyNotInCacheAndInDisk();

	public void testGetKeyInCacheAndInDisk();

	public void testPutKeyNotInCacheAndNotInDisk();

	public void testPutKeyInCacheAndNotInDisk();

	public void testPutKeyInCacheAndInDisk();

	public void testDeleteKeyNotInCacheAndNotInDisk();

	public void testDeleteKeyInCacheAndNotInDisk();

	public void testDeleteKeyInCacheAndInDisk();

	public void testCheckExceedFor() throws InterruptedException ;

}