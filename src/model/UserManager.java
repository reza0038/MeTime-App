package model;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserManager {
    private static final String USER_FILE = "xml/user.xml";
    private static List<User> userList = new ArrayList<>();

    static {
        loadUsers();
    }

    public static void loadUsers() {
        File file = new File(USER_FILE);
        if (!file.exists()) {
            saveUsers(); 
            return;
        }

        XStream xstream = new XStream(new StaxDriver());
        xstream.addPermission(AnyTypePermission.ANY);
        xstream.alias("user", User.class);
        xstream.alias("users", List.class);

        try (FileReader reader = new FileReader(USER_FILE)) {
            userList = (List<User>) xstream.fromXML(reader);
        } catch (Exception e) {
            e.printStackTrace();
            userList = new ArrayList<>(); 
        }

        if (userList == null) {
            userList = new ArrayList<>();
        }
    }

    public static void saveUsers() {
        XStream xstream = new XStream(new StaxDriver());
        xstream.addPermission(AnyTypePermission.ANY);
        xstream.alias("user", User.class);
        xstream.alias("users", List.class);
        
        
        try {
           
            File file = new File(USER_FILE);
            File parentDir = file.getParentFile();

            if (parentDir != null && !parentDir.exists()) {
                System.out.println("Membuat direktori: " + parentDir.getPath());
                parentDir.mkdirs(); 
            }

           
            FileWriter writer = new FileWriter(file);
            xstream.toXML(userList, writer);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static User getUserByUsername(String username) {
        if (userList == null) return null;
        for (User user : userList) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
    }

    public static void updateUser(User updatedUser) {
        if (userList == null) return;
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getUsername().equalsIgnoreCase(updatedUser.getUsername())) {
                userList.set(i, updatedUser);
                saveUsers();
                return;
            }
        }
    }

    public static void addUser(User user) {
        if (userList == null) {
            userList = new ArrayList<>();
        }
        userList.add(user);
        saveUsers();
    }

    public static List<User> getAllUsers() {
        return userList;
    }
}