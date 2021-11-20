package de.tum.i13.server.kv;

import de.tum.i13.server.storageManagment.DataManager;
import de.tum.i13.server.storageManagment.PersistType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class KVPersist implements DataManager {
    private static final Logger LOGGER = Logger.getLogger(KVStoreImpl.class.getName());

    private final File storeFileName;
    private final Marshaller marshaller;
    private final Unmarshaller unmarshaller;
    private static KVPersist instance;

    private KVPersist (Path dataDir) throws JAXBException, IOException {
        JAXBContext jaxbContext = JAXBContext.newInstance(KVItemCollection.class);

        marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        unmarshaller = jaxbContext.createUnmarshaller();
        Path filePath = Paths.get(dataDir.toString(), "KVStore.jaxb");
        storeFileName = new File(filePath.toString());
        storeFileName.createNewFile();
    }

    public static KVPersist getInstance() {
        if (instance != null) {
            return instance;
        }
        throw new RuntimeException("Initialize KVPersist first!");
    }

    public static KVPersist init(Path dataDir) throws Exception {
        if (instance == null) {
           instance = new KVPersist(dataDir);
        }
        return instance;
    }

    public void serializeAndPersistItem(KVItemCollection kvItemCollection) throws JAXBException {
        marshaller.marshal(kvItemCollection, storeFileName);
    }

    public KVItemCollection deserializeItem() throws JAXBException {
        return (KVItemCollection) unmarshaller.unmarshal(storeFileName);
    }

    public PersistType put(KVItem putItem) throws JAXBException {
        KVItemCollection storedItems = null;
        try {
            storedItems = this.deserializeItem();
            for (KVItem item : storedItems.parts){
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
            storedItems = new KVItemCollection();
        }
        storedItems.parts.add(putItem);
        this.serializeAndPersistItem(storedItems);
        LOGGER.info("New key saved successfully.");
        return PersistType.INSERT;
    }

    public KVItem get(String key) {
        try {
            KVItemCollection storedItems = this.deserializeItem();
            for (KVItem item : storedItems.parts){
                if (item.key.equals(key)){
                    LOGGER.info("Key found.");
                    return item;
                }
            }
            LOGGER.info("Key: " + key + " could not found.");
            return null;
        }
        catch (JAXBException exception){
            LOGGER.info("Data not found on disk.");
            return null;
        }
    }

    public PersistType delete(String key) throws JAXBException {
        try {
            KVItemCollection storedItems = this.deserializeItem();
            boolean isRemoved = storedItems.parts.removeIf(item -> item.key.equals(key));
            this.serializeAndPersistItem(storedItems);
            if (isRemoved) {
                LOGGER.info("Key has been deleted.");
            } else {
                LOGGER.info("Key could not be found.");
            }
            return PersistType.DELETE;
        } catch (JAXBException exception) {
            //TODO
            throw exception;
        }

    }

}
