/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serialmonitor;

import java.nio.charset.Charset;
import java.util.Timer;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 *
 * @author fuerst
 */
@XmlRootElement(name = "SerialInterface")
@XmlAccessorType(XmlAccessType.NONE)
public class SerialInterface {

  private static final Logger LOG = LogManager.getLogger(SerialInterface.class.getSimpleName());
//   private CommPort commPort = null;
  private SerialPort serialPort = null;
//  private SerialReader serialEventListener = null;

  Timer timer = null;
  String device;
  private final IntegerProperty baudRate = new SimpleIntegerProperty(115200);
  private final ObjectProperty<Parity> parity = new SimpleObjectProperty<>(Parity.NONE);
  private final ObjectProperty<Stop> stopBit = new SimpleObjectProperty<>(Stop.BIT_1);
  private final ObjectProperty<Data> dataBits = new SimpleObjectProperty<>(Data.BITS_8);
  private final BooleanProperty connected = new SimpleBooleanProperty(false);
//  private final ListProperty<ByteData> commData = new SimpleListProperty<>(FXCollections.observableArrayList(new ArrayList<>()));
//  private final ListProperty<ByteData> txcommData = new SimpleListProperty<>(FXCollections.observableArrayList(new ArrayList<>()));
  private final IntegerProperty rxBytes = new SimpleIntegerProperty(0);
  private final IntegerProperty txBytes = new SimpleIntegerProperty(0);
  private final StringProperty lastRead = new SimpleStringProperty();

  public String getLastRead() {
    return lastRead.get();
  }

  public void setLastRead(String value) {
    lastRead.set(value);
  }

  public StringProperty lastReadProperty() {
    return lastRead;
  }
  private final StringProperty lastWrite = new SimpleStringProperty();

  public String getLastWrite() {
    return lastWrite.get();
  }

  public void setLastWrite(String value) {
    lastWrite.set(value);
  }

  public StringProperty lastWriteProperty() {
    return lastWrite;
  }

  public static enum Parity {
    NONE, ODD, EVEN
  }

  public static enum BaudRate {
    BAUD1200(1200), BAUD2400(2400),
    BAUD4800(4800), BAUD9600(9600),
    BAUD19200(19200), BAUD38400(38400),
    BAUD57600(57600), BAUD115200(115200);

    private final int value;

    BaudRate(final int newValue) {
      value = newValue;
    }

    public int getValue() {
      return value;
    }
  }

  public static enum Data {

    BITS_5(5), BITS_6(6), BITS_7(7), BITS_8(8);

    private final int value;

    Data(final int newValue) {
      value = newValue;
    }

    public int getValue() {
      return value;
    }
  }

  public static enum Stop {

    BIT_1(1), BIT_2(2), BIT_15(15);
    private final int value;

    Stop(final int newValue) {
      value = newValue;
    }

    public int getValue() {
      return value;
    }
  }

  // Konstruktor
  public SerialInterface() {
    super();
  }

//  boolean isConnected = false;
  public void close() {
    if (isConnected()) {
      setConnected(false);
      if (timer != null) {
        timer.cancel();
        timer.purge();
        timer = null;
      }
      try {

        serialPort.purgePort(SerialPort.PURGE_RXABORT | SerialPort.PURGE_TXCLEAR | SerialPort.PURGE_TXABORT | SerialPort.PURGE_RXCLEAR);
        boolean rv = serialPort.closePort();

//        serialPort = null;
        if (!rv) {
          LOG.warn("Port " + device + " could not be closed");
        }
      } catch (SerialPortException ex) {
        LOG.warn("Error Closing Port " + device + " (" + ex.toString() + ")");
      }
    }
    LOG.debug("Port closed: " + device);
  }

  private void handleNewChars() {
    try {
      String b;
//            b = serialPort.readString();
      byte c[] = serialPort.readBytes();
      if (c == null) {
        return;
      }
      // ISO-8859-1 encoding has a bijective  mapping from byte to char
      b = new String(c, Charset.forName("ISO-8859-1"));

      if ((lastRead.get() != null) && (lastRead.get().equals(b))) {
        lastRead.setValue("");
      }
      lastRead.setValue(b);
      Platform.runLater(() -> {
        setRxBytes(getRxBytes() + b.length());
      });

    } catch (SerialPortException ex) {
      LOG.error("SerialPortException: " + ex.getPortName() + " - " + ex.getExceptionType());
    }
  }

  public String connect(String device) {
    String rv = "Connected";
    if (serialPort == null) {
      serialPort = new SerialPort(device);

    } else {
      serialPort.setPortName(device);
    }
    setConnected(true);

    try {
      serialPort.openPort();
      applyInterfaceSettings();
      serialPort.purgePort(SerialPort.PURGE_RXABORT | SerialPort.PURGE_TXCLEAR | SerialPort.PURGE_TXABORT | SerialPort.PURGE_RXCLEAR);
      serialPort.addEventListener((SerialPortEvent spe) -> {
        if (spe.isRXCHAR()) {
          handleNewChars();
        }
      });

    } catch (SerialPortException ex) {
      setConnected(false);
      LOG.error("Error open Port " + device + " (" + ex.getMessage() + ")");
      rv = "Error: " + ex.getExceptionType();
      try {
        serialPort.closePort();
      } catch (SerialPortException ex1) {
        LOG.error("Error open Port " + device + " (" + ex1.getMessage() + ")");
        
      }
      return rv;
    }

//    timer = new Timer("SerialInterfaceTimer" + device, true);
//
//    timer.schedule(new TimerTask() {
//      
//      @Override
//      public void run() {
//        
//        if (isConnected()) {
//         handleNewChars();
//        }
//      }
//    }, 0, 1);
    LOG.debug("Port opened: " + device + ", Baudrate: " + getBaudRate());
    this.device = device;
    return rv;
  }

  private void applyInterfaceSettings() {
    if (isConnected() && (serialPort != null)) {
      try {
        serialPort.setParams(baudRate.get(), dataBits.get().ordinal() + 5, stopBit.get().ordinal() + 1, parity.get().ordinal());
        LOG.debug("Baud: " + baudRate.get() + ", Data: " + (dataBits.get().ordinal() + 5) + ", Stop: " + (stopBit.get().ordinal() + 1) + ", Parity: " + parity.get().ordinal());
      } catch (SerialPortException ex) {
        LOG.error("Error apply Interface Settings " + device + " (" + ex.getMessage() + ")");
      }

    }
  }

  public void write(String str) {

    if ((lastWrite.get() != null) && (lastWrite.get().equals(str))) {
      lastWrite.setValue("");
    }
    lastWrite.setValue(str);
    try {
      serialPort.writeString(str);
    } catch (SerialPortException ex) {
      LOG.error(ex.toString());
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // Getter and Setter Methods
  //////////////////////////////////////////////////////////////////////////////////////////////////
  @XmlElement
  public int getBaudRate() {
    return baudRate.get();
  }

  public void setBaudRate(int value) {
    baudRate.set(value);
    applyInterfaceSettings();
  }

  public IntegerProperty baudRateProperty() {
    return baudRate;
  }

  @XmlElement
  public Stop getStopBit() {
    return stopBit.get();
  }

  public void setStopBit(Stop value) {
    stopBit.set(value);
    applyInterfaceSettings();
  }

  public ObjectProperty stopBitProperty() {
    return stopBit;
  }

  public void setStopBit(int value) {
    for (Stop d : Stop.values()) {
      if (d.getValue() == value) {
        setStopBit(d);
      }
    }
  }

  @XmlElement
  public Parity getParity() {
    return parity.get();
  }

  public void setParity(Parity value) {
    parity.set(value);
    applyInterfaceSettings();
  }

  public ObjectProperty parityProperty() {
    return parity;
  }

  public void setParity(int value) {
    for (Parity d : Parity.values()) {
      if (d.ordinal() == value) {
        setParity(d);
      }
    }
  }

  @XmlElement
  public Data getDataBits() {
    return dataBits.get();
  }

  public void setDataBits(Data value) {
    dataBits.set(value);
    applyInterfaceSettings();
  }

  public ObjectProperty dataBitsProperty() {
    return dataBits;
  }

  public void setDataBits(int value) {
    for (Data d : Data.values()) {
      if (d.getValue() == value) {
        setDataBits(d);
      }
    }
  }

//  public ObservableList getCommData() {
//    return commData.get();
//  }
//
//  public void setCommData(ObservableList value) {
//    commData.set(value);
//  }
//
//  public ListProperty txCommDataProperty() {
//    return txcommData;
//  }
//public ObservableList getTxCommData() {
//    return txcommData.get();
//  }
//
//  public void setTxCommData(ObservableList value) {
//    txcommData.set(value);
//  }
//
//  public ListProperty commDataProperty() {
//    return commData;
//  }
  public boolean isConnected() {
    return connected.get();
  }

  public void setConnected(boolean value) {
    connected.set(value);
  }

  public BooleanProperty connectedProperty() {
    return connected;
  }

  public int getTxBytes() {
    return txBytes.get();
  }

  public void setTxBytes(int value) {
    txBytes.set(value);
  }

  public IntegerProperty txBytesProperty() {
    return txBytes;
  }

  public int getRxBytes() {
    return rxBytes.get();
  }

  public void setRxBytes(int value) {
    if (rxBytes != null) {
      rxBytes.set(value);
    }
  }

  public IntegerProperty rxBytesProperty() {
    return rxBytes;
  }
}
