package de.tum.i13.keyring;

import de.tum.i13.shared.Server;
import de.tum.i13.ecs.keyring.KeyRingService;
import de.tum.i13.ecs.keyring.RingItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;


public class KeyringServiceTest {
    private final Server server1 = new Server("127.0.0.1","6969");
    private final Server server2 = new Server("127.0.0.2","6870");
    private final Server server3 = new Server("127.1.0.2","3243");
    private final Server server4 = new Server("127.1.0.4","3253");
    private final Server server5 = new Server("127.1.2.2","3213");
    private final String server1Hash = Server.serverToHashString(server1);
    private final String server2Hash = Server.serverToHashString(server2);
    private final String server3Hash = Server.serverToHashString(server3);
    private final String server4Hash = Server.serverToHashString(server4);
    private final String server5Hash = Server.serverToHashString(server5);

    @BeforeEach
    public void resetSingleton() throws NoSuchFieldException, IllegalAccessException {
        Field instance = KeyRingService.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    @Test
    public void helperTest() {
        System.out.println("Hashes of the servers :");
        System.out.println("Server 1 : " + server1Hash);
        System.out.println("Server 2 : " + server2Hash);
        System.out.println("Server 3 : " + server3Hash);
        System.out.println("Server 4 : " + server4Hash);
        System.out.println("Server 5 : " + server5Hash);
    }
    @Test
    public void addGetTest() {
       KeyRingService keyRingService = KeyRingService.getInstance();
       keyRingService.put(RingItem.createRingItemFromServer(server1));
       assert(keyRingService.get(server1Hash).value.getAddress().equals(server1.getAddress()));
       keyRingService.put(RingItem.createRingItemFromServer(server2));
       assert(keyRingService.get(server1Hash).value.getAddress().equals(server1.getAddress()));
       assert(keyRingService.get(server2Hash).value.getAddress().equals(server2.getAddress()));
       keyRingService.put(RingItem.createRingItemFromServer(server3));
       assert(keyRingService.get(server1Hash).value.getAddress().equals(server1.getAddress()));
       assert(keyRingService.get(server2Hash).value.getAddress().equals(server2.getAddress()));
       assert(keyRingService.get(server3Hash).value.getAddress().equals(server3.getAddress()));
   }

   @Test
   public void addRemoveTest() {
       KeyRingService keyRingService = KeyRingService.getInstance();
       keyRingService.put(RingItem.createRingItemFromServer(server1));
       keyRingService.put(RingItem.createRingItemFromServer(server2));
       keyRingService.put(RingItem.createRingItemFromServer(server3));
       keyRingService.put(RingItem.createRingItemFromServer(server4));
       RingItem ringItemServer2 =  keyRingService.get(server2Hash);
       keyRingService.delete(ringItemServer2);
       assert(keyRingService.get(server1Hash).value.getAddress().equals(server1.getAddress()));
       RingItem server2RingItem =keyRingService.get(server2Hash);
       assert(server2RingItem == null);
       assert(keyRingService.get(server3Hash).value.getAddress().equals(server3.getAddress()));
       assert(keyRingService.get(server4Hash).value.getAddress().equals(server4.getAddress()));
       keyRingService.put(RingItem.createRingItemFromServer(server5));
       RingItem ringItemServer1 =  keyRingService.get(server1Hash);
       keyRingService.delete(ringItemServer1);
       assert(keyRingService.get(server1Hash) == null);
       RingItem server3RingItem = keyRingService.get(server3Hash);
       assert(server3RingItem.value.getAddress().equals(server3.getAddress()));
       assert(keyRingService.get(server4Hash).value.getAddress().equals(server4.getAddress()));
       assert(keyRingService.get(server5Hash).value.getAddress().equals(server5.getAddress()));
   }

   @Test
   public void keyRangeHashTest() {
       KeyRingService keyRingService = KeyRingService.getInstance();
       keyRingService.put(RingItem.createRingItemFromServer(server1));
       keyRingService.put(RingItem.createRingItemFromServer(server2));
       keyRingService.put(RingItem.createRingItemFromServer(server3));
       keyRingService.put(RingItem.createRingItemFromServer(server4));
       keyRingService.put(RingItem.createRingItemFromServer(server5));
       System.out.println("Serialized Key Ring");
       System.out.println(keyRingService.serializeKeyRing());
       System.out.println("Serialized Key Ranges");
       System.out.println(keyRingService.serializeKeyRanges());
//       RingItem ringItem = keyRingService.findPredecessor("9149BBA9507A48D46D346AF2C9DC9FBB");
//       assert (ringItem.key.equals(server4Hash));
//       System.out.println(ringItem.key);
   }
   @Test
    public void serializeDeserializeKeyRing() {
        
   }
}
