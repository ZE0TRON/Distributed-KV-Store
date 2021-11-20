package de.tum.i13.server.kv;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import java.io.File;
import java.util.logging.Logger;

public class KVPersist {
    private static final Logger LOGGER = Logger.getLogger(KVStoreImpl.class.getName());

    private final File storeFileName;
    private final Marshaller marshaller;
    private final Unmarshaller unmarshaller;

    public KVPersist(String fileName) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(KVItemCollection.class);

        marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        unmarshaller = jaxbContext.createUnmarshaller();
        storeFileName = new File(fileName);
    }

    public void serializeAndPersistItem(KVItemCollection kvItemCollection) throws JAXBException {
        marshaller.marshal(kvItemCollection, storeFileName);
    }

    public KVItemCollection deserializeItem() throws JAXBException {
        return (KVItemCollection) unmarshaller.unmarshal(storeFileName);
    }

    public void put(KVItem putItem) throws JAXBException {
        KVItemCollection storedItems = this.deserializeItem();
        for (KVItem item : storedItems.parts){
            if (item.key.equals(putItem.key)){
                item.value = putItem.value;
                this.serializeAndPersistItem(storedItems);
                LOGGER.info("Value of the key updated.");
                return;
            }
        }
        storedItems.parts.add(putItem);
        this.serializeAndPersistItem(storedItems);
        LOGGER.info("New key saved successfully.");
    }

    public KVItem get(String key) throws JAXBException {
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

    public void delete(String key) throws JAXBException {
        KVItemCollection storedItems = this.deserializeItem();
        boolean isRemoved = storedItems.parts.removeIf(item -> item.key.equals(key));
        this.serializeAndPersistItem(storedItems);
        if (isRemoved){
            LOGGER.info("Key has been deleted.");
        }
        else {
            LOGGER.info("Key could not be found.");
        }
    }

    public static void main(String[] args) throws JAXBException {
        KVItem message = new KVItem();
        message.key = "abc";
        message.value = "def";

        KVItem message2 = new KVItem();
        message2.key = "ghj";
        message2.value = "klm";

        KVItem message3 = new KVItem();
        message3.key = "bla";
        message3.value = "ble";

        KVItem message4 = new KVItem();
        message4.key = "bla";
        message4.value = "blu";

        KVPersist persister = new KVPersist("a-store.jaxb");

        persister.get("bok");
        persister.put(message3);

        persister.get("bla");
        persister.delete("bla");

//        if (!message.toString().equals(storedMessage.toString())) throw new RuntimeException("Store failed! " + message + " vs " + storedMessage);
    }
}
