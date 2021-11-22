package de.tum.i13.server.storageManagment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import de.tum.i13.server.kv.KVItem;

public class CacheManagerFIFOTest implements CMTest {

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

	@Test
	public void testGetInstanceBeforeInitializeIt() {
		RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> {
			CacheManager.getInstance();
		});

		assertEquals("Initialize CacheManager first!", thrown.getMessage());
	}

	private CacheManagerFIFO create(int cacheSize) {
		CacheManagerFactory.create(cacheSize, CacheDisplacementStrategy.FIFO);

		return (CacheManagerFIFO) CacheManager.getInstance();
	}

	@Test
	public void testInitCacheSize100Strategy() {
		CacheManagerFIFO instance = create(100);

		assertEquals(100, instance.cacheSize);
	}

	@Test
	public void testGetKeyNotInCache() {
		CacheManagerFIFO instance = create(100);

		String key = "k";

		assertEquals(0, instance.map.size());
		assertEquals(0, instance.keyList.size());

		KVItem valueReturnFromCache = instance.get(key);

		assertEquals(null, valueReturnFromCache);

		assertEquals(0, instance.map.size());
		assertEquals(0, instance.keyList.size());
	}

	// get in cache and in disk FIFO
	@Test
	public void testGetKeyInCache() throws Exception {
		CacheManagerFIFO instance = create(100);

		String key = "k";
		String value = "v";

		instance.put(new KVItem(key, value));

		assertEquals(1, instance.map.size());
		assertEquals(1, instance.keyList.size());

		KVItem valueReturnFromCache = instance.get(key);

		assertEquals(1, instance.map.size());
		assertEquals(1, instance.keyList.size());

		assertEquals(value, valueReturnFromCache.value);
	}

	@Test
	public void testPutKeyNotInCache() throws Exception {
		CacheManagerFIFO instance = create(100);

		for (int i = 1; i <= 10; i++) {
			instance.put(new KVItem("k" + i, "v" + i));
		}

		assertEquals(10, instance.map.size());

		PersistType pt = instance.put(new KVItem("k", "v"));

		assertEquals(11, instance.map.size());
		assertEquals(PersistType.INSERT, pt);
		assertEquals("v", instance.get("k").value);
	}

	// is it the same?
	@Test
	public void testPutKeyInCache() throws Exception {
		CacheManagerFIFO instance = create(100);

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
		CacheManagerFIFO instance = create(100);

		for (int i = 1; i <= 10; i++) {
			instance.put(new KVItem("k" + i, "v" + i));
		}

		assertEquals(10, instance.map.size());

		PersistType pt = instance.delete("k");

		assertEquals(10, instance.map.size());
		assertEquals(PersistType.DELETE, pt);
	}

	// should map size be 9?
	@Test
	public void testDeleteKeyInCache() throws Exception {
		CacheManagerFIFO instance = create(100);

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

		CacheManagerFIFO instance = create(3);

		for (int i = 1; i <= 3; i++) {
			instance.put(new KVItem("k" + i, "v" + i));
		}

		assertEquals("k1", instance.keyList.get(0));
		assertEquals("k2", instance.keyList.get(1));
		assertEquals("k3", instance.keyList.get(2));

		instance.put(new KVItem("k4", "v4"));

		assertEquals("k2", instance.keyList.get(0));
		assertEquals("k3", instance.keyList.get(1));
		assertEquals("k4", instance.keyList.get(2));

		assertEquals(3, instance.map.size());

	}

}
