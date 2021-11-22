package de.tum.i13.server.storageManagment;

public interface CMTest {

	public void resetSingleton() throws NoSuchFieldException, IllegalAccessException;

	public void testGetInstanceBeforeInitializeIt();

	public void testInitCacheSize100Strategy();

	public void testGetKeyNotInCache();

	public void testGetKeyInCache() throws Exception ;

	public void testPutKeyNotInCache() throws Exception ;

	public void testPutKeyInCache() throws Exception ;

	public void testDeleteKeyNotInCache() throws Exception ;

	public void testDeleteKeyInCache() throws Exception ;

	public void testCheckExceedFor() throws Exception ;
	

}