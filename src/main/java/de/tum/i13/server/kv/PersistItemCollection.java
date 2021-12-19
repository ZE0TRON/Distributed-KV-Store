package de.tum.i13.server.kv;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

@XmlRootElement
public class PersistItemCollection {
    public ArrayList<PersistItem> parts = new ArrayList<>();

    @Override
    public String toString() {
        return "PersistItemCollection{" +
                "parts=" + parts +
                '}';
    }
}
