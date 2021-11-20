package de.tum.i13.server.kv;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class KVItem {

    @XmlElement(required = true)
    public String key;

    @XmlElement(required = true)
    public String value;

    public KVItem() {

    }
    public KVItem(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return "KVitem{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}