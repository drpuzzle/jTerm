/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serialmonitor;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.IntFunction;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import org.apache.log4j.Logger;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.richtext.model.Paragraph;
import org.reactfx.collection.LiveList;
import org.reactfx.value.Val;

/**
 *
 * @author drpuzzle
 */
public class FXMLDocumentController implements Initializable {

  Logger logger = Logger.getLogger(FXMLDocumentController.class);

  @FXML
  private Spinner<Integer> msSpinner;

//  @FXML
//  private TreeView<String> cmdTreeView;

  @FXML
  private Button connectButton;
  
//   @FXML
//    private AnchorPane centralpane;
   

  @FXML
  private ComboBox<String> devicesCombo;

  @FXML
  private CheckBox autoscrollCheck;

  @FXML
  private ComboBox<String> baudRateCombo;

  @FXML
  private ComboBox<String> dataBitsCombo;

  @FXML
  private ComboBox<String> stopbitsCombo;

  @FXML
  private ComboBox<String> parityCombo;

  @FXML
  private Label statusLabel;

  @FXML
  private Label connectionLabel;

  @FXML
  private Label debugLabel;

  @FXML
  private Button clearButton;

  @FXML
  private ComboBox<String> linefeedCombo;

  @FXML
  private TextField rxCounterField;

  @FXML
  private TextField txCounterField;

  @FXML
  private ToggleGroup formatToggleGroup;

  private StyleClassedTextArea mainTextArea;

  @FXML
  private ChoiceBox<String> inputFormatCombo;

  @FXML
  private TextField sendTextField;

  @FXML
  private RadioButton showTXDataRadio;

  @FXML
  private RadioButton showRXDataRadio;

  @FXML
  private ChoiceBox<String> sendOnEnterCombo;

  @FXML
  private Button sendButton;

  @FXML
  private BorderPane mainBorder;

  SerialInterface serial = new SerialInterface();

  Timer timer = null;

  List<String> historyList = new ArrayList<>();

  @FXML
  void sendButtonAction(ActionEvent event) {
    if (serial.isConnected()) {

      // textfieldentry
      String str = sendTextField.getText();

      // remove from history list
      if (historyList.contains(str)) {
        historyList.remove(str);
      }

      // add new entry in history list
      historyList.add(0, str);

      //append character selected in onEnterCombo
      switch (sendOnEnterCombo.getSelectionModel().getSelectedItem()) {
        case "LF":
          str += "\n";
          break;
        case "CR":
          str += "\r";
          break;
        case "CR+LF":
          str += "\r\n";
          break;
        default:
          break;
      }

      // send character
      serial.write(str);

      // clear textfield
      sendTextField.setText("");
    }
  }

  private boolean requestclear = false;

  @FXML
  void clearButtonAction(ActionEvent event) {
//    logger.info("" + mainTextArea.getStyleOfChar(2));
    Platform.runLater(() -> {
      mainTextArea.clear();
    });
    serial.setRxBytes(0);
    dataList.get().clear();
    numberOfBatches = 0;
    aktBatch = 0;
    lastEntryDirection = BYTE_UNKNOWN;
    lastTimeStamp = 0;
    lastLineMissing = msSpinner.getValue();
//    mainTextArea.setStyleClass(0, 100, "red");
//      
  }

  private void setConnectionLabel() {
    if (serial.isConnected()) {
      connectionLabel.setText(devicesCombo.getSelectionModel().getSelectedItem()
              + " ( "
              + baudRateCombo.getValue()
              + " / "
              + dataBitsCombo.getValue()
              + " / "
              + stopbitsCombo.getValue()
              + " / "
              + parityCombo.getValue()
              + " )");
    } else {
      connectionLabel.setText("");
    }
  }

  private void closePort() {

    serial.close();
    statusLabel.setText("Port closed: " + devicesCombo.getSelectionModel().getSelectedItem());
    connectButton.setText("Connect");
  }

  @FXML
  void connectButtonAction(ActionEvent event) {
    if (devicesCombo.getSelectionModel().isEmpty()) {
      statusLabel.setText("Select a valid Port");
    } else {

      if (serial.isConnected()) {
        closePort();

      } else {
        serial.setBaudRate(Integer.parseInt(baudRateCombo.getValue()));
        serial.setDataBits(Integer.parseInt(dataBitsCombo.getValue()));
        serial.setParity(SerialInterface.Parity.valueOf(parityCombo.getValue()));
        serial.setStopBit(Integer.parseInt(stopbitsCombo.getValue()));
        String rv = serial.connect(devicesCombo.getSelectionModel().getSelectedItem());
        statusLabel.setText(devicesCombo.getSelectionModel().getSelectedItem() + ":  " + rv);
        if (serial.isConnected()) {
          connectButton.setText("Disconnect");
        }
      }
    }
    setConnectionLabel();
  }

   public static String[] Split(String text, int chunkSize, int maxLength) { 
        char[] data = text.toCharArray();       
        int len = Math.min(data.length,maxLength);
        String[] result = new String[(len+chunkSize-1)/chunkSize];
        int linha = 0;
        for (int i=0; i < len; i+=chunkSize) {
            result[linha] = new String(data, i, Math.min(chunkSize,len-i));
            linha++;
        }
        return result;
    }

  
  private static final int BYTE_READ = 0;
  private static final int BYTE_WRITE = 1;
  private static final int BYTE_UNKNOWN = 2;
  private final ListProperty<DisplayData> dataList = new SimpleListProperty<>(FXCollections.observableArrayList(new ArrayList<>()));

  private int numberOfBatches = 0;
  private int aktBatch = 0;
  private boolean isLastCharLF = true;
  private int lastEntryDirection = BYTE_READ;
  private long lastTimeStamp = 0;
  private String debugString = "";
  private int lastLineMissing=0;
  
  
  
  

  // function called every 10 ms
  private void updateTextField() {
    long startTime = System.nanoTime();
    // local variables
    // string composed for area update
    String newEntry = "";
    // local copy of numberOfBatches
    int number = numberOfBatches;
    // variable to store direction for correct formatting
    int aktEntryDirection = BYTE_READ;

    // buffer to store data
    DisplayData dd;
    // string to compose single datastring
    String newString = "";
    // local copy of requestclear
    boolean requestclearCopy = requestclear;
    // loop variable
    int i;

    // requestclear handled by local copy
    requestclear = false;

    // if clear is requested start from beginning of list
    if (requestclearCopy) {
      aktBatch = 0;
      lastEntryDirection = BYTE_UNKNOWN;
      lastTimeStamp = 0;
      lastLineMissing = msSpinner.getValue();
    }

    // cycle through list
    for (i = aktBatch; i < number; i++) {

      // get current list entry
      dd = dataList.get().get(i);

      // if direction of entry (TX) is not shown continue
      if ((dd.direction == BYTE_WRITE) && !showTXDataRadio.isSelected()) {
        continue;
      }
      // if direction of entry (RX) is not shown continue
      if ((dd.direction == BYTE_READ) && !showRXDataRadio.isSelected()) {
        continue;
      }

      // if direction changes add newline char to textfield (except first line or newline is already present)
      if ((dd.direction != lastEntryDirection)
              && (lastEntryDirection != BYTE_UNKNOWN)
              && (!isLastCharLF)) {
        newEntry += "\n";
      } // if timedifference is greater than selected value add newlinechar (except first line or newline is already present)
      else if ((lastTimeStamp > 0)
              && ((dd.timestamp - lastTimeStamp) > msSpinner.getValue())
              && (!isLastCharLF)
              && ("ms".equals(linefeedCombo.getSelectionModel().getSelectedItem()))) {
        newEntry += "\n";
      }
      // store timestamp value
      lastTimeStamp = dd.timestamp;

      // if direction changes break loop and display data with specific formating 
      if (dd.direction != lastEntryDirection) {
        // store aktual batch
        aktBatch = i;
        // store direction
        lastEntryDirection = dd.direction;
        // break loop
        break;
      }
      // store direction
      aktEntryDirection = dd.direction;

      // decide hex or ascii
      if (((RadioButton) formatToggleGroup.getSelectedToggle()).getText().equals("ASCII")) {
        // replace LF with symbol
        newString = dd.bytes.replace("\n", "\u2424");
        // replace NULL string with symbol
        newString = newString.replace("\0", "\u2400");
        // replace TAB with symbol
        newString = newString.replace("\t", "\u2409");

        // if newline at LF is selected add newline
        if ("LF".equals(linefeedCombo.getSelectionModel().getSelectedItem())) {
          newString = newString.replace("\u2424", "\u2424\n");
        }
        
        // fixed length
        if ("char".equals(linefeedCombo.getValue())){
          // store buffer
          String bufferStr = newString;
          // clear newString
          newString = "";
          while (bufferStr.length() > 0){
            String str;
            if (bufferStr.length()>= lastLineMissing )
              str = bufferStr.substring(0, lastLineMissing);
            else 
              str = bufferStr;
            newString += str;
            lastLineMissing = lastLineMissing - str.length();
            bufferStr = bufferStr.substring(str.length());
            if (lastLineMissing == 0){
              newString +="\n";
              lastLineMissing = msSpinner.getValue();
            }
            
          }
               
        }

      } else if (((RadioButton) formatToggleGroup.getSelectedToggle()).getText().equals("HEX")) {
        // empty string
        newString = "";
        // loop through bytes and add HEX representation
        for (byte b : dd.bytes.getBytes(Charset.forName("ISO-8859-1"))) {
          newString += String.format("%02X ", b);
          // if newline at LF is selected add newline
          if (("LF".equals(linefeedCombo.getSelectionModel().getSelectedItem()))
                  && (b == '\n')) {
            newString += "\n";
          }
        }
      }

      isLastCharLF = newString.endsWith("\n");
      //concatenate complete string
      newEntry += newString;
    }// end of loop

    // store current batchnumber
    aktBatch = i;

    // copy string to not loose entry in FX update thread
    String bufferStr = newEntry;

    // copy direction to not loose entry in FX update thread
    int bufferDirection = aktEntryDirection;

    // if new string is available append to FX update thread
    if (bufferStr.length() > 0) {

      Platform.runLater(() -> {
        // debug output
        debugLabel.setText(debugString);

        // clear area if requested
        if (requestclearCopy) {
          mainTextArea.clear();
        }

        // append new string to area
        mainTextArea.appendText(bufferStr);

        // do appropriate style 
        if (bufferDirection == BYTE_READ) {
          mainTextArea.setStyleClass(mainTextArea.getText().length() - bufferStr.length(), mainTextArea.getText().length(), "green");
        } else {
          mainTextArea.setStyleClass(mainTextArea.getText().length() - bufferStr.length(), mainTextArea.getText().length(), "red");
        }

        // if autoscroll then last paragraph is alwaiys selected
        if (autoscrollCheck.isSelected()) {
          mainTextArea.showParagraphAtBottom(mainTextArea.getCurrentParagraph());
        }

      });

    } else {
      // if only clear then clear
      if (requestclearCopy) {
        Platform.runLater(() -> {
          mainTextArea.clear();

        });
      }
    }
    long done = System.nanoTime() - startTime;
    if (done > 1000000) {
      debugString = String.format("%6.3f %%", (done * 1.0 / 1000000.));
    }

  }

  @Override
  public void initialize(URL url, ResourceBundle rb) {
    // TODO
    parityCombo.getItems().removeAll(baudRateCombo.getItems());
    parityCombo.getItems().addAll(Arrays.stream(SerialInterface.Parity.class.getEnumConstants()).map((aEnum) -> String.valueOf(aEnum.name())).toArray(String[]::new));
    parityCombo.getSelectionModel().select(Settings.getValue("Parity", "NONE"));
    parityCombo.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
      Settings.setValue("Parity", parityCombo.getSelectionModel().getSelectedItem());
      serial.setParity(SerialInterface.Parity.valueOf(parityCombo.getValue()));
      setConnectionLabel();
    });

    baudRateCombo.getItems().removeAll(baudRateCombo.getItems());
    baudRateCombo.getItems().addAll(Arrays.stream(SerialInterface.BaudRate.class.getEnumConstants()).map((aEnum) -> String.valueOf(aEnum.getValue())).toArray(String[]::new));
    baudRateCombo.getSelectionModel().select(Settings.getValue("Baudrate", "115200"));
    baudRateCombo.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
      Settings.setValue("Baudrate", baudRateCombo.getSelectionModel().getSelectedItem());
      serial.setBaudRate(Integer.parseInt(baudRateCombo.getValue()));
      setConnectionLabel();
    });

    dataBitsCombo.getItems().removeAll(dataBitsCombo.getItems());
    dataBitsCombo.getItems().addAll(Arrays.stream(SerialInterface.Data.class.getEnumConstants()).map((aEnum) -> String.valueOf(aEnum.getValue())).toArray(String[]::new));
    dataBitsCombo.getSelectionModel().select(Settings.getValue("Databits", "8"));
    dataBitsCombo.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
      Settings.setValue("Databits", dataBitsCombo.getSelectionModel().getSelectedItem());
      serial.setDataBits(Integer.parseInt(dataBitsCombo.getValue()));
      setConnectionLabel();

    });

    stopbitsCombo.getItems().removeAll(stopbitsCombo.getItems());
    stopbitsCombo.getItems().addAll(Arrays.stream(SerialInterface.Stop.class.getEnumConstants()).map((aEnum) -> String.valueOf(aEnum.getValue())).toArray(String[]::new));
    stopbitsCombo.getSelectionModel().select(Settings.getValue("Stopbits", "1"));
    stopbitsCombo.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
      Settings.setValue("Stopbits", stopbitsCombo.getSelectionModel().getSelectedItem());
      serial.setStopBit(Integer.parseInt(stopbitsCombo.getValue()));
      setConnectionLabel();
    });

    msSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, Settings.getValue("#ofms", 1), 1));
    msSpinner.setOnScroll(event -> {
      if (event.getDeltaY() < 0) {
        msSpinner.decrement();
      } else if (event.getDeltaY() > 0) {
        msSpinner.increment();
      }
    });
    msSpinner.valueProperty().addListener((ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) -> {
      Settings.setValue("#ofms", "" + newValue);
      requestclear = true;
    });
    linefeedCombo.getItems().removeAll(linefeedCombo.getItems());
    linefeedCombo.getItems().addAll("CR", "LF", "CR+LF", "NONE", "ms", "char");
    linefeedCombo.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
      if ((newValue != null) && (!"".equals(newValue))) {
        Settings.setValue("Linefeed", linefeedCombo.getSelectionModel().getSelectedItem());
        if (newValue.endsWith("ms") || newValue.endsWith("char")) {
          Platform.runLater(() -> {
            msSpinner.setDisable(false);
          });
        } else {
          Platform.runLater(() -> {
            msSpinner.setDisable(true);
          });
        }
        requestclear = true;
      }

    });
    linefeedCombo.getSelectionModel().select(Settings.getValue("Linefeed", "LF"));

    showTXDataRadio.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
      requestclear = true;
    });
    showRXDataRadio.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
      requestclear = true;
    });

    sendOnEnterCombo.getItems().clear();
    sendOnEnterCombo.getItems().addAll("CR", "LF", "CR+LF", "NONE", "Custom...");
    sendOnEnterCombo.getSelectionModel().select(Settings.getValue("SendOnEnter", "LF"));

    sendOnEnterCombo.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
      Settings.setValue("SendOnEnter", sendOnEnterCombo.getSelectionModel().getSelectedItem());
      if (newValue.equals("Custom...")) {
        logger.info("Select custom Value");
        // TODO Add dialog box to 
      }
    });

    inputFormatCombo.getItems().clear();
    inputFormatCombo.getItems().addAll("ASCII", "HEX");
    inputFormatCombo.getSelectionModel().select(Settings.getValue("InputFormat", "ASCII"));
    inputFormatCombo.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
      Settings.setValue("InputFormat", inputFormatCombo.getSelectionModel().getSelectedItem());
    });

    for (Toggle toggle : (formatToggleGroup.getToggles())) {
      if (((RadioButton) toggle).getText().equals(Settings.getValue("DisplayFormat", "ASCII"))) {
        ((RadioButton) toggle).setSelected(true);
      }
      ((RadioButton) toggle).selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
        if (newValue) {
//          logger.info("Selected: " + ((RadioButton) toggle).getText());
          Settings.setValue("DisplayFormat", ((RadioButton) toggle).getText());
        }
      });
    }

    formatToggleGroup.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
      requestclear = true;
    });

    mainTextArea = new StyleClassedTextArea();
    mainTextArea.setUndoManager(null);
//    scrollArea.setContent(mainTextArea);
    VirtualizedScrollPane vsp = new VirtualizedScrollPane<>(mainTextArea);
    mainTextArea.setAutoScrollOnDragDesired(true);
    mainTextArea.setCache(true);
    
//    centralpane.getChildren().add(vsp);
//    AnchorPane.setBottomAnchor(vsp, 0.);
//    AnchorPane.setLeftAnchor(vsp, 0.);
//    AnchorPane.setRightAnchor(vsp, 0.);
//    AnchorPane.setTopAnchor(vsp, 0.);
    mainBorder.setCenter(vsp);

    StringConverter<Number> sc = new StringConverter<Number>() {
      @Override
      public String toString(Number i) {
        return "" + i;
      }

      @Override
      public Number fromString(String s) {

        return Integer.parseInt(s);

      }
    };

    devicesCombo.getItems().addListener((ListChangeListener.Change<? extends String> c) -> {
      if (serial.isConnected()) {
        c.next();
        if (c.getRemoved().contains(serial.device)) {
          Platform.runLater(() -> {
            closePort();
            setConnectionLabel();
          });
        }
      }
    });

    Bindings.bindBidirectional(rxCounterField.textProperty(), serial.rxBytesProperty(), sc);
    Bindings.bindBidirectional(txCounterField.textProperty(), serial.txBytesProperty(), sc);
    logger.info("" + FXMLDocumentController.class.getResourceAsStream("/ModifiedFreeMono.ttf"));

    Font font = Font.loadFont(FXMLDocumentController.class.getResourceAsStream("/ModifiedFreeMono.ttf"), 10);
    logger.info(font.toString());
    mainTextArea.setStyle("-fx-font-size:11;-fx-font-family: FreeMono2; ");
    mainTextArea.setWrapText(true);
    MyLineNumberFactory mlnf = new MyLineNumberFactory(mainTextArea, digits -> "%1$" + digits + "s");
    mainTextArea.setParagraphGraphicFactory(mlnf);

    System.out.println(Arrays.toString(getMonoFontFamilyNames().toArray()));

    deviceDiscovery();

//    cmdTreeView.setCellFactory((TreeView<String> p) -> new TextFieldTreeCellImpl());
//
//    
//    
//    
//    
//    rootItem.setExpanded(true);
//    TreeItem<String> firstItem = new TreeItem<>("Commands");
//    rootItem.getChildren().add(firstItem);
//    for (int i = 1; i < 6; i++) {
//      TreeItem<String> item = new TreeItem<>("Message" + i);
//      firstItem.getChildren().add(item);
//    }
//
//    cmdTreeView.setRoot(rootItem);
//    cmdTreeView.setShowRoot(false);
//    createContextMenu();
//    cmdTreeView.setContextMenu(addMenu);
            
    serial.lastReadProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
      if (!"".equals(newValue)) {
        dataList.add(numberOfBatches, new DisplayData(numberOfBatches, BYTE_READ, System.currentTimeMillis(), newValue));
        numberOfBatches++;
      }
    });
    serial.lastWriteProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
      if (!"".equals(newValue)) {
        dataList.add(numberOfBatches, new DisplayData(numberOfBatches, BYTE_WRITE, System.currentTimeMillis(), newValue));
        numberOfBatches++;
      }
    });

    timer = new Timer("UI_UpdateTimer", true);

    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        updateTextField();
      }
    }, 0, 20);

  }
  
  TreeItem<String> rootItem = new TreeItem<>("Commands");
  private final ContextMenu addMenu = new ContextMenu();

    
    private void createContextMenu(){
      MenuItem addMenuItem = new MenuItem("Add List");
      addMenu.getItems().clear();
      addMenu.getItems().add(addMenuItem);
      addMenuItem.setOnAction(new EventHandler() {
        @Override
        public void handle(Event t) {
          TreeItem newItem
                  = new TreeItem<>("New List" );
          rootItem.getChildren().add(newItem);
        }
      });
      
    }
  

  public void deviceDiscovery() {
    // Indicate Start Discovery
    logger.info("Start Discovery");

    MonitorRunnable myRunnable = null;
    // Check Operatin System
    String osname = System.getProperty("os.name");
    logger.trace("Operating System: <" + osname + ">");
    switch (osname) {
      case "Linux":
        // Init Thread
        myRunnable = new DeviceMonitorUdev();
        break;
      case "Windows 7":
      case "Windows XP":
      case "Windows 8.1":
      case "Windows 8":
      case "Windows 10":
        myRunnable = new DeviceMonitorFtdi();
        break;
    }

    if (myRunnable != null) {

      myThread = new Thread(myRunnable);
      // Set Name of Thread
      myThread.setName(FXMLDocumentController.class.getSimpleName() + ".DeviceDiscovery");
      // Let Thread stop if application is shutdown
      myThread.setDaemon(true);
      // Start Thread
      myThread.start();

      myRunnable.deviceListProperty().addListener((ListChangeListener.Change c) -> {

        c.next();
        if (c.wasAdded()) {
          devicesCombo.getItems().addAll(c.getAddedSubList());
        } else {
          devicesCombo.getItems().removeAll(c.getRemoved());
        }
        if (devicesCombo.getSelectionModel().isEmpty()) {
          devicesCombo.getSelectionModel().select(0);
        }
      });
    }
  }
  Thread myThread;

  private ObservableList<String> getMonoFontFamilyNames() {

    // Compare the layout widths of two strings. One string is composed
    // of "thin" characters, the other of "wide" characters. In mono-spaced
    // fonts the widths should be the same.
    final Text thinTxt = new Text("1 labcdefgm"); // note the space
    final Text thikTxt = new Text("MWXABCDEFGX");

    List<String> fontFamilyList = Font.getFamilies();
    List<String> monoFamilyList = new ArrayList<>();

    Font font;

    for (String fontFamilyName : fontFamilyList) {
      font = Font.font(fontFamilyName, FontWeight.NORMAL, FontPosture.REGULAR, 14.0d);
      thinTxt.setFont(font);
      thikTxt.setFont(font);
      if (thinTxt.getLayoutBounds().getWidth() == thikTxt.getLayoutBounds().getWidth()) {
        monoFamilyList.add(fontFamilyName);
      }
    }

    return FXCollections.observableArrayList(monoFamilyList);
  }

  @FXML
  void keyPressedInputTextfield(KeyEvent event) {

    // on Keycode up and down cycle through historyList
    if (event.getCode() == KeyCode.UP) {

      // find current index 
      int index = historyList.indexOf(sendTextField.getText());

      // if index is inside historylist choose correct entry
      if ((index + 1) < historyList.size()) {

        sendTextField.setText(historyList.get(index + 1));

        // mark complete textfield
        sendTextField.selectAll();
      }

      // consume event so that the standard behaviour is not performed
      event.consume();
    } else if (event.getCode() == KeyCode.DOWN) {

      // find current index 
      int index = historyList.indexOf(sendTextField.getText());

      // if index is inside historylist choose correct entry
      if (index > 0) {

        sendTextField.setText(historyList.get(index - 1));

        // mark complete textfield
        sendTextField.selectAll();
      }

      // consume event so that the standard behaviour is not performed
      event.consume();
    }

  }

  public class MyLineNumberFactory implements IntFunction<Node> {

    private final Insets DEFAULT_INSETS = new Insets(0.0, 5.0, 0.0, 5.0);
    private final Paint DEFAULT_TEXT_FILL = Color.web("#666");
    private final Font DEFAULT_FONT
            = Font.font("monospace", FontPosture.ITALIC, 10);
    private final Background DEFAULT_BACKGROUND
            = new Background(new BackgroundFill(Color.web("#ddd"), null, null));

//    public IntFunction<Node> get(GenericStyledArea<?, ?, ?> area) {
//      return get(area, digits -> "%1$" + digits + "s ");
//    }
//    public static IntFunction<Node> get(
//            GenericStyledArea<?, ?, ?> area,
//            IntFunction<String> format) {
//      return new MyLineNumberFactory(area, format);
//    }
    private final Val<Integer> nParagraphs;
    private final IntFunction<String> format;
    private final LiveList<? extends Paragraph<?, ?, ?>> list;

    private MyLineNumberFactory(
            GenericStyledArea<?, ?, ?> area,
            IntFunction<String> format) {
      nParagraphs = LiveList.sizeOf(area.getParagraphs());
      list = area.getParagraphs();
      this.format = format;
    }

    @Override
    public Node apply(int idx) {
      Val<String> formatted = nParagraphs.map((Integer t) -> {
        try {
          if (((List<String>) list.get(idx).getParagraphStyle()).get(0).equals("green")) {
//            if (list.get(idx).getText().startsWith("\u2000")) {

            return format(idx + 1, t) + "\u21E6";
          } else if (((List<String>) list.get(idx).getParagraphStyle()).get(0).equals("red")) {
            return format(idx + 1, t) + "\u21E8";
          }
        } catch (IndexOutOfBoundsException e) {

        }
        return format(idx + 1, t) + " ";
      });
//                n -> format(idx+1, n));

      Label lineNo = new Label();
      lineNo.setFont(DEFAULT_FONT);
      lineNo.setBackground(DEFAULT_BACKGROUND);
      lineNo.setTextFill(DEFAULT_TEXT_FILL);
      lineNo.setPadding(DEFAULT_INSETS);
      lineNo.setAlignment(Pos.TOP_RIGHT);
      lineNo.getStyleClass().add("lineno");

      // bind label's text to a Val that stops observing area's paragraphs
      // when lineNo is removed from scene
      lineNo.textProperty().bind(formatted.conditionOnShowing(lineNo));

      return lineNo;
    }

    private String format(int x, int max) {
      int digits = (int) Math.floor(Math.log10(max)) + 1;
      return String.format(format.apply(digits), x);
    }
  }

  private final class TextFieldTreeCellImpl extends TreeCell<String> {

    private TextField textField;

    private final ContextMenu addMenu = new ContextMenu();

    
    private void createContextMenu(String str, boolean doParent){
      MenuItem addMenuItem = new MenuItem("Add "+str);
      addMenu.getItems().clear();
      addMenu.getItems().add(addMenuItem);
      addMenuItem.setOnAction(new EventHandler() {
        @Override
        public void handle(Event t) {
          TreeItem newItem
                  = new TreeItem<>("New " + str);
          if (doParent) {
            if (getTreeItem() != null) {
              getTreeItem().getParent().getChildren().add(newItem);
            }
          } else {
            getTreeItem().getChildren().add(newItem);
          }
        }
      });
      
    }
    
    
    public TextFieldTreeCellImpl() {
      
    }

    @Override
    public void startEdit() {
      super.startEdit();

      if (textField == null) {
        createTextField();
      }
      setText(null);
      setGraphic(textField);
      textField.selectAll();
    }

    @Override
    public void cancelEdit() {
      super.cancelEdit();
      setText((String) getItem());
      setGraphic(getTreeItem().getGraphic());
    }

    @Override
    public void updateItem(String item, boolean empty) {
      super.updateItem(item, empty);

      if (empty) {
        setText(null);
        setGraphic(null);
      } else {
        if (isEditing()) {
          if (textField != null) {
            textField.setText(getString());
          }
          setText(null);
          setGraphic(textField);
        } else {
          setText(getString());
          setGraphic(getTreeItem().getGraphic());
          if ( (getTreeItem().isLeaf() && getTreeItem().getParent().getParent()!=null) ) {
            createContextMenu("Command", true);
            setContextMenu(addMenu);
          } else if (!getTreeItem().isLeaf() ) {
            createContextMenu("Command", false);
            setContextMenu(addMenu);
          }
        }
      }
    }

    private void createTextField() {
      textField = new TextField(getString());
      textField.setOnKeyReleased((KeyEvent t) -> {
        if (t.getCode() == KeyCode.ENTER) {
          commitEdit(textField.getText());
        } else if (t.getCode() == KeyCode.ESCAPE) {
          cancelEdit();
        }
      });
    }

    private String getString() {
      return getItem() == null ? "" : getItem();
    }
  }

}
