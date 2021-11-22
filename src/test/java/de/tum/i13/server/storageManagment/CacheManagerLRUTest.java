package de.tum.i13.server.storageManagment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.tum.i13.server.kv.KVItem;

public class CacheManagerLRUTest implements CMTest {

	@BeforeEach
	/**
	 *
	 * citation from:
	 * https://stackoverflow.com/questions/8256989/singleton-and-unit-testing
	 *
	 */
	public void resetSingleton() throws NoSuchFieldException, IllegalAccessException {
		Field instance = CacheManager.class.getDeclaredField("instance");
		instance.setAccessible(true);
		instance.set(null, null);
		
	}

	private CacheManagerLRU create(int cahceSize) {
		CacheManagerFactory.create(cahceSize, CacheDisplacementStrategy.LRU);

		return (CacheManagerLRU) CacheManager.getInstance();
	}
	


	@Test
	public void testGetInstanceBeforeInitializeIt() {
		RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> {
			CacheManager.getInstance();
		});

		assertEquals("Initialize CacheManager first!", thrown.getMessage());
	}

	@Test
	public void testInitCacheSize100Strategy() {
		CacheManagerLRU instance = create(100);
		assertEquals(100, instance.cacheSize);
	}


	@Test
	public void testGetKeyNotInCache() {
		CacheManagerLRU instance = create(100);

		String key = "k";

		assertEquals(0, instance.map.size());

		KVItem valueReturnFromCache = instance.get(key);

		assertEquals(null, valueReturnFromCache);

		assertEquals(0, instance.map.size());
	}

	@Test
	public void testGetKeyInCache() throws Exception {
		CacheManagerLRU instance = create(100);

		String key = "k";
		String value = "v";

		instance.put(new KVItem(key, value));

		assertEquals(1, instance.map.size());

		KVItem valueReturnFromCache = instance.get(key);

		assertEquals(1, instance.map.size());

		assertEquals(value, valueReturnFromCache.value);
	}

	@Test
	public void testPutKeyNotInCache() throws Exception {
		CacheManagerLRU instance = create(100);

		for (int i = 1; i <= 10; i++) {
			instance.put(new KVItem("k" + i, "v" + i));
		}

		assertEquals(10, instance.map.size());

		PersistType pt = instance.put(new KVItem("k", "v"));

		assertEquals(11, instance.map.size());
		assertEquals(PersistType.INSERT, pt);
		assertEquals("v", instance.get("k").value);
	}

	@Test
	public void testPutKeyInCache() throws Exception {
		CacheManagerLRU instance = create(100);

		for (int i = 1; i <= 10; i++) {
			instance.put(new KVItem("k" + i, "v" + i));
		}

		assertEquals(10, instance.map.size());

		PersistType pt = instance.put(new KVItem("k1", "vNew"));

		assertEquals(10, instance.map.size());
		assertEquals(PersistType.UPDATE, pt);
		assertEquals("vNew", instance.get("k1").value);
	}

	@Test
	public void testDeleteKeyNotInCache() throws Exception {
		CacheManagerLRU instance = create(100);

		for (int i = 1; i <= 10; i++) {
			instance.put(new KVItem("k" + i, "v" + i));
		}

		assertEquals(10, instance.map.size());

		PersistType pt = instance.delete("k");

		assertEquals(10, instance.map.size());
		assertEquals(PersistType.DELETE, pt);
	}

	@Test
	public void testDeleteKeyInCache() throws Exception {
		CacheManagerLRU instance = create(100);

		for (int i = 1; i <= 10; i++) {
			instance.put(new KVItem("k" + i, "v" + i));
		}

		assertEquals(10, instance.map.size());

		PersistType pt = instance.delete("k1");

		assertEquals(9, instance.map.size());
		assertEquals(PersistType.DELETE, pt);

	}

	@Test
	public void testCheckExceedFor() throws Exception {

		CacheManagerLRU instance = create(3);

		for (int i = 1; i <= 3; i++) {
			instance.put(new KVItem("k" + i, "v" + i));
			Thread.sleep(2);
		}

		instance.get("k1");

		instance.put(new KVItem("k4", "v4"));
		assertFalse(instance.map.keySet().contains("k2"));
		assertEquals(3, instance.map.size());

		Thread.sleep(2);

		instance.put(new KVItem("k5", "v5"));
		assertFalse(instance.map.keySet().contains("k3"));
		assertEquals(3, instance.map.size());

		Thread.sleep(2);

		instance.delete("k4");
		assertEquals(2, instance.map.size());

		Thread.sleep(2);

		instance.put(new KVItem("k6", "v6"));
		assertTrue(instance.map.keySet().contains("k1"));
		assertEquals(3, instance.map.size());

		Thread.sleep(2);

		instance.put(new KVItem("k7", "v7"));
		assertFalse(instance.map.keySet().contains("k1"));
		assertEquals(3, instance.map.size());
		
	}

}
