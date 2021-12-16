package de.tum.i13.server.storageManagment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;

import de.tum.i13.server.kv.PersistItem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CacheManagerLFUTest implements CMTest {

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

	private CacheManagerLFU create(int cahceSize) {
		CacheManagerFactory.create(cahceSize, CacheDisplacementStrategy.LFU);

		return (CacheManagerLFU) CacheManager.getInstance();
	}

	@Test
	public void testInitCacheSize100Strategy() {
		CacheManagerLFU instance = create(100);
		assertEquals(100, instance.cacheSize);
	}

	@Test
	public void testGetKeyNotInCache() {
		CacheManagerLFU instance = create(100);

		String key = "k";

		assertEquals(0, instance.map.size());

		PersistItem valueReturnFromCache = instance.get(key);

		assertEquals(null, valueReturnFromCache);

		assertEquals(0, instance.map.size());
	}

	@Test
	public void testGetKeyInCache() throws Exception {
		CacheManagerLFU instance = create(100);

		String key = "k";
		String value = "v";

		instance.put(new PersistItem(key, value));

		assertEquals(1, instance.map.size());

		PersistItem valueReturnFromCache = instance.get(key);

		assertEquals(1, instance.map.size());

		assertEquals(value, valueReturnFromCache.value);
	}

	@Test
	public void testPutKeyNotInCache() throws Exception {
		CacheManagerLFU instance = create(100);

		for (int i = 1; i <= 10; i++) {
			instance.put(new PersistItem("k" + i, "v" + i));
		}

		assertEquals(10, instance.map.size());

		PersistType pt = instance.put(new PersistItem("k", "v"));

		assertEquals(11, instance.map.size());
		assertEquals(PersistType.INSERT, pt);
		assertEquals("v", instance.get("k").value);
	}

	@Test
	public void testPutKeyInCache() throws Exception {
		CacheManagerLFU instance = create(100);

		for (int i = 1; i <= 10; i++) {
			instance.put(new PersistItem("k" + i, "v" + i));
		}

		assertEquals(10, instance.map.size());

		PersistType pt = instance.put(new PersistItem("k1", "vNew"));

		assertEquals(10, instance.map.size());
		assertEquals(PersistType.UPDATE, pt);
		assertEquals("vNew", instance.get("k1").value);
	}

	@Test
	public void testDeleteKeyNotInCache() throws Exception {
		CacheManagerLFU instance = create(100);

		for (int i = 1; i <= 10; i++) {
			instance.put(new PersistItem("k" + i, "v" + i));
		}

		assertEquals(10, instance.map.size());

		PersistType pt = instance.delete("k");

		assertEquals(10, instance.map.size());
		assertEquals(PersistType.DELETE, pt);
	}

	@Test
	public void testDeleteKeyInCache() throws Exception {
		CacheManagerLFU instance = create(100);

		for (int i = 1; i <= 10; i++) {
			instance.put(new PersistItem("k" + i, "v" + i));
		}

		assertEquals(10, instance.map.size());

		PersistType pt = instance.delete("k1");

		assertEquals(9, instance.map.size());
		assertEquals(PersistType.DELETE, pt);

	}

	@Test
	public void testCheckExceedFor() throws Exception {
		CacheManagerLFU instance = create(3);

		// put k1 k2 k3 -> k1(1) k2(1) k3(1)
		for (int i = 1; i <= 3; i++) {
			instance.put(new PersistItem("k" + i, "v" + i));
		}
		assertEquals(1, instance.map.get("k1").compareValue);
		assertEquals(1, instance.map.get("k2").compareValue);
		assertEquals(1, instance.map.get("k3").compareValue);

		// get k1, k3 -> k1(2) k2(1) k3(2)
		instance.get("k1");
		instance.get("k3");
		assertEquals(2, instance.map.get("k1").compareValue);
		assertEquals(1, instance.map.get("k2").compareValue);
		assertEquals(2, instance.map.get("k3").compareValue);

		// put k4 -> k1(2) k3(2) k4(1)
		instance.put(new PersistItem("k4", "v4"));
		assertEquals(2, instance.map.get("k1").compareValue);
		assertEquals(2, instance.map.get("k3").compareValue);
		assertEquals(1, instance.map.get("k4").compareValue);
		assertEquals(3, instance.map.size());

		// put k5 -> k1(2) k3(2) k5(1)
		instance.put(new PersistItem("k5", "v5"));
		assertEquals(2, instance.map.get("k1").compareValue);
		assertEquals(2, instance.map.get("k3").compareValue);
		assertEquals(1, instance.map.get("k5").compareValue);

		// get k3 and k5 2 times -> k1(4) k3(2) k5(3)
		instance.get("k1");
		instance.get("k1");
		instance.get("k5");
		instance.get("k5");
		assertEquals(4, instance.map.get("k1").compareValue);
		assertEquals(2, instance.map.get("k3").compareValue);
		assertEquals(3, instance.map.get("k5").compareValue);

		// put k6 -> k1(4) k5(3) k6(1)
		instance.put(new PersistItem("k6", "v6"));
		assertEquals(4, instance.map.get("k1").compareValue);
		assertEquals(3, instance.map.get("k5").compareValue);
		assertEquals(1, instance.map.get("k6").compareValue);

		// update k5 -> k1(4) k5(4) k6(1)
		instance.put(new PersistItem("k5", "v5"));
		assertEquals(4, instance.map.get("k1").compareValue);
		assertEquals(4, instance.map.get("k5").compareValue);
		assertEquals(1, instance.map.get("k6").compareValue);

		// delete k5 -> k1(4) k6(1)
		instance.delete("k5");
		assertEquals(4, instance.map.get("k1").compareValue);
		assertEquals(1, instance.map.get("k6").compareValue);
		assertEquals(2, instance.map.size());

		// put k7 -> k1(4) k6(1) k7(1)
		instance.put(new PersistItem("k7", "v7"));
		assertEquals(4, instance.map.get("k1").compareValue);
		assertEquals(1, instance.map.get("k6").compareValue);
		assertEquals(1, instance.map.get("k7").compareValue);
		assertEquals(3, instance.map.size());

		// put k8 -> k1(4) k7(1) k8(1)
		instance.put(new PersistItem("k8", "v8"));
		assertEquals(4, instance.map.get("k1").compareValue);
		assertEquals(1, instance.map.get("k7").compareValue);
		assertEquals(1, instance.map.get("k8").compareValue);
		assertEquals(3, instance.map.size());

	}

}