package de.tum.i13.server.storageManagment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CacheManagerFIFOTest implements CMTest {

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

	private CacheManagerFIFO create(int cahceSize) {
		CacheManagerFactory.create(cahceSize, CacheDisplacementStrategy.FIFO);

		return (CacheManagerFIFO) CacheManager.getInstance();
	}

	@Test
	public void testInitCacheSize100Strategy() {
		CacheManagerFIFO instance = create(100);

		assertEquals(100, instance.cacheSize);
	}

	@Test
	public void testGetKeyNotInCacheAndNotInDisk() {
		CacheManagerFIFO instance = create(100);

		String key = "key";
		lenient().when(diskManager.get(key)).thenReturn(null);

		String value = instance.get(key);

		assertEquals(null, value);
		assertEquals(0, instance.map.size());
		assertEquals(0, instance.keyList.size());

	}

	@Test
	public void testGetKeyNotInCacheAndInDisk() {
		CacheManagerFIFO instance = create(100);

		String key = "k";
		String value = "v";
		lenient().when(diskManager.get(key)).thenReturn(value);

		assertEquals(0, instance.map.size());
		assertEquals(0, instance.keyList.size());

		String valueReturnFromCache = instance.get(key);

		assertEquals(value, valueReturnFromCache);

		assertEquals(1, instance.map.size());
		assertEquals(1, instance.keyList.size());
	}

	// get in cache and in disk FIFO
	@Test
	public void testGetKeyInCacheAndInDisk() {
		CacheManagerFIFO instance = create(100);

		String key = "k";
		String value = "v";
		lenient().when(diskManager.get(key)).thenReturn(value);

		instance.put(key, value);

		assertEquals(1, instance.map.size());
		assertEquals(1, instance.keyList.size());

		String valueReturnFromCache = instance.get(key);

		assertEquals(1, instance.map.size());
		assertEquals(1, instance.keyList.size());

		assertEquals(value, valueReturnFromCache);
	}

	@Test
	public void testPutKeyNotInCacheAndNotInDisk() {
		CacheManagerFIFO instance = create(100);

		for (int i = 1; i <= 10; i++) {
			instance.put("k" + i, "v" + i);
		}

		assertEquals(10, instance.map.size());

		lenient().when(diskManager.put("k", "v")).thenReturn(true);

		boolean isInserted = instance.put("k", "v");

		assertEquals(11, instance.map.size());
		assertTrue(isInserted);
	}
//

	@Test
	public void testPutKeyInCacheAndNotInDisk() {
		CacheManagerFIFO instance = create(100);

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

	// is it the same?
	@Test
	public void testPutKeyInCacheAndInDisk() {
		CacheManagerFIFO instance = create(100);

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

	// should delete method diskmanager be mocked?
	// should map size be 9?
	@Test
	@DisplayName("Delete key not in the cache and not in disk for FIFO")
	public void testDeleteKeyNotInCacheAndNotInDisk() {
		CacheManagerFIFO instance = create(100);

		for (int i = 1; i <= 10; i++) {
			instance.put("k" + i, "v" + i);
		}

		assertEquals(10, instance.map.size());

		lenient().when(diskManager.delete("k1")).thenReturn(false);

		boolean isDeleted = instance.delete("k1");

		assertEquals(9, instance.map.size());
		assertFalse(isDeleted);
	}

	// should map size be 9?
	@Test
	public void testDeleteKeyInCacheAndNotInDisk() {
		CacheManagerFIFO instance = create(100);

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
		CacheManagerFIFO instance = create(100);

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
	public void testCheckExceedFor() {

		CacheManagerFIFO instance = create(3);

		for (int i = 1; i <= 3; i++) {
			instance.put("k" + i, "v" + i);
		}

		assertEquals("k1", instance.keyList.get(0));
		assertEquals("k2", instance.keyList.get(1));
		assertEquals("k3", instance.keyList.get(2));

		instance.put("k4", "v4");

		assertEquals("k2", instance.keyList.get(0));
		assertEquals("k3", instance.keyList.get(1));
		assertEquals("k4", instance.keyList.get(2));

		assertEquals(3, instance.map.size());

	}

}
