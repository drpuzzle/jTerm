/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serialmonitor;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *

 */
public class Command {

  private final StringProperty value = new SimpleStringProperty("");

  public String getValue() {
    return value.get();
  }

  public void setValue(String value) {
    this.value.set(value);
  }

  public StringProperty valueProperty() {

    return value;
  }
  private final StringProperty name = new SimpleStringProperty("");

  public String getName() {
    return name.get();
  }

  public void setName(String value) {
    name.set(value);
  }

  public StringProperty nameProperty() {
    return name;
  }
  private final StringProperty linefeed = new SimpleStringProperty("");

  public String getLinefeed() {
    return linefeed.get();
  }

  public void setLinefeed(String value) {
    linefeed.set(value);
  }

  public StringProperty linefeedProperty() {
    return linefeed;
  }
  private final StringProperty script = new SimpleStringProperty("");

  public String getScript() {
    return script.get();
  }

  public void setScript(String value) {
    script.set(value);
  }

  public StringProperty scriptProperty() {
    return script;
  }
  
  
  
  
}
