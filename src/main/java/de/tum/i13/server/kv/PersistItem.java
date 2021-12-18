package de.tum.i13.server.kv;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PersistItem {

    @XmlElement(required = true)
    public String key;

    @XmlElement(required = true)
    public String value;

    public PersistItem() {

    }
    public PersistItem(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return key+"\t"+value;
    }
}