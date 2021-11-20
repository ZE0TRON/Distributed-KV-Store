package de.tum.i13.server.kv;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class KVItemCollection  {
    public List<KVItem> parts = new ArrayList<>();

    @Override
    public String toString() {
        return "KVItemCollection{" +
                "parts=" + parts +
                '}';
    }
}
