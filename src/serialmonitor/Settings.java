/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package serialmonitor;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import java.io.*;
import java.util.*;

/**
 *
 * @author Martin
 */
public class Settings extends Object {

    private static Settings instance = null;
    private static Properties defaultProperties;
    private static Properties properties;
    private static Properties propertiesFromDefault;
    private static final Logger logger = LogManager.getLogger(Settings.class.getSimpleName());

//   private SortedProperties tmp;
    private Settings() {
        defaultProperties = new Properties() {
            public static final long serialVersionUID = 30L;

            @Override
            public Set<Object> keySet() {
                return Collections.unmodifiableSet(new TreeSet<>(super.keySet()));
            }

            @Override
            public synchronized Enumeration<Object> keys() {
                return Collections.enumeration(new TreeSet<>(super.keySet()));
            }
        };
        properties = new Properties(defaultProperties) {
            public static final long serialVersionUID = 31L;

            @Override
            public Set<Object> keySet() {
                return Collections.unmodifiableSet(new TreeSet<>(super.keySet()));
            }

            @Override
            public synchronized Enumeration<Object> keys() {
                return Collections.enumeration(new TreeSet<>(super.keySet()));
            }
        };
        propertiesFromDefault = new Properties();
        load();

    }

    private static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    private void load() {

        try {
            propertiesFromDefault.load(Settings.class.getResourceAsStream("/settings.properties"));
        } catch (IOException | NullPointerException e) {
            logger.warn("Default Datei \"settings.properties\"nicht gefunden!.");
        }

        try {
//          logger.info("Load settings");
            properties.load(new FileInputStream("settings.properties"));
//            logger.info("Loaded settings");

        } catch (IOException ex) {
            logger.warn("Datei \"settings.properties\"nicht gefunden! Versuche default Datei.");
        }
//      if (!loaded) {
//
//         try {
//            properties.loadFromXML(Settings.class.getResourceAsStream("/settings.xml"));
//         } catch (IOException ex) {
//            MyLoggingRegistry.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
//         }
//      }
    }

    public static void save() {
        getInstance();
        try {
//         tmp.putAll(properties);         
//         properties.storeToXML(new FileOutputStream("settings.xml"), "");
            properties.store(new FileOutputStream("settings.properties"), "");
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    public static double getValue(String var, double defaultValue) {
        double rv;
        String str = getValue(var, "" + defaultValue);
        rv = Double.parseDouble(str);
        return rv;
    }

    public static int getValue(String var, int defaultValue) {
        int rv;
        String str = getValue(var, "" + defaultValue);
        rv = Integer.parseInt(str);
        return rv;
    }

    public static boolean getValue(String var, boolean defaultValue) {
        boolean rv;
        String str = getValue(var, "" + defaultValue);
        rv = Boolean.parseBoolean(str);
        return rv;
    }

    public static long getValue(String var, long defaultValue) {
        long rv;
        String str = getValue(var, "" + defaultValue);
        rv = Long.parseLong(str);
        return rv;
    }

    public static String getValue(String var, String defaultValue) {
        String rv;
        getInstance();
        rv = properties.getProperty(var);
        if (properties.getProperty(var) == null) {
            rv = propertiesFromDefault.getProperty(var);
            if (rv == null) {
                rv = defaultValue;
            }
            properties.setProperty(var, rv);
        }
        return rv;
    }

    public static void setValue(String id, String value) {
        getInstance();
        properties.setProperty(id, value);
    }

    public static void setValue(String id, boolean value) {
        getInstance();
        setValue(id, "" + value);
    }

    @Override
    public String toString() {
        //String str = null;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        properties.list(new PrintStream(bout, true));
//      properties.list(System.out);
        return bout.toString();
    }

}
