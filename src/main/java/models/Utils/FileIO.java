package models.Utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileIO {

    // i genuinely dunno what to name this
    public static Set<String> filterIDFileBelow(String filenameWithoutTXT, int columnToCheck, int columnToGet, Set<String> parentIds) throws IOException {
        Set<String> ids = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/db/" + filenameWithoutTXT + ".txt"))) {
            for (String line; (line = br.readLine()) != null; ) {
                String[] parts = line.split(",");
                if (parentIds.contains(parts[columnToCheck].trim())) {
                    // store id
                    ids.add(parts[columnToGet].trim());
                }
            }
        }
        return ids;
    }

    // if using this
    // make sure to have this constructor in your class, example:
    /*
    public Supplier(String[] data) {
		supplier_id = data[0];
		supplier_name = data[1];
		company = data[2];
		phone_number = data[3];
		address = data[4];
	}
    */
    public static <T> ArrayList<T> getIDsAsObjects(Class<T> classname, String filenameWithoutTXT, Set<String> IDs) throws IOException, ReflectiveOperationException {
        ArrayList<T> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/db/" + filenameWithoutTXT + ".txt"))) {
            for (String line; (line = br.readLine()) != null;) {
                String[] parts = line.split(",");
                if (IDs.contains(parts[0].trim())) {
                    Constructor<T> constructor = classname.getConstructor(String[].class);
                    data.add(constructor.newInstance((Object) parts));
                }
            }
        }
        return data;
    }

}
