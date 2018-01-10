/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serialmonitor;

import javafx.beans.property.ListProperty;

/**
 *
 * @author drpuzzle
 */
public interface MonitorRunnable extends  Runnable {

  public ListProperty deviceListProperty();
  public String getDeviceInfo(String dev);
}
