/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serialmonitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jssc.SerialPortList;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 *
 * @author drpuzzle
 */
public class DeviceMonitorFtdi implements MonitorRunnable {

  private boolean interrupt = false;
  private final Logger logger = LogManager.getLogger(DeviceMonitorFtdi.class.getSimpleName());

  private static final int MAX_RETRIES = 10;
  Random r = new Random(System.currentTimeMillis());

  private final ListProperty<String> deviceList = new SimpleListProperty<>(FXCollections.observableArrayList(new ArrayList<>()));

  public ObservableList getDeviceList() {
    return deviceList.get();
  }

  public void setDeviceList(ObservableList value) {
    deviceList.set(value);
  }

  @Override
  public ListProperty deviceListProperty() {
    return deviceList;
  }

  public void interrupt() {
    interrupt = true;
  }

  @Override
  @SuppressWarnings("SleepWhileInLoop")
  public void run() {
    logger.debug("Enter DeviceMonitorFtdi Run");
//        JD2XX jd = new JD2XX();
//        logger.debug("FTDI Library Version: " + String.format("%08X", jd.getLibraryVersion()));
//
//        Map<String, String> deviceList2 = new HashMap<>();
//        Map<String, Integer> deviceUsed = new HashMap<>();
//
//        try {
//            logger.trace("Request Device List");
//            Object[] devs = jd.listDevicesBySerialNumber();
//
//            for (int i = 0; i < devs.length; ++i) {
//                // Log New Found Device
//
//                deviceList2.put(devs[i].toString(), "");
//                deviceUsed.put(devs[i].toString(), 0);
//                // Extract ComPortNumber
//                jd.openBySerialNumber(devs[i].toString());
//                logger.trace("Found Device: " + devs[i].toString() + " (Driver Version: " + String.format("%08X", jd.getDriverVersion()) + ")");
//                String commport = "COM" + jd.getComPortNumber();
//
//                deviceList2.put(devs[i].toString(), commport);
//                deviceUsed.put(devs[i].toString(), MAX_RETRIES);
//                deviceList.add(commport);
//                jd.resetPort();
//                jd.close();
//
//                // Wait until Port is closed 
//                // Time could be optimized
////        Thread.sleep(1000);
//                // Generate New Device
////        Device d = new Device(devs[i].toString(), "SerialInterface", commport);
//                // Register new Device
////        DeviceRegister.getInstance().addDevice(d.getSerial(), d);
//            }
//        } catch (IOException ex) {
//            logger.error("Error: " + ex.toString());
//        }

    while (!interrupt) {
      // All COM Ports in List         
//        List<String> portNamesList = new ArrayList<>(Arrays.asList(SerialPortList.getPortNames()));
      List<String> portNamesList = new ArrayList<>(Arrays.asList(SerialPortList.getPortNames()));
//logger.info(Arrays.deepToString(portNamesList.toArray()));
// add new entries
      for (String name : portNamesList) {
        if (!deviceList.contains(name)) {
          deviceList.add(name);
        }
      }

// remove old entries
      List<String> notfound = new ArrayList<>();
      for (String name : deviceList) {
        if (!portNamesList.contains(name)) {
          notfound.add(name);
        }

      }
      deviceList.removeAll(notfound);
      try {
        Thread.sleep(200);
      } catch (InterruptedException ex) {
        logger.error("DeviceMonitorFtdi Interrupted: " + ex.toString());
      }
    }

  }

  @Override
  public String getDeviceInfo(String dev) {
    logger.info("Device Information not supported");
    return "Device Information not supported";
  }

}
