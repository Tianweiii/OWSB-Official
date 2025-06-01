package models.Utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

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

    public static HashMap<String, String> filterIDToHashMap(String filenameWithoutTXT, int columnToCheck, int columnToKey, int columnToValue, String id) throws IOException {
        HashMap<String, String> data = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/db/" + filenameWithoutTXT + ".txt"))) {
            for (String line; (line = br.readLine()) != null; ) {
                String[] parts = line.split(",");
                if (parts[columnToCheck].trim().equals(id)) {
                    // store id
                    data.put(parts[columnToKey], parts[columnToValue]);
                }
            }
        }
        return data;
    }

    public static Set<String> filterIDFileBelow(String filenameWithoutTXT, int columnToCheck, int columnToGet, String targetID) throws IOException {
        Set<String> ids = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/db/" + filenameWithoutTXT + ".txt"))) {
            for (String line; (line = br.readLine()) != null;) {
                String[] parts = line.split(",");
                if (parts[columnToCheck].trim().equals(targetID)) {
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

    public static <T> ArrayList<T> getIDsAsObjects(Class<T> classname, String filenameWithoutTXT, String targetID) throws IOException, ReflectiveOperationException {
        ArrayList<T> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/db/" + filenameWithoutTXT + ".txt"))) {
            for (String line; (line = br.readLine()) != null;) {
                String[] parts = line.split(",");
                if (parts[0].trim().equals(targetID)) {
                    Constructor<T> constructor = classname.getConstructor(String[].class);
                    data.add(constructor.newInstance((Object) parts));
                }
            }
        }
        return data;
    }

    public static <T> ArrayList<T> getIDsAsObjects(Class<T> classname, String filenameWithoutTXT, String targetID, int columnToCheck) throws IOException, ReflectiveOperationException {
        ArrayList<T> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/db/" + filenameWithoutTXT + ".txt"))) {
            for (String line; (line = br.readLine()) != null;) {
                String[] parts = line.split(",");
                if (parts[columnToCheck].trim().toLowerCase().equals(targetID)) {
                    Constructor<T> constructor = classname.getConstructor(String[].class);
                    data.add(constructor.newInstance((Object) parts));
                }
            }
        }
        return data;
    }

    public static <T> T getIDsAsObject(Class<T> classname, String filenameWithoutTXT, String targetID) throws IOException, ReflectiveOperationException {
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/db/" + filenameWithoutTXT + ".txt"))) {
            for (String line; (line = br.readLine()) != null;) {
                String[] parts = line.split(",");
                if (parts[0].trim().equals(targetID)) {
                    Constructor<T> constructor = classname.getConstructor(String[].class);
                    return constructor.newInstance((Object) parts);
                }
            }
        }
        System.out.println("No data found.");
        return null;
    }

    public static String getXFromID(String filenameWithoutTXT, int columnToCheck, int columnToGet, String targetID) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/db/" + filenameWithoutTXT + ".txt"))) {
            for (String line; (line = br.readLine()) != null; ) {
                String[] parts = line.split(",");
                if (parts[columnToCheck].trim().equals(targetID)) {
                    return parts[columnToGet];
                }
            }
        }
        return "";
    }

    public static <T> ArrayList<T> getObjectsFromXLines(Class<T> classname, String filenameWithoutTXT, int lines) throws IOException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        ArrayList<T> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/db/" + filenameWithoutTXT + ".txt"))) {
            for (int i = 0; i < lines + 1; i++) {
                String line = br.readLine();
                String[] parts = line.split(",");
                Constructor<T> constructor = classname.getConstructor(String[].class);
                list.add(constructor.newInstance((Object) parts));
            }
        }
        return list;
    }

    public static <T> T getObjectFromID(Class<T> classname, String filenameWithoutTXT, int indexToCheck, String targetID) throws IOException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/db/" + filenameWithoutTXT + ".txt"))) {
            for (String line; (line = br.readLine()) != null;) {
                String[] parts = line.split(",");
                if (parts[indexToCheck].trim().equals(targetID)) {
                    Constructor<T> constructor = classname.getConstructor(String[].class);
                    return constructor.newInstance((Object) parts);
                }
            }
        }
        System.out.println("No data found.");
        return null;
    }

    public static <T> ArrayList<T> getAllLines(Class<T> classname, String filenameWithoutTXT) throws IOException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        ArrayList<T> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/db/" + filenameWithoutTXT + ".txt"))) {
            for (String line; (line = br.readLine()) != null; ) {
                String[] parts = line.split(",");
                Constructor<T> constructor = classname.getConstructor(String[].class);
                list.add(constructor.newInstance((Object) parts));
            }
        }
        return list;
    }

    public static int getRowCount(String filenameWithoutTXT) throws IOException {
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/db/" + filenameWithoutTXT + ".txt"))) {
            while (br.readLine() != null) {
                count ++;
            }
        }
        return count;
    }

    public static double getCountOfX(String filenameWithoutTXT, int indexToReceive) throws IOException {
        double amount = 0;
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/db/" + filenameWithoutTXT + ".txt"))) {
            for (String line; (line = br.readLine()) != null; ) {
                String[] parts = line.split(",");
                    amount += Double.parseDouble(parts[indexToReceive].trim());
            }
        }
        return amount;
    }

    public static double getCountOfX(String filenameWithoutTXT, int indexToCheck, int indexToReceive, String condition) throws IOException {
        double amount = 0;
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/db/" + filenameWithoutTXT + ".txt"))) {
            for (String line; (line = br.readLine()) != null; ) {
                String[] parts = line.split(",");
                if (parts[indexToCheck].trim().toLowerCase().equals(condition)) {
                    amount += Double.parseDouble(parts[indexToReceive].trim());
                }
            }
        }
        return amount;
    }

    public static <T> ArrayList<T> getObjectsFromLastXLines(Class<T> classname, String filenameWithoutTXT, int lines)
            throws IOException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Deque<String> lastLines = new ArrayDeque<>(lines);

        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/db/" + filenameWithoutTXT + ".txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                lastLines.addLast(line);

                if (lastLines.size() > lines) {
                    lastLines.removeFirst();
                }
            }
        }

        ArrayList<T> list = new ArrayList<>();
        Constructor<T> constructor = classname.getConstructor(String[].class);

        for (String line : lastLines) {
            String[] parts = line.split(",");
            list.add(constructor.newInstance((Object) parts));
        }

        return list;
    }
}
