package de.tum.i13.server.storageManagment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@TestMethodOrder(OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
public class CacheManagerTest {
	
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

		assertEquals("Initialize CacheManager first", thrown.getMessage());
	}

	@Test
	public void testInitCacheSize100StrategyFIFO() {
		CacheManager.init(100, CacheDisplacementStrategy.FIFO);

		CacheManager instance = CacheManager.getInstance();

		assertEquals(100, instance.cacheSize);
		assertEquals(CacheDisplacementStrategy.FIFO, instance.cds);
	}

	@Test
	public void testInitCacheSize10StrategyLRU() {
		CacheManager.init(10, CacheDisplacementStrategy.LRU);

		CacheManager instance = CacheManager.getInstance();

		assertEquals(10, instance.cacheSize);
		assertEquals(CacheDisplacementStrategy.LRU, instance.cds);
	}

	@Test
	public void testInitCacheSize1000StrategyLFU() {
		CacheManager.init(1000, CacheDisplacementStrategy.LFU);

		CacheManager instance = CacheManager.getInstance();

		assertEquals(1000, instance.cacheSize);
		assertEquals(CacheDisplacementStrategy.LFU, instance.cds);
	}

	@Test
	public void testGetKeyNotInCahceAndNotInDiskFIFO() {
		String key = "key";
		when(diskManager.get(key)).thenReturn(null);
		
		CacheManager.init(100, CacheDisplacementStrategy.FIFO);

		CacheManager instance = CacheManager.getInstance();
		String value = instance.get(key);
		
		assertEquals(null, value);
		assertEquals(0, instance.map.size());
		assertEquals(0, instance.keyList.size());

	}
	
	@Test
	public void testGetKeyNotInCahceAndInDiskFIFO() {
		String key = "k";
		String value = "v";
		when(diskManager.get(key)).thenReturn(value);
		
		CacheManager.init(100, CacheDisplacementStrategy.FIFO);

		CacheManager instance = CacheManager.getInstance();
		
		assertEquals(0, instance.map.size());
		assertEquals(0, instance.keyList.size());
		
		String valueReturnFromCache = instance.get(key);
		
		assertEquals(value, valueReturnFromCache);
		
		assertEquals(1, instance.map.size());
		assertEquals(1, instance.keyList.size());

	}
	
	// get in cache and in disk FIFO
	
	// get not in cache and not in disk LRU
	// get not in cache and in disk LRU
	// get in cache and in disk LRU
	
	// get not in cache and not in disk LFU
	// get not in cache and in disk LFU
	// get in cache and in disk LFU

	
	// test removeFromCache
}
