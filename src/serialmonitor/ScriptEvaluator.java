/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serialmonitor;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Alert;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.log4j.Logger;


public class ScriptEvaluator {

  Logger logger = Logger.getLogger(ScriptEvaluator.class);
  
  public static String defaultScript = ""
          + "/*\n"
          + "* evaluation function after 'Enter' or \n"
          + "* 'Send Button' is pressed\n"
          + "* @param data: byte array of textfield input\n"
          + "* @return : byte array to send\n"
          + "*/\n"
          + "function eval(data) {\n"
          + "\t// output array\n"
          + "\tvar array=[];\n"
          + "\t// put orginal data to array\n"
          + "\tarray = array.concat(Java.from(data));\n"
          + "\t// add line feed at the end	\n"
          + "\tarray = array.concat(10);\n"
          + "\t// return data\n"
          + "\treturn array;\n"          
          + "}\n";

  private final StringProperty script = new SimpleStringProperty(defaultScript);

  public String getScript() {
    return script.get();
  }

  public void setScript(String value) {
    script.set(value);
  }

  public StringProperty scriptProperty() {
    return script;
  }
  
  
  
  public String evaluate(byte[] stringToSend){
    ScriptEngineManager manager = new ScriptEngineManager();
    String returnvalue="";
    for (ScriptEngineFactory sef : manager.getEngineFactories()){
      logger.info(""+sef.getEngineName()+", "+sef.getLanguageName());
    }
    ScriptEngine se = manager.getEngineByName("JavaScript");
    List<Byte> list = new ArrayList<>();
    
    for (byte b : stringToSend) {
      list.add(b);
    }

    try {
      se.eval(getScript());
      Invocable inv = (Invocable) se;
      ScriptObjectMirror to = ((ScriptObjectMirror) inv.invokeFunction("eval", list));
      int[] intArray = to.to(int[].class);
      byte[] byteArray = new byte[intArray.length];
      for (int i = 0; i< intArray.length;i++){
        byteArray[i] = (byte)(intArray[i]&0xFF);
      }
      returnvalue = new String(byteArray, Charset.forName("US-ASCII"));
    } catch (ScriptException | NoSuchMethodException ex) {
      Alert alert = new Alert(Alert.AlertType.WARNING);
      alert.setHeaderText("Error in Javascript");
      alert.setContentText(ex.getMessage());
      
      logger.warn(ex.getMessage());
      alert.showAndWait();
    }

    
    
    
    return returnvalue;
  }
  
  
}
