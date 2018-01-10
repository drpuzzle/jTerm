/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serialmonitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.freedesktop.libudev.Enumerate;
import org.freedesktop.libudev.LibUdev;
import org.freedesktop.libudev.ListEntry;
import org.freedesktop.libudev.Monitor;

/**
 *
 * @author drpuzzle
 */
public class DeviceMonitorUdev implements MonitorRunnable {

  private boolean interrupt = false;
  private final Logger logger = LogManager.getLogger(DeviceMonitorUdev.class.getSimpleName());
  private final ListProperty<String> deviceList = new SimpleListProperty<>(FXCollections.observableArrayList(new ArrayList<>()));
  private final Map<String, org.freedesktop.libudev.Device> deviceMap = new HashMap<>();

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

  public void startThread() {
    Thread th = new Thread(this);
    th.setDaemon(true);
    th.setName("Monitoring Thread");
    th.start();
  }

  public void interrupt() {
    interrupt = true;
  }

  private void addDevice(org.freedesktop.libudev.Device dev) {
    // Register new Device in DeviceManager        
    logger.trace("Found Device: " + dev.getDevnode());
    deviceList.get().add(dev.getDevnode());
    deviceMap.put(dev.getDevnode(), dev);

  }

  private void scanDevices() {
    // Create the udev object 
    LibUdev udev = LibUdev.create();
    logger.debug("Enter DeviceMonitorUdev Run");
    // Get Startup Devices
    Enumerate enumerate = Enumerate.create(udev);

    // Look for tty-like Devices
    if (enumerate.addMatchSubsystem("tty") < 0) {
      logger.error("Error when adding tty subsystem match.");
      return;
    }
    if (enumerate.scanDevices() < 0) {
      logger.error("Error when scanning devices.");
      return;
    }

    // Get list of devices and loop 
    ListEntry devices = enumerate.getListEntry();
    for (ListEntry devListEntry = devices;
            devListEntry != null;
            devListEntry = devListEntry.getNext()) {

      /* Get the filename of the /sys entry for the device
       * and create a udev_device object (dev) representing it */
      String path = devListEntry.getName();

      org.freedesktop.libudev.Device dev = org.freedesktop.libudev.Device.newFromSyspath(udev, path);
      if (dev == null) {
        continue;
      }

      // Filter only valid tty Ports
      // Up to now only USB Ports are valid
      if (dev.getPropertyValue("ID_BUS") != null) {
        addDevice(dev);

      } else {
      }
    }

  }

  @Override
  @SuppressWarnings("SleepWhileInLoop")
  public void run() {
    logger.debug("Enter DeviceMonitorUdev Run");

    // Single Scan at startup
    scanDevices();

    // Permanent scan
    LibUdev udev = LibUdev.create();

    // Notification on dynamic changes      
    Monitor m = Monitor.newFromNetlink(udev, "udev");

    // Mögliche Subsysteme: ls /sys/class
    // atm        graphics       ieee1394_protocol  printer       thermal
    // backlight  hidraw         input              rfkill        tty
    // bdi        hwmon          mem                scsi_device   usb
    // block      i2c-adapter    misc               scsi_disk     vc
    // bluetooth  ide_port       net                scsi_generic  video_output
    // dma        ieee1394       pci_bus            scsi_host     vtconsole
    // dmi        ieee1394_host  power_supply       sound
    // firmware   ieee1394_node  ppdev              spi_master
    m.addMatchSubsystemDevtype("tty", null);
//    m.addMatchSubsystemDevtype("usb", null);

    m.enableReceiving();

    // Thread main loop
    // Wait on new udev Devices
    while (!interrupt) {
      // Pause Thread for 100ms
      try {
        Thread.sleep(100);
      } catch (InterruptedException ex) {
        logger.fatal(ex.getMessage());

      }
      // Get new Device (if return value == null no new device found)
      org.freedesktop.libudev.Device dev = m.receiveDevice();
      if (dev == null) {
        continue;
      }

      // Different Action
      switch (dev.getAction()) {
        case "add":
          addDevice(dev);

          break;
        case "remove":
          // Remove tty Device in DeviceManager
          logger.trace("Remove Device: " + dev.getDevnode());
          deviceList.get().remove(dev.getDevnode());
          deviceMap.remove(dev.getDevnode());
          break;
        default:
          // should not occur
          logger.info("Unknown Action " + dev.getAction() + "of device " + dev.getDevnode());
      }

    }
  }

  @Override
  public String getDeviceInfo(String dev) {
    if (deviceMap.containsKey(dev)) {
      String str = "";
      ListEntry ll = deviceMap.get(dev).getSysattr();
      do {
        str += ll.getName() + ":\t" + deviceMap.get(dev).getSysattrValue(ll.getName()) + "\n";
      } while ((ll = ll.getNext()) != null);
      ll = deviceMap.get(dev).getProperties();
      do {
        str += ll.getName() + ":\t" + deviceMap.get(dev).getPropertyValue(ll.getName()) + "\n";
      } while ((ll = ll.getNext()) != null);
      ll = deviceMap.get(dev).getTags();
      do {
        str += ll.getName() + ":\t" + deviceMap.get(dev).getPropertyValue(ll.getName()) + "\n";
      } while ((ll = ll.getNext()) != null);

      return str;
    } else {
      return "";
    }
  }

}
