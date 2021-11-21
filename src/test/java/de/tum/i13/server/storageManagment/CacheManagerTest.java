//package de.tum.i13.server.storageManagment;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.lenient;
//
//import java.lang.reflect.Field;
//
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//@ExtendWith(MockitoExtension.class)
//public class CacheManagerTest {
//
//	@Mock
//	private DiskManager diskManager;
//
//	@BeforeEach
//	/**
//	 *
//	 * citation from:
//	 * https://stackoverflow.com/questions/8256989/singleton-and-unit-testing
//	 *
//	 */
//	public void resetSingleton() throws NoSuchFieldException, IllegalAccessException {
//		Field instance = CacheManager.class.getDeclaredField("instance");
//		instance.setAccessible(true);
//		instance.set(null, null);
//
//		DiskManager.instance = diskManager;
//	}
//
//	@Test
//	public void testGetInstanceBeforeInitializeIt() {
//		RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> {
//			CacheManager.getInstance();
//		});
//
//		assertEquals("Initialize CacheManager first!", thrown.getMessage());
//	}
//
//	@Test
//	public void testInitCacheSize100StrategyFIFO() {
//		CacheManager.init(100, CacheDisplacementStrategy.FIFO);
//
//		CacheManager instance = CacheManager.getInstance();
//
//		assertEquals(100, instance.cacheSize);
//		assertEquals(CacheDisplacementStrategy.FIFO, instance.cds);
//	}
//
//	@Test
//	public void testInitCacheSize10StrategyLRU() {
//		CacheManager.init(10, CacheDisplacementStrategy.LRU);
//
//		CacheManager instance = CacheManager.getInstance();
//
//		assertEquals(10, instance.cacheSize);
//		assertEquals(CacheDisplacementStrategy.LRU, instance.cds);
//	}
//
//	@Test
//	public void testInitCacheSize1000StrategyLFU() {
//		CacheManager.init(1000, CacheDisplacementStrategy.LFU);
//
//		CacheManager instance = CacheManager.getInstance();
//
//		assertEquals(1000, instance.cacheSize);
//		assertEquals(CacheDisplacementStrategy.LFU, instance.cds);
//	}
//
//	@Test
//	public void testGetKeyNotInCacheAndNotInDiskFIFO() {
//		String key = "key";
//		lenient().when(diskManager.get(key)).thenReturn(null);
//
//		CacheManager.init(100, CacheDisplacementStrategy.FIFO);
//
//		CacheManager instance = CacheManager.getInstance();
//		String value = instance.get(key);
//
//		assertEquals(null, value);
//		assertEquals(0, instance.map.size());
//		assertEquals(0, instance.keyList.size());
//
//	}
//
//	@Test
//	public void testGetKeyNotInCacheAndInDiskFIFO() {
//		String key = "k";
//		String value = "v";
//		lenient().when(diskManager.get(key)).thenReturn(value);
//
//		CacheManager.init(100, CacheDisplacementStrategy.FIFO);
//
//		CacheManager instance = CacheManager.getInstance();
//
//		assertEquals(0, instance.map.size());
//		assertEquals(0, instance.keyList.size());
//
//		String valueReturnFromCache = instance.get(key);
//
//		assertEquals(value, valueReturnFromCache);
//
//		assertEquals(1, instance.map.size());
//		assertEquals(1, instance.keyList.size());
//	}
//
//	// get in cache and in disk FIFO
//	@Test
//	public void testGetKeyInCacheAndInDiskFIFO() {
//		String key = "k";
//		String value = "v";
//		lenient().when(diskManager.get(key)).thenReturn(value);
//
//		CacheManager.init(100, CacheDisplacementStrategy.FIFO);
//
//		CacheManager instance = CacheManager.getInstance();
//
//		instance.put(key, value);
//
//		assertEquals(1, instance.map.size());
//		assertEquals(1, instance.keyList.size());
//
//		String valueReturnFromCache = instance.get(key);
//
//		assertEquals(1, instance.map.size());
//		assertEquals(1, instance.keyList.size());
//
//		assertEquals(value, valueReturnFromCache);
//	}
//
//	// get not in cache and not in disk LRU
//	@Test
//	public void testGetKeyNotInCacheAndNotInDiskLRU() {
//		String key = "key";
//		lenient().when(diskManager.get(key)).thenReturn(null);
//
//		CacheManager.init(100, CacheDisplacementStrategy.LRU);
//
//		CacheManager instance = CacheManager.getInstance();
//		String value = instance.get(key);
//
//		assertEquals(null, value);
//		assertEquals(0, instance.map.size());
//	}
//
//	// get not in cache and in disk LRU
//	@Test
//	public void testGetKeyNotInCacheAndInDiskLRU() {
//		String key = "k";
//		String value = "v";
//		lenient().when(diskManager.get(key)).thenReturn(value);
//
//		CacheManager.init(100, CacheDisplacementStrategy.LRU);
//
//		CacheManager instance = CacheManager.getInstance();
//
//		assertEquals(0, instance.map.size());
//
//		String valueReturnFromCache = instance.get(key);
//
//		assertEquals(value, valueReturnFromCache);
//
//		assertEquals(1, instance.map.size());
//	}
//
//	// get in cache and in disk LRU
//	@Test
//	public void testGetKeyInCacheAndInDiskLRU() {
//		String key = "k";
//		String value = "v";
//		lenient().when(diskManager.get(key)).thenReturn(value);
//
//		CacheManager.init(100, CacheDisplacementStrategy.LRU);
//
//		CacheManager instance = CacheManager.getInstance();
//
//		instance.put(key, value);
//
//		assertEquals(1, instance.map.size());
//
//		String valueReturnFromCache = instance.get(key);
//
//		assertEquals(1, instance.map.size());
//
//		assertEquals(value, valueReturnFromCache);
//	}
//
//	// get not in cache and not in disk LFU
//	@Test
//	public void testGetKeyNotInCacheAndNotInDiskLFU() {
//		String key = "key";
//		lenient().when(diskManager.get(key)).thenReturn(null);
//
//		CacheManager.init(100, CacheDisplacementStrategy.LFU);
//
//		CacheManager instance = CacheManager.getInstance();
//		String value = instance.get(key);
//
//		assertEquals(null, value);
//		assertEquals(0, instance.map.size());
//	}
//
//	// get not in cache and in disk LFU
//	@Test
//	public void testGetKeyNotInCacheAndInDiskLFU() {
//		String key = "k";
//		String value = "v";
//		lenient().when(diskManager.get(key)).thenReturn(value);
//
//		CacheManager.init(100, CacheDisplacementStrategy.LFU);
//
//		CacheManager instance = CacheManager.getInstance();
//
//		assertEquals(0, instance.map.size());
//
//		String valueReturnFromCache = instance.get(key);
//
//		assertEquals(value, valueReturnFromCache);
//
//		assertEquals(1, instance.map.size());
//	}
//
//	// get in cache and in disk LFU
//	@Test
//	public void testGetKeyInCacheAndInDiskLFU() {
//		String key = "k";
//		String value = "v";
//		lenient().when(diskManager.get(key)).thenReturn(value);
//
//		CacheManager.init(100, CacheDisplacementStrategy.LFU);
//
//		CacheManager instance = CacheManager.getInstance();
//
//		instance.put(key, value);
//
//		assertEquals(1, instance.map.size());
//
//		String valueReturnFromCache = instance.get(key);
//
//		assertEquals(1, instance.map.size());
//
//		assertEquals(value, valueReturnFromCache);
//	}
//
//	@Test
//	public void testPutKeyNotInCacheAndNotInDiskFIFO() {
//		CacheManager.init(100, CacheDisplacementStrategy.FIFO);
//
//		CacheManager instance = CacheManager.getInstance();
//
//		for (int i = 1; i <= 10; i++) {
//			instance.put("k" + i, "v" + i);
//		}
//
//		assertEquals(10, instance.map.size());
//
//		lenient().when(diskManager.put("k", "v")).thenReturn(true);
//
//		boolean isInserted = instance.put("k", "v");
//
//		assertEquals(11, instance.map.size());
//		assertTrue(isInserted);
//	}
////
//
//	@Test
//	public void testPutKeyInCacheAndNotInDiskFIFO() {
//		CacheManager.init(100, CacheDisplacementStrategy.FIFO);
//
//		CacheManager instance = CacheManager.getInstance();
//
//		for (int i = 1; i <= 10; i++) {
//			instance.put("k" + i, "v" + i);
//		}
//
//		assertEquals(10, instance.map.size());
//
//		lenient().when(diskManager.put("k1", "vNew")).thenReturn(true);
//
//		boolean isInserted = instance.put("k1", "vNew");
//
//		assertEquals(10, instance.map.size());
//		assertTrue(isInserted);
//		assertEquals("vNew", instance.get("k1"));
//	}
//
//	// is it the same?
//	@Test
//	public void testPutKeyInCacheAndInDiskFIFO() {
//		CacheManager.init(100, CacheDisplacementStrategy.FIFO);
//
//		CacheManager instance = CacheManager.getInstance();
//
//		for (int i = 1; i <= 10; i++) {
//			instance.put("k" + i, "v" + i);
//		}
//
//		assertEquals(10, instance.map.size());
//
//		lenient().when(diskManager.put("k1", "vNew")).thenReturn(true);
//
//		boolean isInserted = instance.put("k1", "vNew");
//
//		assertEquals(10, instance.map.size());
//		assertTrue(isInserted);
//		assertEquals("vNew", instance.get("k1"));
//
//	}
//
//	@Test
//	public void testPutKeyNotInCacheAndNotInDiskLRU() {
//		CacheManager.init(100, CacheDisplacementStrategy.LRU);
//
//		CacheManager instance = CacheManager.getInstance();
//
//		for (int i = 1; i <= 10; i++) {
//			instance.put("k" + i, "v" + i);
//		}
//
//		assertEquals(10, instance.map.size());
//
//		lenient().when(diskManager.put("k", "v")).thenReturn(true);
//
//		boolean isInserted = instance.put("k", "v");
//
//		assertEquals(11, instance.map.size());
//		assertTrue(isInserted);
//	}
//
//	@Test
//	public void testPutKeyInCacheAndNotInDiskLRU() {
//		CacheManager.init(100, CacheDisplacementStrategy.LRU);
//
//		CacheManager instance = CacheManager.getInstance();
//
//		for (int i = 1; i <= 10; i++) {
//			instance.put("k" + i, "v" + i);
//		}
//
//		assertEquals(10, instance.map.size());
//
//		lenient().when(diskManager.put("k1", "vNew")).thenReturn(true);
//
//		boolean isInserted = instance.put("k1", "vNew");
//
//		assertEquals(10, instance.map.size());
//		assertTrue(isInserted);
//		assertEquals("vNew", instance.get("k1"));
//	}
//
//	@Test
//	public void testPutKeyInCacheAndInDiskLRU() {
//		CacheManager.init(100, CacheDisplacementStrategy.LRU);
//
//		CacheManager instance = CacheManager.getInstance();
//
//		for (int i = 1; i <= 10; i++) {
//			instance.put("k" + i, "v" + i);
//		}
//
//		assertEquals(10, instance.map.size());
//
//		lenient().when(diskManager.put("k1", "vNew")).thenReturn(true);
//
//		boolean isInserted = instance.put("k1", "vNew");
//
//		assertEquals(10, instance.map.size());
//		assertTrue(isInserted);
//		assertEquals("vNew", instance.get("k1"));
//	}
//
//	@Test
//	public void testPutKeyNotInCacheAndNotInDiskLFU() {
//		CacheManager.init(100, CacheDisplacementStrategy.LFU);
//
//		CacheManager instance = CacheManager.getInstance();
//
//		for (int i = 1; i <= 10; i++) {
//			instance.put("k" + i, "v" + i);
//		}
//
//		assertEquals(10, instance.map.size());
//
//		lenient().when(diskManager.put("k", "v")).thenReturn(true);
//
//		boolean isInserted = instance.put("k", "v");
//
//		assertEquals(11, instance.map.size());
//		assertTrue(isInserted);
//	}
//
//	@Test
//	public void testPutKeyInCacheAndNotInDiskLFU() {
//		CacheManager.init(100, CacheDisplacementStrategy.LFU);
//
//		CacheManager instance = CacheManager.getInstance();
//
//		for (int i = 1; i <= 10; i++) {
//			instance.put("k" + i, "v" + i);
//		}
//
//		assertEquals(10, instance.map.size());
//
//		lenient().when(diskManager.put("k1", "vNew")).thenReturn(true);
//
//		boolean isInserted = instance.put("k1", "vNew");
//
//		assertEquals(10, instance.map.size());
//		assertTrue(isInserted);
//		assertEquals("vNew", instance.get("k1"));
//	}
//
//	@Test
//	public void testPutKeyInCacheAndInDiskLFU() {
//		CacheManager.init(100, CacheDisplacementStrategy.LFU);
//
//		CacheManager instance = CacheManager.getInstance();
//
//		for (int i = 1; i <= 10; i++) {
//			instance.put("k" + i, "v" + i);
//		}
//
//		assertEquals(10, instance.map.size());
//
//		lenient().when(diskManager.put("k1", "vNew")).thenReturn(true);
//
//		boolean isInserted = instance.put("k1", "vNew");
//
//		assertEquals(10, instance.map.size());
//		assertTrue(isInserted);
//		assertEquals("vNew", instance.get("k1"));
//	}
//
//	@Test
//	@DisplayName("Delete key not in the cache and not in disk for FIFO")
//	public void testDeleteKeyNotInCacheAndNotInDiskFIFO() {
//		CacheManager.init(10, CacheDisplacementStrategy.FIFO);
//
//		CacheManager instance = CacheManager.getInstance();
//
//		for (int i = 1; i <= 10; i++) {
//			instance.put("k" + i, "v" + i);
//		}
//
//		assertEquals(10, instance.map.size());
//
//		lenient().when(diskManager.get("k")).thenReturn(null);
//
//		boolean isDeleted = instance.delete("k");
//
//		assertEquals(10, instance.map.size());
//		assertFalse(isDeleted);
//	}
//
//	//
//	@Test
//	public void testDeleteKeyInCacheAndNotInDiskFIFO() {
//
//	}
//
//	@Test
//	public void testDeleteKeyInCacheAndInDiskFIFO() {
//
//	}
//
//	@Test
//	public void testDeleteKeyNotInCacheAndNotInDiskLRU() {
//
//	}
//
//	@Test
//	public void testDeleteKeyInCacheAndNotInDiskLRU() {
//
//	}
//
//	@Test
//	public void testDeleteKeyInCacheAndInDiskLRU() {
//
//	}
//
//	@Test
//	public void testDeleteKeyNotInCacheAndNotInDiskLFU() {
//
//	}
//
//	@Test
//	public void testDeleteKeyInCacheAndNotInDiskLFU() {
//
//	}
//
//	@Test
//	public void testDeleteKeyInCacheAndInDiskLFU() {
//
//	}
//
//	@Test
//	public void testCheckExceedForFIFO() {
//		CacheManager.init(3, CacheDisplacementStrategy.FIFO);
//		CacheManager instance = CacheManager.getInstance();
//
//		for (int i = 1; i <= 3; i++) {
//			instance.put("k" + i, "v" + i);
//		}
//
//		assertEquals("k1", instance.keyList.get(0));
//		assertEquals("k2", instance.keyList.get(1));
//		assertEquals("k3", instance.keyList.get(2));
//
//		instance.put("k4", "v4");
//
//		assertEquals("k2", instance.keyList.get(0));
//		assertEquals("k3", instance.keyList.get(1));
//		assertEquals("k4", instance.keyList.get(2));
//
//		assertEquals(3, instance.map.size());
//
//	}
//
//	@Test
//	public void testCheckExceedForLRU() throws InterruptedException {
//		CacheManager.init(3, CacheDisplacementStrategy.LRU);
//		CacheManager instance = CacheManager.getInstance();
//
//		for (int i = 1; i <= 3; i++) {
//			instance.put("k" + i, "v" + i);
//			Thread.sleep(2);
//		}
//
//		// Make k1 updated in the case, k2 is least recently used
//		lenient().when(diskManager.get("k1")).thenReturn("v1");
//		instance.get("k1");
//
//		instance.put("k4", "v4");
//		assertFalse(instance.map.keySet().contains("k2"));
//		assertEquals(3, instance.map.size());
//
//		Thread.sleep(2);
//
//		instance.put("k5", "v5");
//		assertFalse(instance.map.keySet().contains("k3"));
//		assertEquals(3, instance.map.size());
//
//		Thread.sleep(2);
//
//		lenient().when(diskManager.delete("k4")).thenReturn(true);
//		instance.delete("k4");
//		assertEquals(2, instance.map.size());
//
//		Thread.sleep(2);
//
//		instance.put("k6", "v6");
//		assertTrue(instance.map.keySet().contains("k1"));
//		assertEquals(3, instance.map.size());
//
//		Thread.sleep(2);
//
//		instance.put("k7", "v7");
//		assertFalse(instance.map.keySet().contains("k1"));
//		assertEquals(3, instance.map.size());
//	}
//
//	@Test
//	public void testCheckExceedForLFU() {
//		CacheManager.init(3, CacheDisplacementStrategy.LFU);
//		CacheManager instance = CacheManager.getInstance();
//
//		// put k1 k2 k3 -> k1(1) k2(1) k3(1)
//		for (int i = 1; i <= 3; i++) {
//			instance.put("k" + i, "v" + i);
//		}
//		assertEquals(1, instance.map.get("k1").compareValue);
//		assertEquals(1, instance.map.get("k2").compareValue);
//		assertEquals(1, instance.map.get("k3").compareValue);
//
//		// get k1, k3 -> k1(2) k2(1) k3(2)
//		lenient().when(diskManager.get("k1")).thenReturn("v1");
//		lenient().when(diskManager.get("k3")).thenReturn("v3");
//		instance.get("k1");
//		instance.get("k3");
//		assertEquals(2, instance.map.get("k1").compareValue);
//		assertEquals(1, instance.map.get("k2").compareValue);
//		assertEquals(2, instance.map.get("k3").compareValue);
//
//		// put k4 -> k1(2) k3(2) k4(1)
//		instance.put("k4", "v4");
//		assertEquals(2, instance.map.get("k1").compareValue);
//		assertEquals(2, instance.map.get("k3").compareValue);
//		assertEquals(1, instance.map.get("k4").compareValue);
//		assertEquals(3, instance.map.size());
//
//		// put k5 -> k1(2) k3(2) k5(1)
//		instance.put("k5", "v5");
//		assertEquals(2, instance.map.get("k1").compareValue);
//		assertEquals(2, instance.map.get("k3").compareValue);
//		assertEquals(1, instance.map.get("k5").compareValue);
//
//		// get k3 and k5 2 times -> k1(4) k3(2) k5(3)
//		lenient().when(diskManager.get("k5")).thenReturn("v5");
//		instance.get("k1");
//		instance.get("k1");
//		instance.get("k5");
//		instance.get("k5");
//		assertEquals(4, instance.map.get("k1").compareValue);
//		assertEquals(2, instance.map.get("k3").compareValue);
//		assertEquals(3, instance.map.get("k5").compareValue);
//
//		// put k6 ->  k1(4) k5(3) k6(1)
//		instance.put("k6", "v6");
//		assertEquals(4, instance.map.get("k1").compareValue);
//		assertEquals(3, instance.map.get("k5").compareValue);
//		assertEquals(1, instance.map.get("k6").compareValue);
//
//		// update k5 ->  k1(4) k5(4) k6(1)
//		instance.put("k5", "v5");
//		assertEquals(4, instance.map.get("k1").compareValue);
//		assertEquals(4, instance.map.get("k5").compareValue);
//		assertEquals(1, instance.map.get("k6").compareValue);
//
//		// delete k5 -> k1(4) k6(1)
//		lenient().when(diskManager.delete("k5")).thenReturn(true);
//		instance.delete("k5");
//		assertEquals(4, instance.map.get("k1").compareValue);
//		assertEquals(1, instance.map.get("k6").compareValue);
//		assertEquals(2, instance.map.size());
//
//		// put k7 -> k1(4) k6(1) k7(1)
//		instance.put("k7", "v7");
//		assertEquals(4, instance.map.get("k1").compareValue);
//		assertEquals(1, instance.map.get("k6").compareValue);
//		assertEquals(1, instance.map.get("k7").compareValue);
//		assertEquals(3, instance.map.size());
//
//		// put k8 -> k1(4) k7(1) k8(1)
//		instance.put("k8", "v8");
//		assertEquals(4, instance.map.get("k1").compareValue);
//		assertEquals(1, instance.map.get("k7").compareValue);
//		assertEquals(1, instance.map.get("k8").compareValue);
//		assertEquals(3, instance.map.size());
//
//	}
//}
