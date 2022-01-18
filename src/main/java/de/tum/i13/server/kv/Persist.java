package de.tum.i13.server.kv;

import de.tum.i13.server.storageManagment.DataManager;
import de.tum.i13.server.storageManagment.PersistType;

import javax.xml.bind.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class Persist implements DataManager {
    private static final Logger LOGGER = Logger.getLogger(KVStoreImpl.class.getName());

    private final File storeFileName;
    private final Marshaller marshaller;
    private final Unmarshaller unmarshaller;
    private static Persist instance;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private Persist(Path dataDir) throws JAXBException, IOException {
        JAXBContext jaxbContext = JAXBContext.newInstance(PersistItemCollection.class);

        marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        unmarshaller = jaxbContext.createUnmarshaller();
        Path filePath = Paths.get(dataDir.toString(), "KVStore.jaxb");
        storeFileName = new File(filePath.toString());
        storeFileName.createNewFile();
    }

    public static Persist getInstance() {
        if (instance != null) {
            return instance;
        }
        throw new RuntimeException("Initialize Persist first!");
    }

    public static Persist init(Path dataDir) throws Exception {
        if (instance == null) {
           instance = new Persist(dataDir);
        }
        return instance;
    }

    public void serializeAndPersistItem(PersistItemCollection persistItemCollection){
        try {
            marshaller.marshal(persistItemCollection, storeFileName);
        } catch (JAXBException ex) {
            LOGGER.severe("Exception thrown while writing to the persistence file.");
            LOGGER.severe(ex.getMessage());
        }
    }

    public PersistItemCollection deserializeItem(){
        PersistItemCollection result = null;
        try {
            result = (PersistItemCollection) unmarshaller.unmarshal(storeFileName);
        } catch (JAXBException ex) {
            LOGGER.severe("Exception thrown while reading the persistence file.");
            LOGGER.severe(ex.getMessage());
        }
        return result;
    }

    public synchronized PersistType put(PersistItem putItem){
        PersistItemCollection storedItems;
        try {
            storedItems = this.deserializeItem();
            for (PersistItem item : storedItems.parts){
                if (item.key.equals(putItem.key)){
                    item.value = putItem.value;
                    this.serializeAndPersistItem(storedItems);
                    LOGGER.info("Value of the key updated.");
                    return PersistType.UPDATE;
                }
            }
        }
        // If it's the first item
        catch (Exception e) {
            storedItems = new PersistItemCollection();
        }
        storedItems.parts.add(putItem);
        this.serializeAndPersistItem(storedItems);
        LOGGER.info("New key saved successfully.");
        return PersistType.INSERT;
    }

    public PersistItem get(String key) {
        PersistItemCollection storedItems = this.deserializeItem();
        for (PersistItem item : storedItems.parts){
            if (item.key.equals(key)){
                LOGGER.info("Key found.");
                return item;
            }
        }
        LOGGER.info("Key: " + key + " could not found.");
        return null;
    }

    public PersistType delete(String key) throws Exception {
        PersistItemCollection storedItems = this.deserializeItem();
        boolean isRemoved = storedItems.parts.removeIf(item -> item.key.equals(key));
        this.serializeAndPersistItem(storedItems);
        if (isRemoved) {
            LOGGER.info("Key has been deleted.");
            return PersistType.DELETE;
        }
        LOGGER.info("Key could not be found.");
        throw new Exception("The key not found");
    }

}
