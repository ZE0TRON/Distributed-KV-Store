package de.tum.i13.server.storageManagment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CacheManagerLFUTest implements CMTest {

	@Mock
	private DiskManager diskManager;

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

		DiskManager.instance = diskManager;

	}

	@Test
	public void testGetInstanceBeforeInitializeIt() {
		RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> {
			CacheManager.getInstance();
		});

		assertEquals("Initialize CacheManager first!", thrown.getMessage());
	}

	private CacheManagerLFU create(int cacheSize) {
		CacheManagerFactory.create(cacheSize, CacheDisplacementStrategy.LFU);

		return (CacheManagerLFU) CacheManager.getInstance();
	}

	@Test
	public void testInitCacheSize100Strategy() {
		CacheManagerLFU instance = create(100);
		assertEquals(100, instance.cacheSize);
	}

	// get not in cache and not in disk LRU
	@Test
	public void testGetKeyNotInCacheAndNotInDisk() {
		CacheManagerLFU instance = create(100);

		String key = "key";
		lenient().when(diskManager.get(key)).thenReturn(null);

		String value = instance.get(key);

		assertEquals(null, value);
		assertEquals(0, instance.map.size());
	}

	// get not in cache and in disk LRU
	@Test
	public void testGetKeyNotInCacheAndInDisk() {
		CacheManagerLFU instance = create(100);

		String key = "k";
		String value = "v";
		lenient().when(diskManager.get(key)).thenReturn(value);

		assertEquals(0, instance.map.size());

		String valueReturnFromCache = instance.get(key);

		assertEquals(value, valueReturnFromCache);

		assertEquals(1, instance.map.size());
	}

	// get in cache and in disk LRU
	@Test
	public void testGetKeyInCacheAndInDisk() {
		CacheManagerLFU instance = create(100);

		String key = "k";
		String value = "v";
		lenient().when(diskManager.get(key)).thenReturn(value);

		instance.put(key, value);

		assertEquals(1, instance.map.size());

		String valueReturnFromCache = instance.get(key);

		assertEquals(1, instance.map.size());

		assertEquals(value, valueReturnFromCache);
	}

	@Test
	public void testPutKeyNotInCacheAndNotInDisk() {
		CacheManagerLFU instance = create(100);

		for (int i = 1; i <= 10; i++) {
			instance.put("k" + i, "v" + i);
		}

		assertEquals(10, instance.map.size());

		lenient().when(diskManager.put("k", "v")).thenReturn(true);

		boolean isInserted = instance.put("k", "v");

		assertEquals(11, instance.map.size());
		assertTrue(isInserted);
	}

	@Test
	public void testPutKeyInCacheAndNotInDisk() {
		CacheManagerLFU instance = create(100);

		for (int i = 1; i <= 10; i++) {
			instance.put("k" + i, "v" + i);
		}

		assertEquals(10, instance.map.size());

		lenient().when(diskManager.put("k1", "vNew")).thenReturn(true);

		boolean isInserted = instance.put("k1", "vNew");

		assertEquals(10, instance.map.size());
		assertTrue(isInserted);
		assertEquals("vNew", instance.get("k1"));
	}

	@Test
	public void testPutKeyInCacheAndInDisk() {
		CacheManagerLFU instance = create(100);

		for (int i = 1; i <= 10; i++) {
			instance.put("k" + i, "v" + i);
		}

		assertEquals(10, instance.map.size());

		lenient().when(diskManager.put("k1", "vNew")).thenReturn(true);

		boolean isInserted = instance.put("k1", "vNew");

		assertEquals(10, instance.map.size());
		assertTrue(isInserted);
		assertEquals("vNew", instance.get("k1"));
	}

	@Test
	public void testDeleteKeyNotInCacheAndNotInDisk() {
		CacheManagerLFU instance = create(100);

		for (int i = 1; i <= 10; i++) {
			instance.put("k" + i, "v" + i);
		}

		assertEquals(10, instance.map.size());

		lenient().when(diskManager.delete("k1")).thenReturn(false);

		boolean isDeleted = instance.delete("k1");

		assertEquals(9, instance.map.size());
		assertFalse(isDeleted);

	}

	@Test
	public void testDeleteKeyInCacheAndNotInDisk() {
		CacheManagerLFU instance = create(100);

		for (int i = 1; i <= 10; i++) {
			instance.put("k" + i, "v" + i);
		}

		assertEquals(10, instance.map.size());

		lenient().when(diskManager.get("k1")).thenReturn(null);

		boolean isDeleted = instance.delete("k1");

		assertEquals(10, instance.map.size());
		assertFalse(isDeleted);
	}

	@Test
	public void testDeleteKeyInCacheAndInDisk() {
		CacheManagerLFU instance = create(100);

		for (int i = 1; i <= 10; i++) {
			instance.put("k" + i, "v" + i);
		}

		assertEquals(10, instance.map.size());

		lenient().when(diskManager.delete("k1")).thenReturn(true);

		boolean isDeleted = instance.delete("k1");

		assertEquals(9, instance.map.size());
		assertTrue(isDeleted);
	}

	@Test
	public void testCheckExceedFor() throws InterruptedException {
		CacheManagerLFU instance = create(3);

		for (int i = 1; i <= 3; i++) {
			instance.put("k" + i, "v" + i);
			Thread.sleep(2);
		}

		// Make k1 updated in the case, k2 is least recently used
		lenient().when(diskManager.get("k1")).thenReturn("v1");
		instance.get("k1");

		instance.put("k4", "v4");
		assertFalse(instance.map.keySet().contains("k2"));
		assertEquals(3, instance.map.size());

		Thread.sleep(2);

		instance.put("k5", "v5");
		assertFalse(instance.map.keySet().contains("k3"));
		assertEquals(3, instance.map.size());

		Thread.sleep(2);

		lenient().when(diskManager.delete("k4")).thenReturn(true);
		instance.delete("k4");
		assertEquals(2, instance.map.size());

		Thread.sleep(2);

		instance.put("k6", "v6");
		assertTrue(instance.map.keySet().contains("k1"));
		assertEquals(3, instance.map.size());

		Thread.sleep(2);

		instance.put("k7", "v7");
		assertFalse(instance.map.keySet().contains("k1"));
		assertEquals(3, instance.map.size());
	}
}