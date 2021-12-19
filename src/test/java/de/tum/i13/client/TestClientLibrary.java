//package de.tum.i13.client;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.Mockito.when;
//
//import java.lang.reflect.Field;
//import java.util.ArrayList;
//
//import de.tum.i13.shared.keyring.ConsistentHashingService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//@ExtendWith(MockitoExtension.class)
//public class TestClientLibrary {
//
//	@Mock
//	private CommandSender commandSender;
//
//	private KVStoreClientLibraryImpl clientLibrary;
//
//	@BeforeEach
//	public void init() {
//		clientLibrary = new KVStoreClientLibraryImpl("localhost", 12345);
//
//	}
//
//	@SuppressWarnings("unchecked")
//	private ArrayList<KeyRange> getMetaData() {
//		try {
//			Field privateStringField = KVStoreClientLibraryImpl.class.getDeclaredField("metaData");
//			privateStringField.setAccessible(true);
//			return (ArrayList<KeyRange>) privateStringField.get(clientLibrary);
//		} catch (NoSuchFieldException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (SecurityException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalArgumentException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return null;
//	}
//
//	@Test
//	public void testSendGetRequest() throws Exception {
//
//		String expected = "get_success key1 value1";
//		when(commandSender.sendCommandToServer("localhost", 12345, "get key1")).thenReturn(expected);
//		String result = clientLibrary.sendGetRequest("get key1");
//		assertEquals(expected, result);
//
//		expected = "get_error key1 key not found";
//		when(commandSender.sendCommandToServer("localhost", 12345, "get key1")).thenReturn(expected);
//		result = clientLibrary.sendGetRequest("get key1");
//		assertEquals(expected, result);
//	}
//
//	@Test
//	public void testSendPutRequest() throws Exception {
//
//		// put_success
//		String expectedSuccess = "put_success key1";
//		when(commandSender.sendCommandToServer("localhost", 12345, "put key1 value1")).thenReturn(expectedSuccess);
//		String resultSuccess = clientLibrary.sendPutRequest("put key1 value1");
//
//		assertEquals(expectedSuccess, resultSuccess);
//
//		// put_update
//		String expectedUpdate = "put_update key1";
//		when(commandSender.sendCommandToServer("localhost", 12345, "put key1 value2")).thenReturn(expectedUpdate);
//		String resultUpdate = clientLibrary.sendPutRequest("put key1 value2");
//
//		assertEquals(expectedUpdate, resultUpdate);
//
//		// put_error
//		String expectedError = "put_error key1 value1";
//		when(commandSender.sendCommandToServer("localhost", 12345, "put key1 value1")).thenReturn(expectedError);
//		String resultError = clientLibrary.sendPutRequest("put key1 value1");
//
//		assertEquals(expectedError, resultError);
//	}
//
//	@Test
//	public void testDeletePutRequest() throws Exception {
//
//		// delete_success
//		String expectedSuccess = "delete_success key1";
//		when(commandSender.sendCommandToServer("localhost", 12345, "delete key1")).thenReturn(expectedSuccess);
//		String resultSuccess = clientLibrary.sendGetRequest("delete key1");
//
//		assertEquals(expectedSuccess, resultSuccess);
//
//		// delete_error
//		String expectedError = "delete_error key1";
//		when(commandSender.sendCommandToServer("localhost", 12345, "delete key1")).thenReturn(expectedError);
//		String resultError = clientLibrary.sendGetRequest("delete key1");
//
//		assertEquals(expectedError, resultError);
//	}
//
//	/*
//	 * server_write_lock indicates that the storage server is currently blocked for
//	 * write requests due to reallocation of data in case of joining or leaving
//	 * storage nodes.
//	 */
//
//	@Test
//	public void test_server_write_lock() throws Exception {
//
//		String expected = "server_write_lock";
//		when(commandSender.sendCommandToServer("localhost", 12345, "get key1")).thenReturn(expected);
//		String result = clientLibrary.sendGetRequest("get key1");
//
//		assertEquals(expected, result);
//	}
//
//	/*
//	 * keyrange_success returns the ranges and which KVStores are responsible for
//	 * the range. The range is expressed in hex (128bit). The list of ranges and the
//	 * corresponding servers are returned as a list of semicolon separated triples
//	 * <kr-from>, <kr-to>, <ip:port>; <kr-from>, <krto>, <ip:port>;...
//	 */
//
//	// keyrange_success
//	@Test
//	public void keyrange_success() throws Exception {
//		ArrayList<KeyRange> metaData = getMetaData();
//		assertEquals(1, metaData.size());
//
//		String expected = "keyrange_success 9DE0BC202346026CAD3749DB215A2D43,411900A46428D02426F4951156382B7D,127.0.0.2:6870;" +
//				"411900A46428D02426F4951156382B7D,57A22CF84442B4B9D53444F145DEFF66,127.1.2.2:3213;" +
//				"57A22CF84442B4B9D53444F145DEFF66,587378D4ED623848E51B2779283FFDE8,127.1.0.2:3243;" +
//				"587378D4ED623848E51B2779283FFDE8,9049BBA9507A48D46D346AF2C9DC9FBB,127.0.0.1:6969;" +
//				"9049BBA9507A48D46D346AF2C9DC9FBB,9DE0BC202346026CAD3749DB215A2D43,127.1.0.4:3253";
//		when(commandSender.sendCommandToServer("localhost", 12345, "keyrange")).thenReturn(expected);
//
//		KeyRange kr = clientLibrary.findCorrectKeyRange("key1");
//		clientLibrary.updateKeyRanges(kr.host, kr.port);
//		metaData = getMetaData();
//
//		assertEquals(5, metaData.size());
//		assertEquals(metaData.get(0).from, "9DE0BC202346026CAD3749DB215A2D43");
//		assertEquals(metaData.get(0).to, "411900A46428D02426F4951156382B7D");
//		assertEquals(metaData.get(0).host, "127.0.0.2");
//		assertEquals(metaData.get(0).port, 6870);
//
//		assertEquals(metaData.get(1).from, "411900A46428D02426F4951156382B7D");
//		assertEquals(metaData.get(1).to, "57A22CF84442B4B9D53444F145DEFF66");
//		assertEquals(metaData.get(1).host, "127.1.2.2");
//		assertEquals(metaData.get(1).port, 3213);
//		String key1 = "apple";
//		String key1Hash = ConsistentHashingService.findHash(key1);
//		System.out.println(key1Hash);
//		KeyRange kr2 = clientLibrary.findCorrectKeyRange(key1);
//		System.out.println(kr2.host + " " +  kr2.from + " " +  kr2.to);
//
//
//	}
//
//	/*
//	 * not_responsible is used by the storage server to respond to requests that
//	 * could not be processed by the respective server because the requested key is
//	 * not within its range. Such messages should not be passed back to the client
//	 * application but invoke a retrieval of the keyrange.
//	 */
//
//	// server_not_responsible
//	@Test
//	public void test_server_not_responsible() throws Exception {
//		ArrayList<KeyRange> metaData = getMetaData();
//		assertEquals(1, metaData.size());
//
//		String updatedMetadata = "keyrange_success 0,5,101.0.0.0:1001;6,10,102.0.0.0:1002";
//		when(commandSender.sendCommandToServer("localhost", 12345, "keyrange")).thenReturn(updatedMetadata);
//		KeyRange kr = clientLibrary.findCorrectKeyRange("key1");
//		clientLibrary.updateKeyRanges(kr.host, kr.port);
//
//		// checking if the metadata is updated correctly
//		metaData = getMetaData();
//
//		assertEquals(2, metaData.size());
//		assertEquals(metaData.get(0).from, "0");
//		assertEquals(metaData.get(0).to, "5");
//		assertEquals(metaData.get(0).host, "101.0.0.0");
//		assertEquals(metaData.get(0).port, 1001);
//
//		assertEquals(metaData.get(1).from, "6");
//		assertEquals(metaData.get(1).to, "10");
//		assertEquals(metaData.get(1).host, "102.0.0.0");
//		assertEquals(metaData.get(1).port, 1002);
//	}
//
//	/*
//	 * server_stopped indicates that currently no requests are processed by the
//	 * server since the whole storage service is under initialization. Hence, from
//	 * the client�s perspective the server is stopped for serving requests. The
//	 * client retries several times using exponential back-off with jitter.
//	 */
//
//	// server_stopped
//	@Test
//	public void test_server_stopped() throws Exception {
//		String expected = "get_success key1 value1";
//		when(commandSender.sendCommandToServer("localhost", 12345, "get key1")).thenReturn("server_stopped");
//
//		new Thread(() -> {
//			try {
//				Thread.sleep(2000);
//				when(commandSender.sendCommandToServer("localhost", 12345, "get key1")).thenReturn(expected);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}).start();
//
//		String result = clientLibrary.sendGetRequest("get key1");
//		assertEquals(expected, result);
//	}
//}
