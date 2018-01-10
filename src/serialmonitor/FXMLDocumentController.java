/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serialmonitor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.IntFunction;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SplitPane;
import javafx.scene.control.SplitPane.Divider;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.apache.log4j.Logger;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.Caret;
import org.fxmisc.richtext.GenericStyledArea;

import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.richtext.event.MouseOverTextEvent;
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
  private AnchorPane mainAnchorPane;

  @FXML
  private Label statusLabel;

  @FXML
  private Label debugLabel;

  @FXML
  private Label connectionLabel;

  @FXML
  private BorderPane mainBorder;

  @FXML
  private Button clearButton;

  @FXML
  private Spinner<Integer> msSpinner;

  @FXML
  private ComboBox<String> linefeedCombo;

  @FXML
  private CheckBox autoscrollCheck;

  @FXML
  private ToggleGroup formatToggleGroup;

  @FXML
  private RadioButton showTXDataRadio;

  @FXML
  private RadioButton showRXDataRadio;

  @FXML
  private VBox portBox1;

  @FXML
  private RadioButton showPort1Radio;

  @FXML
  private RadioButton showPort2Radio;

  @FXML
  private ChoiceBox<String> inputFormatCombo;

  @FXML
  private TextField sendTextField;

  @FXML
  private ChoiceBox<String> sendOnEnterCombo;

  @FXML
  private Button sendButton;
  @FXML
  private VBox portBox;

  @FXML
  private RadioButton port1Radio;

  @FXML
  private ToggleGroup portToggleGroup;

  @FXML
  private RadioButton port2Radio;

  @FXML
  private SplitPane mainSplit;

  @FXML
  private TreeView<Command> commandsTree;

  @FXML
  private AnchorPane mainAnchor;

  @FXML
  private Button connectButton;

  @FXML
  private ComboBox<String> devicesCombo;

  @FXML
  private ComboBox<String> baudRateCombo;

  @FXML
  private ComboBox<String> dataBitsCombo;

  @FXML
  private ComboBox<String> stopbitsCombo;

  @FXML
  private ComboBox<String> parityCombo;

  @FXML
  private TextField rxCounterField;

  @FXML
  private TextField txCounterField;

  @FXML
  private MenuButton infobutton;

  @FXML
  private CheckMenuItem twomenuItem;

  @FXML
  private HBox secondConnectionBar;

  @FXML
  private Button connectButton1;

  @FXML
  private ComboBox<String> devicesCombo1;

  @FXML
  private ComboBox<String> baudRateCombo1;

  @FXML
  private ComboBox<String> dataBitsCombo1;

  @FXML
  private ComboBox<String> stopbitsCombo1;

  @FXML
  private ComboBox<String> parityCombo1;

  @FXML
  private TextField rxCounterField1;

  @FXML
  private TextField txCounterField1;

  @FXML
  private Button infobutton1;

  private StyleClassedTextArea mainTextArea;

  SerialInterface serial = new SerialInterface();
  SerialInterface serial1 = new SerialInterface();

  Timer timer = null;
  MaskField myMaskField = new MaskField();
  ScriptEvaluator se = new ScriptEvaluator();
  List<String> historyList = new ArrayList<>();
  private boolean requestclear = false;
  private static final int BYTE_READ = 0;
  private static final int BYTE_WRITE = 1;
  private static final int BYTE_UNKNOWN = 2;
  private final ListProperty<DisplayData> dataList = new SimpleListProperty<>(FXCollections.observableArrayList(new ArrayList<>()));

  private int numberOfBatches = 0;
  private int aktBatch = 0;
  private boolean isLastCharLF = true;
  private int lastEntryDirection = BYTE_READ;
  private String lastEntryPort = "";
  private long lastTimeStamp = 0;
  private String debugString = "";
  private int lastLineMissing = 0;
  TreeItem<Command> rootItem = new TreeItem<>();
  private final ContextMenu addMenu = new ContextMenu();
  Thread myThread;

  public static byte[] hexStringToByteArray(String s) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
              + Character.digit(s.charAt(i + 1), 16));
    }
    return data;
  }

  private byte[] toBytes(String str) {
    if (inputFormatCombo.getSelectionModel().getSelectedItem().equals("ASCII")) {
      return str.getBytes(Charset.forName("US-ASCII"));
    } else if (inputFormatCombo.getSelectionModel().getSelectedItem().equals("HEX")) {
      return hexStringToByteArray(str);
    }

    return null;
  }

  @FXML
  void sendButtonAction(ActionEvent event) {  
    
    // textfieldentry
    String str = myMaskField.getPlainText();

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
      case "Custom...":
        se.setScript(Settings.getValue("CustomScript", ScriptEvaluator.defaultScript));
        str = se.evaluate(toBytes(myMaskField.getPlainText()));
        break;
      default:
        break;
    }

    if (port1Radio.isSelected() && (serial.isConnected())) {
      // send character
      serial.write(str);      
    }
    else if (port2Radio.isSelected() && (serial1.isConnected())) {
      // send character
      serial1.write(str);      
    }
    // clear textfield
    myMaskField.clear();//setText("");

  }

  @FXML
  void clearButtonAction(ActionEvent event) {
//    logger.info("" + mainTextArea.getStyleOfChar(2));
    Platform.runLater(() -> {
      mainTextArea.clear();
    });
    serial.setRxBytes(0);
    serial1.setRxBytes(0);
    dataList.get().clear();
    numberOfBatches = 0;
    aktBatch = 0;
    lastEntryDirection = BYTE_UNKNOWN;
    lastEntryPort = "";
    lastTimeStamp = 0;
    lastLineMissing = msSpinner.getValue();
//    mainTextArea.setStyleClass(0, 100, "red");
//      
  }

  @FXML
  void connectButtonAction(ActionEvent event) {
    if (devicesCombo.getSelectionModel().isEmpty()) {
      statusLabel.setText("Select a valid Port");
    } else {

      if (serial.isConnected()) {
        closePort();
        devicesCombo1.getItems().add(devicesCombo.getSelectionModel().getSelectedItem());
        if (devicesCombo1.getSelectionModel().isEmpty()){
          devicesCombo1.getSelectionModel().select(0);
        }
      } else {
        devicesCombo1.getItems().remove(devicesCombo.getSelectionModel().getSelectedItem());
        if (devicesCombo1.getSelectionModel().isEmpty()){
          devicesCombo1.getSelectionModel().select(0);
        }
        serial.setBaudRate(Integer.parseInt(baudRateCombo.getValue()));
        serial.setDataBits(Integer.parseInt(dataBitsCombo.getValue()));
        serial.setParity(SerialInterface.Parity.valueOf(parityCombo.getValue()));
        serial.setStopBit(Integer.parseInt(stopbitsCombo.getValue()));
        String rv = serial.connect(devicesCombo.getSelectionModel().getSelectedItem());
        statusLabel.setText(devicesCombo.getSelectionModel().getSelectedItem() + ":  " + rv);
        if (serial.isConnected()) {
          connectButton.setText("Disconnect");
        }
        if (!serial1.isConnected()){
          port1Radio.setSelected(true);
        }
      }
    }
    setConnectionLabel();
  }

  @FXML
  void connectButton1Action(ActionEvent event) {
    if (devicesCombo1.getSelectionModel().isEmpty()) {
      statusLabel.setText("Select a valid Port");
    } else {

      if (serial1.isConnected()) {
        closePort1();
        devicesCombo.getItems().add(devicesCombo1.getSelectionModel().getSelectedItem());
        if (devicesCombo.getSelectionModel().isEmpty()){
          devicesCombo.getSelectionModel().select(0);
        }
      } else {
        devicesCombo.getItems().remove(devicesCombo1.getSelectionModel().getSelectedItem());
        if (devicesCombo.getSelectionModel().isEmpty()){
          devicesCombo.getSelectionModel().select(0);
        }
        serial1.setBaudRate(Integer.parseInt(baudRateCombo1.getValue()));
        serial1.setDataBits(Integer.parseInt(dataBitsCombo1.getValue()));
        serial1.setParity(SerialInterface.Parity.valueOf(parityCombo1.getValue()));
        serial1.setStopBit(Integer.parseInt(stopbitsCombo1.getValue()));
        String rv = serial1.connect(devicesCombo1.getSelectionModel().getSelectedItem());
        statusLabel.setText(devicesCombo1.getSelectionModel().getSelectedItem() + ":  " + rv);
        if (serial1.isConnected()) {
          connectButton1.setText("Disconnect");
        }
        if (!serial.isConnected()){
          port2Radio.setSelected(true);
        }
      }
    }
    setConnectionLabel();
  }

  @FXML
  void infoAction(ActionEvent event) {
    DialogFactory.showInfoDialog();
  }

  @FXML
  void keyPressedInputTextfield(KeyEvent event) {

    // on Keycode up and down cycle through historyList
    if (event.getCode() == KeyCode.UP) {

      // find current index 
      int index = historyList.indexOf(myMaskField.getText());

      // if index is inside historylist choose correct entry
      if ((index + 1) < historyList.size()) {

        myMaskField.setText(historyList.get(index + 1));
        myMaskField.setPlainText(historyList.get(index + 1));
        // mark complete textfield
        myMaskField.selectAll();
      }

      // consume event so that the standard behaviour is not performed
      event.consume();
    } else if (event.getCode() == KeyCode.DOWN) {

      // find current index 
      int index = historyList.indexOf(myMaskField.getText());

      // if index is inside historylist choose correct entry
      if (index > 0) {

        myMaskField.setText(historyList.get(index - 1));
        myMaskField.setPlainText(historyList.get(index - 1));

        // mark complete textfield
        myMaskField.selectAll();
      }

      // consume event so that the standard behaviour is not performed
      event.consume();
    }

  }

  @Override
  public void initialize(URL url, ResourceBundle rb) {
    VBox connectionVBox = (VBox) secondConnectionBar.getParent();
    if (!Settings.getValue("TwoPorts", true)) {
      connectionVBox.getChildren().remove(secondConnectionBar);
      showPort2Radio.setSelected(false);
      port1Radio.setSelected(true);
      portBox1.setVisible(false);
      portBox.setVisible(false);
    }

    // Fill and handle Parity Combobox 
    parityCombo.getItems().removeAll(parityCombo.getItems());
    parityCombo.getItems().addAll(Arrays.stream(SerialInterface.Parity.class.getEnumConstants()).map((aEnum) -> String.valueOf(aEnum.name())).toArray(String[]::new));
    parityCombo.getSelectionModel().select(Settings.getValue("Parity", "NONE"));
    parityCombo.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
      Settings.setValue("Parity", parityCombo.getSelectionModel().getSelectedItem());
      serial.setParity(SerialInterface.Parity.valueOf(parityCombo.getValue()));
      setConnectionLabel();
    });

    parityCombo1.getItems().removeAll(parityCombo1.getItems());
    parityCombo1.getItems().addAll(Arrays.stream(SerialInterface.Parity.class.getEnumConstants()).map((aEnum) -> String.valueOf(aEnum.name())).toArray(String[]::new));
    parityCombo1.getSelectionModel().select(Settings.getValue("Parity", "NONE"));
    parityCombo1.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
      Settings.setValue("Parity1", parityCombo1.getSelectionModel().getSelectedItem());
      serial1.setParity(SerialInterface.Parity.valueOf(parityCombo1.getValue()));
      setConnectionLabel();
    });

    // Fill and handle Baudrate Combobox 
    baudRateCombo.getItems().removeAll(baudRateCombo.getItems());
    baudRateCombo.getItems().addAll(Arrays.stream(SerialInterface.BaudRate.class.getEnumConstants()).map((aEnum) -> String.valueOf(aEnum.getValue())).toArray(String[]::new));
    baudRateCombo.getSelectionModel().select(Settings.getValue("Baudrate", "115200"));
    baudRateCombo.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
      Settings.setValue("Baudrate", baudRateCombo.getSelectionModel().getSelectedItem());
      serial.setBaudRate(Integer.parseInt(baudRateCombo.getValue()));
      setConnectionLabel();
    });

    baudRateCombo1.getItems().removeAll(baudRateCombo1.getItems());
    baudRateCombo1.getItems().addAll(Arrays.stream(SerialInterface.BaudRate.class.getEnumConstants()).map((aEnum) -> String.valueOf(aEnum.getValue())).toArray(String[]::new));
    baudRateCombo1.getSelectionModel().select(Settings.getValue("Baudrate", "115200"));
    baudRateCombo1.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
      Settings.setValue("Baudrate", baudRateCombo1.getSelectionModel().getSelectedItem());
      serial1.setBaudRate(Integer.parseInt(baudRateCombo1.getValue()));
      setConnectionLabel();
    });

    // Fill and handle Databits Combobox 
    dataBitsCombo.getItems().removeAll(dataBitsCombo.getItems());
    dataBitsCombo.getItems().addAll(Arrays.stream(SerialInterface.Data.class.getEnumConstants()).map((aEnum) -> String.valueOf(aEnum.getValue())).toArray(String[]::new));
    dataBitsCombo.getSelectionModel().select(Settings.getValue("Databits", "8"));
    dataBitsCombo.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
      Settings.setValue("Databits", dataBitsCombo.getSelectionModel().getSelectedItem());
      serial.setDataBits(Integer.parseInt(dataBitsCombo.getValue()));
      setConnectionLabel();
    });
    dataBitsCombo1.getItems().removeAll(dataBitsCombo1.getItems());
    dataBitsCombo1.getItems().addAll(Arrays.stream(SerialInterface.Data.class.getEnumConstants()).map((aEnum) -> String.valueOf(aEnum.getValue())).toArray(String[]::new));
    dataBitsCombo1.getSelectionModel().select(Settings.getValue("Databits", "8"));
    dataBitsCombo1.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
      Settings.setValue("Databits", dataBitsCombo1.getSelectionModel().getSelectedItem());
      serial1.setDataBits(Integer.parseInt(dataBitsCombo1.getValue()));
      setConnectionLabel();
    });

    // Fill and handle Stopbits Combobox 
    stopbitsCombo.getItems().removeAll(stopbitsCombo.getItems());
    stopbitsCombo.getItems().addAll(Arrays.stream(SerialInterface.Stop.class.getEnumConstants()).map((aEnum) -> String.valueOf(aEnum.getValue())).toArray(String[]::new));
    stopbitsCombo.getSelectionModel().select(Settings.getValue("Stopbits", "1"));
    stopbitsCombo.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
      Settings.setValue("Stopbits", stopbitsCombo.getSelectionModel().getSelectedItem());
      serial.setStopBit(Integer.parseInt(stopbitsCombo.getValue()));
      setConnectionLabel();
    });
    stopbitsCombo1.getItems().removeAll(stopbitsCombo1.getItems());
    stopbitsCombo1.getItems().addAll(Arrays.stream(SerialInterface.Stop.class.getEnumConstants()).map((aEnum) -> String.valueOf(aEnum.getValue())).toArray(String[]::new));
    stopbitsCombo1.getSelectionModel().select(Settings.getValue("Stopbits", "1"));
    stopbitsCombo1.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
      Settings.setValue("Stopbits", stopbitsCombo1.getSelectionModel().getSelectedItem());
      serial1.setStopBit(Integer.parseInt(stopbitsCombo1.getValue()));
      setConnectionLabel();
    });

    Image image1 = new Image(FXMLDocumentController.class.getResource("/resources/pics/Mimes_White_close.png").toExternalForm(), 25, 25, true, true);
    ImageView imageView1 = new ImageView(image1);
    infobutton1.setGraphic(imageView1);
    infobutton1.setStyle("-fx-padding: 0;-fx-background-radius: 0;");
    infobutton1.setOnAction((ActionEvent event) -> {
      Platform.runLater(() -> {
        connectionVBox.getChildren().remove(secondConnectionBar);
        twomenuItem.setSelected(false);
        showPort2Radio.setSelected(false);
        port1Radio.setSelected(true);
        if (serial1.isConnected()) {
          closePort1();
        }
        portBox.setVisible(false);
        portBox1.setVisible(false);
        Settings.setValue("TwoPorts", false);
      });
    });
    infobutton1.setTooltip(new Tooltip("Close second Communication channel!"));

    // Fill and handle millisecond or bytenumber Spinner
    msSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, Settings.getValue("#ofms", 1), 1));
    // enable increment decrement scrolling
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

    // Fill and handle lineFeedCombo
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

    // handle rx tx show enable radio buttons
    showTXDataRadio.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
      requestclear = true;
    });
    showRXDataRadio.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
      requestclear = true;
    });
    showPort1Radio.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
      requestclear = true;
    });
    showPort2Radio.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
      requestclear = true;
    });

    // Fill and handle behaviour on enter/sendbutton
    sendOnEnterCombo.getItems().clear();
    sendOnEnterCombo.getItems().addAll("CR", "LF", "CR+LF", "NONE", "Custom...");
    sendOnEnterCombo.getSelectionModel().select(Settings.getValue("SendOnEnter", "LF"));
    // script loaded from properties file
    se.setScript(Settings.getValue("CustomScript", ScriptEvaluator.defaultScript));

    // handle click event if it is on Custom... Textarea
    sendOnEnterCombo.setOnMouseClicked((MouseEvent event) -> {
      if (event.getTarget() instanceof Text) {
        Text text = (Text) event.getTarget();
        if (text.getText().equals("Custom...")) {
          event.consume();
          se.setScript(DialogFactory.showJavascriptDialog(se.getScript()));
          Settings.setValue("CustomScript", se.getScript());
        }
      }
    });

    sendOnEnterCombo.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
      Settings.setValue("SendOnEnter", sendOnEnterCombo.getSelectionModel().getSelectedItem());
      if (newValue.equals("Custom...")) {
        logger.info("Select custom Value");

        se.setScript(DialogFactory.showJavascriptDialog(se.getScript()));
        Settings.setValue("CustomScript", se.getScript());
        // TODO Add dialog box to 
      }
    });

    // initialize Maskfield; this is done by removing a textfield from xml initialization 
    // as i wanted to avoid a special library jar file for Maskfield
    myMaskField = new MaskField();
    myMaskField.setStyle("-fx-font-size:11;-fx-font-family: FreeMono2; -fx-font-weight: bold;");
    HBox parent = (HBox) sendTextField.getParent();
    parent.getChildren().remove(sendTextField);
    HBox.setHgrow(myMaskField, Priority.ALWAYS);
    myMaskField.prefHeightProperty().bind(inputFormatCombo.heightProperty());
    myMaskField.setOnKeyPressed((KeyEvent event) -> {
      keyPressedInputTextfield(event);
    });
    myMaskField.setOnAction((ActionEvent event) -> {
      sendButtonAction(event);
    });
    parent.getChildren().add(1, myMaskField);

    // Fill and handle inputFormatfield
    inputFormatCombo.getItems().clear();
    inputFormatCombo.getItems().addAll("ASCII", "HEX");
    inputFormatCombo.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
      Settings.setValue("InputFormat", inputFormatCombo.getSelectionModel().getSelectedItem());
      myMaskField.clear();
      if (newValue.equals("ASCII")) {
        myMaskField.setMask("Xc");
      } else if (newValue.equals("HEX")) {
        myMaskField.setMask("HH c");
      }
      myMaskField.requestFocus();
    });
    inputFormatCombo.getSelectionModel().select(Settings.getValue("InputFormat", "ASCII"));

    // handle display format (Hex or Ascii supported)
    for (Toggle toggle : (formatToggleGroup.getToggles())) {
      if (((RadioButton) toggle).getText().equals(Settings.getValue("DisplayFormat", "ASCII"))) {
        ((RadioButton) toggle).setSelected(true);
      }
      ((RadioButton) toggle).selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
        if (newValue) {
          Settings.setValue("DisplayFormat", ((RadioButton) toggle).getText());
        }
      });
    }
    formatToggleGroup.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {
      requestclear = true;
    });

    // mainArea for displaying in and output data
    mainTextArea = new StyleClassedTextArea();
    mainTextArea.setUndoManager(null);
    VirtualizedScrollPane vsp = new VirtualizedScrollPane<>(mainTextArea);
    mainTextArea.setAutoScrollOnDragDesired(true);
    mainTextArea.setCache(true);
    mainAnchor.getChildren().add(vsp);
    AnchorPane.setBottomAnchor(vsp, 0.);
    AnchorPane.setTopAnchor(vsp, 0.);
    AnchorPane.setLeftAnchor(vsp, 0.);
    AnchorPane.setRightAnchor(vsp, 0.);
    // enable time measurements between bytes on hover(display timestamp) or selection (display timedifference)
    mainTextArea.setMouseOverTextDelay(java.time.Duration.ofMillis(500));
    Popup popup = new Popup();
    Label popupMsg = new Label();
    popupMsg.setStyle(
            "-fx-background-color: black;"
            + "-fx-text-fill: white;"
            + "-fx-font-size:11;-fx-font-family: Source Code Pro; "
            + "-fx-padding: 5;");
    popup.getContent().add(popupMsg);
    mainTextArea.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN, e -> {
      String text;
      SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss:SSS");
      if (mainTextArea.getSelection().getStart() != mainTextArea.getSelection().getEnd()) {
        long difference;
        Date resultdate = new Date(getSelectedTime(mainTextArea.getSelection().getStart()));
        text = "Start: " + sdf.format(resultdate);
        difference = resultdate.getTime();
        resultdate = new Date(getSelectedTime(mainTextArea.getSelection().getEnd()));
        difference = resultdate.getTime() - difference;
        text += " \nEnd:   " + sdf.format(resultdate);
        text += "\nDifference:  " + formatDifference(difference);
      } else {
        long difference;
        Date resultdate = new Date(getSelectedTime(mainTextArea.getSelection().getStart()));
        text = "Start: " + sdf.format(resultdate);
        difference = resultdate.getTime();
        resultdate = new Date(getSelectedTime(e.getCharacterIndex()));
        difference = resultdate.getTime() - difference;
        text += " \nEnd:   " + sdf.format(resultdate);
        text += "\nDifference:  " + formatDifference(difference);
      }

      Point2D pos = e.getScreenPosition();
      popupMsg.setText(text);
      popup.show(mainTextArea, pos.getX(), pos.getY() + 10);

    });
    mainTextArea.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_END, e -> {
      popup.hide();
    });
    mainTextArea.selectedTextProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
      Platform.runLater(() -> {
        autoscrollCheck.setSelected(false);
      });
    });
    // set monospaced font for mainArea
    Font font = Font.loadFont(FXMLDocumentController.class.getResourceAsStream("/ModifiedFreeMono.ttf"), 10);
    logger.info(font.toString());
    font = Font.loadFont(FXMLDocumentController.class.getResourceAsStream("/ModifiedFreeMonoBold.ttf"), 10);
    logger.info(font.toString());
    mainTextArea.setStyle("-fx-font-size:11;-fx-font-family: FreeMono2; -fx-font-weight: bold;-fx-highlight-fill: paleturquoise;");
    mainTextArea.setWrapText(true);
    MyLineNumberFactory mlnf = new MyLineNumberFactory(mainTextArea, digits -> "%1$" + digits + "s");
    mainTextArea.setParagraphGraphicFactory(mlnf);
    mainTextArea.setEditable(false);
    mainTextArea.showCaretProperty().setValue(Caret.CaretVisibility.ON);

    final Tooltip tooltip = new Tooltip();
    devicesCombo.setTooltip(tooltip);
    // change tooltip time
    try {
      Tooltip obj = new Tooltip();
      Class<?> clazz = obj.getClass().getDeclaredClasses()[0];
      Constructor<?> constructor = clazz.getDeclaredConstructor(
              Duration.class,
              Duration.class,
              Duration.class,
              boolean.class);
      constructor.setAccessible(true);
      Object tooltipBehavior = constructor.newInstance(
              new Duration(250L), //open
              new Duration(50000L), //visible
              new Duration(200L), //close
              false);
      Field fieldBehavior = obj.getClass().getDeclaredField("BEHAVIOR");
      fieldBehavior.setAccessible(true);
      fieldBehavior.set(obj, tooltipBehavior);
    } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | NoSuchMethodException | SecurityException | InstantiationException | InvocationTargetException e) {
      logger.error(e.getMessage());
    }

    // add listener to devicelist and close Device if disconnected
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
      tooltip.setText(myRunnable.getDeviceInfo(devicesCombo.getSelectionModel().getSelectedItem()));
    });

    devicesCombo.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
      tooltip.setText(myRunnable.getDeviceInfo(newValue));
    });
    // display number of received and transmitted bytes
    Bindings.bindBidirectional(rxCounterField.textProperty(), serial.rxBytesProperty(), sc);
    Bindings.bindBidirectional(txCounterField.textProperty(), serial.txBytesProperty(), sc);
    Bindings.bindBidirectional(rxCounterField1.textProperty(), serial1.rxBytesProperty(), sc);
    Bindings.bindBidirectional(txCounterField1.textProperty(), serial1.txBytesProperty(), sc);

    // start deviceDiscovery and listeners 
    deviceDiscovery();

    // add listeners for read and written data and fill datalist
    serial.lastReadProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
      if (!"".equals(newValue)) {
        dataList.add(numberOfBatches, new DisplayData("PORT_1", numberOfBatches, BYTE_READ, System.currentTimeMillis(), newValue));
        numberOfBatches++;
      }
    });
    serial.lastWriteProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
      if (!"".equals(newValue)) {
        dataList.add(numberOfBatches, new DisplayData("PORT_1", numberOfBatches, BYTE_WRITE, System.currentTimeMillis(), newValue));
        numberOfBatches++;
      }
    });
    serial1.lastReadProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
      if (!"".equals(newValue)) {
        dataList.add(numberOfBatches, new DisplayData("PORT_2", numberOfBatches, BYTE_READ, System.currentTimeMillis(), newValue));
        numberOfBatches++;
      }
    });
    serial1.lastWriteProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
      if (!"".equals(newValue)) {
        dataList.add(numberOfBatches, new DisplayData("PORT_2", numberOfBatches, BYTE_WRITE, System.currentTimeMillis(), newValue));
        numberOfBatches++;
      }
    });

    // update timer for UI Update (every 20 ms) to avoid large processor loads
    timer = new Timer("UI_UpdateTimer", true);
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        updateTextField();
      }
    }, 0, 20);

    // add info button for Info of app
    Image image = new Image(FXMLDocumentController.class.getResource("/resources/pics/Mimes_White_menu.png").toExternalForm(), 25, 25, true, true);
    ImageView imageView = new ImageView(image);
    infobutton.setGraphic(imageView);
//    infobutton.setStyle("-fx-padding: 0 0 0 0;");

    twomenuItem.setSelected(Settings.getValue("TwoPorts", true));
    portBox.setVisible(Settings.getValue("TwoPorts", true));
    twomenuItem.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
      Settings.setValue("TwoPorts", newValue);
      portBox.setVisible(newValue);
      showPort2Radio.setSelected(newValue);
      portBox1.setVisible(newValue);
      if (newValue) {
        connectionVBox.getChildren().add(secondConnectionBar);
      } else {
        connectionVBox.getChildren().remove(secondConnectionBar);
        port1Radio.setSelected(true);
      }
    });
    // fill commands treeview 
    populateTreeView();

    // store and restore window sizes
    mainAnchorPane.setPrefSize(Settings.getValue("MainPrefWidth", 800.), Settings.getValue("MainPrefHeight", 500.));
    mainAnchorPane.widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
      Settings.setValue("MainPrefWidth", "" + newValue);
    });
    mainAnchorPane.heightProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
      Settings.setValue("MainPrefHeight", "" + newValue);
    });
    ChangeListener<Number> cl = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
      Settings.setValue("DividerPosition", "" + newValue);
    };
    for (Divider d : mainSplit.getDividers()) {
      d.setPosition(Settings.getValue("DividerPosition", 0.15));
      d.positionProperty().addListener(cl);
    }

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

  private void closePort1() {

    serial1.close();
    statusLabel.setText("Port closed: " + devicesCombo1.getSelectionModel().getSelectedItem());
    connectButton1.setText("Connect");
  }

  public static String[] Split(String text, int chunkSize, int maxLength) {
    char[] data = text.toCharArray();
    int len = Math.min(data.length, maxLength);
    String[] result = new String[(len + chunkSize - 1) / chunkSize];
    int linha = 0;
    for (int i = 0; i < len; i += chunkSize) {
      result[linha] = new String(data, i, Math.min(chunkSize, len - i));
      linha++;
    }
    return result;
  }

  private long getSelectedTime(int selectedCharIndex) {
    String s = mainTextArea.getText();
    // get number of LF    
    int numberOfLF = s.substring(0, selectedCharIndex).split("\n").length;

    int currentPos = 0;
    for (DisplayData dd : dataList.get()) {
      currentPos += dd.bytes.length();
      if (currentPos > (selectedCharIndex - numberOfLF)) {
        return dd.timestamp;
      }
    }
    return 0;
  }

  // function called every 20 ms
  private void updateTextField() {
    long startTime = System.nanoTime();
    // local variables
    // string composed for area update
    String newEntry = "";
    // local copy of numberOfBatches
    int number = numberOfBatches;
    // variable to store direction for correct formatting
    int aktEntryDirection = BYTE_READ;
    String aktEntryPort = "";

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
      lastEntryPort = "";
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
      // if port of entry (PORT_1) is not shown continue
      if (("PORT_1".equals(dd.port)) && !showPort1Radio.isSelected()) {
        continue;
      }
      // if port of entry (PORT_2) is not shown continue
      if (("PORT_2".equals(dd.port)) && !showPort2Radio.isSelected()) {
        continue;
      }

      // if direction changes add newline char to textfield (except first line or newline is already present)
      if ((dd.direction != lastEntryDirection)
              && (!dd.port.equals(lastEntryPort))
              && (lastEntryDirection != BYTE_UNKNOWN)
              && (!"".equals(lastEntryPort))
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

      // if port changes break loop and display data with specific formating 
      if (!dd.port.equals(lastEntryPort)) {
        // store aktual batch
        aktBatch = i;
        // store direction
        lastEntryPort = dd.port;
        // break loop
        break;
      }

      // store direction
      aktEntryDirection = dd.direction;
      aktEntryPort = dd.port;

      // decide hex or ascii
      if (((RadioButton) formatToggleGroup.getSelectedToggle()).getText().equals("ASCII")) {
        // replace LF with symbol
        newString = dd.bytes.replace("\n", "\u2424");
        // replace carrage return
        newString = newString.replace("\r", "\u240D");
        // replace NULL string with symbol
        newString = newString.replace("\0", "\u2400");
        // replace TAB with symbol
        newString = newString.replace("\t", "\u2409");

        // if newline at LF is selected add newline
        if ("CR+LF".equals(linefeedCombo.getSelectionModel().getSelectedItem())) {
          newString = newString.replace("\u240D\u2424", "\u240D\u2424\n");
        } else if ("LF".equals(linefeedCombo.getSelectionModel().getSelectedItem())) {
          newString = newString.replace("\u2424", "\u2424\n");
        }

        // fixed length
        if ("char".equals(linefeedCombo.getValue())) {
          // store buffer
          String bufferStr = newString;
          // clear newString
          newString = "";
          while (bufferStr.length() > 0) {
            String str;
            if (bufferStr.length() >= lastLineMissing) {
              str = bufferStr.substring(0, lastLineMissing);
            } else {
              str = bufferStr;
            }
            newString += str;
            lastLineMissing = lastLineMissing - str.length();
            bufferStr = bufferStr.substring(str.length());
            if (lastLineMissing == 0) {
              newString += "\n";
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

    // copy port to not loose entry in FX update thread
    String bufferPort = aktEntryPort;

    // if new string is available append to FX update thread
    if (bufferStr.length() > 0) {

      Platform.runLater(() -> {
        // debug output
        debugLabel.setText(debugString);

        // clear area if requested
        if (requestclearCopy) {
          mainTextArea.clear();
        }

        int start = mainTextArea.getSelection().getStart();
        int caretPosition = mainTextArea.getCaretPosition();
        // append new string to area
        mainTextArea.appendText(bufferStr);

        // do appropriate style 
        if (bufferPort.equals("PORT_1")) {
          if (bufferDirection == BYTE_READ) {
            mainTextArea.setStyleClass(mainTextArea.getText().length() - bufferStr.length(), mainTextArea.getText().length(), "rx_port1");
          } else {
            mainTextArea.setStyleClass(mainTextArea.getText().length() - bufferStr.length(), mainTextArea.getText().length(), "tx_port1");
          }
        } else if (bufferPort.equals("PORT_2")) {
          if (bufferDirection == BYTE_READ) {
            mainTextArea.setStyleClass(mainTextArea.getText().length() - bufferStr.length(), mainTextArea.getText().length(), "rx_port2");
          } else {
            mainTextArea.setStyleClass(mainTextArea.getText().length() - bufferStr.length(), mainTextArea.getText().length(), "tx_port2");

          }

        }

        // if autoscroll then last paragraph is alwaiys selected
        if (autoscrollCheck.isSelected()) {
          mainTextArea.showParagraphAtBottom(mainTextArea.getCurrentParagraph());
        } else {
          mainTextArea.selectRange(start, caretPosition);
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

  private String formatDifference(long diff) {
    String rv;

    if (diff > 1000) {
      rv = "" + (diff / 1000) + "." + String.format("%03d", diff % 1000) + " s";
    } else {
      rv = "" + diff + " ms";
    }
    return rv;
  }

  private void populateTreeView() {
    createContextMenu();
    commandsTree.setContextMenu(addMenu);
    commandsTree.setRoot(rootItem);
    commandsTree.setShowRoot(false);
    commandsTree.setCellFactory((TreeView<Command> p) -> new TextFieldTreeCellImpl());
    commandsTree.editableProperty().bind(serial.connectedProperty().not().and(serial1.connectedProperty().not()));

    EventHandler<MouseEvent> mouseEventHandle = (MouseEvent event) -> {
      handleMouseClicked(event);
    };

    commandsTree.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEventHandle);
    readTreeviewSettings();
  }

  private void handleMouseClicked(MouseEvent event) {
    if ((serial.isConnected()|| serial1.isConnected()) && (event.getClickCount() == 2) && (event.getButton().equals(MouseButton.PRIMARY))) {
      Node node = event.getPickResult().getIntersectedNode();
      // Accept clicks only on node cells, and not on empty spaces of the TreeView
      if (node instanceof Text || (node instanceof TreeCell && ((TreeCell) node).getText() != null)) {
        Command c = ((TreeItem<Command>) commandsTree.getSelectionModel().getSelectedItem()).getValue();

        // send command
        // textfieldentry
        String str = c.getValue();

        //append character selected in onEnterCombo
        switch (c.getLinefeed()) {
          case "LF":
            str += "\n";
            break;
          case "CR":
            str += "\r";
            break;
          case "CR+LF":
            str += "\r\n";
            break;
          case "Custom...":
            se.setScript(c.getScript());
            str = se.evaluate(toBytes(c.getValue()));
            break;
          default:
            break;
        }

        if (port1Radio.isSelected() && serial.isConnected()){
          // send character
          serial.write(str);
        }
        else if (port2Radio.isSelected() && serial1.isConnected()){
          // send character
          serial1.write(str);
        }
      }
    }
  }

  private void readTreeviewSettings() {

    int i = 0, j = 0;
    do {
      if (Settings.hasValue("TreeviewItemBase" + i)) {
        Command c = new Command();
        c.setName(Settings.getValue("TreeviewItemBase" + i, ""));

        TreeItem<Command> newItem = new TreeItem<>(c);

        newItem.setExpanded(Settings.getValue("TreeviewItemBase" + i + ".colapsed", true));
        rootItem.getChildren().add(newItem);

        do {
          if (Settings.hasValue("TreeviewItemChild" + i + "_" + j + ".Name")) {
            Command cChild = new Command();
            cChild.setName(Settings.getValue("TreeviewItemChild" + i + "_" + j + ".Name", ""));
            cChild.setLinefeed(Settings.getValue("TreeviewItemChild" + i + "_" + j + ".Linefeed", ""));
            cChild.setScript(Settings.getValue("TreeviewItemChild" + i + "_" + j + ".Script", ""));
            cChild.setValue(Settings.getValue("TreeviewItemChild" + i + "_" + j + ".Value", ""));

            TreeItem<Command> newChildItem = new TreeItem<>(cChild);
            newItem.getChildren().add(newChildItem);

          } else {
            break;
          }

          j++;
        } while (true);

      } else {
        break;
      }
      i++;
      j = 0;
    } while (true);

  }

  private void createContextMenu() {
    addMenu.getItems().clear();

    MenuItem addMenuItem = new MenuItem("Add new List");
    addMenuItem.setOnAction((ActionEvent t) -> {
      Command c = new Command();
      c.setName("New List");
      TreeItem<Command> newItem = new TreeItem<>(c);
      newItem.setExpanded(true);
      rootItem.getChildren().add(newItem);
      treeViewModified();
    });

    MenuItem exportMenuItem = new MenuItem("Export Complete List");
    exportMenuItem.setOnAction((ActionEvent t) -> {
      logger.info("Export complete List");
      exportList();
    });

    MenuItem importMenuItem = new MenuItem("Import List");
    importMenuItem.setOnAction((ActionEvent t) -> {
      logger.info("Import List");
      importList();
    });

    addMenu.getItems().addAll(addMenuItem, exportMenuItem, importMenuItem);
  }

  private void exportList() {
    exportList(null);
  }

  private void exportList(String section) {
    String filename = DialogFactory.getFilenname(mainAnchor.getScene().getWindow());
    if ((filename == null) || ("".equals(filename))) {
      return;
    }
    Properties p = new Properties();

    int i = 0, j = 0;
    for (TreeItem<Command> child : rootItem.getChildren()) {
      if (section != null) {
        if (!child.getValue().getName().equals(section)) {
          continue;
        }
      }
      p.setProperty("TreeviewItemBase" + i, child.getValue().getName());
      p.setProperty("TreeviewItemBase" + i + ".colapsed", "" + child.isExpanded());
      for (TreeItem<Command> grandchild : child.getChildren()) {
        p.setProperty("TreeviewItemChild" + i + "_" + j + ".Name", grandchild.getValue().getName());
        p.setProperty("TreeviewItemChild" + i + "_" + j + ".Value", grandchild.getValue().getValue());
        p.setProperty("TreeviewItemChild" + i + "_" + j + ".Script", grandchild.getValue().getScript());
        p.setProperty("TreeviewItemChild" + i + "_" + j + ".Linefeed", grandchild.getValue().getLinefeed());
        j++;
      }
      j = 0;
      i++;
    }
    try {
      p.store(new FileOutputStream(filename), "");
    } catch (FileNotFoundException ex) {
      logger.error(ex.getMessage());
    } catch (IOException ex) {
      logger.error(ex.getMessage());
    }

  }

  private void importList() {
    String filename = DialogFactory.getFilenname(mainAnchor.getScene().getWindow());
    if ((filename == null) || ("".equals(filename))) {
      return;
    }

    Properties p = new Properties();
    try {
      p.load(new BufferedReader(new FileReader(filename)));
    } catch (FileNotFoundException ex) {
      logger.error(ex.getMessage());
    } catch (IOException ex) {
      logger.error(ex.getMessage());
    }

    int i = 0, j = 0;
    do {
      if (Settings.hasValue("TreeviewItemBase" + i)) {
        Command c = new Command();
        c.setName(p.getProperty("TreeviewItemBase" + i));

        TreeItem<Command> newItem = new TreeItem<>(c);
        newItem.setExpanded(true);
        rootItem.getChildren().add(newItem);

        do {
          if (p.getProperty("TreeviewItemChild" + i + "_" + j + ".Name") != null) {
            Command cChild = new Command();
            cChild.setName(p.getProperty("TreeviewItemChild" + i + "_" + j + ".Name", ""));
            cChild.setLinefeed(p.getProperty("TreeviewItemChild" + i + "_" + j + ".Linefeed", ""));
            cChild.setScript(p.getProperty("TreeviewItemChild" + i + "_" + j + ".Script", ""));
            cChild.setValue(p.getProperty("TreeviewItemChild" + i + "_" + j + ".Value", ""));

            TreeItem<Command> newChildItem = new TreeItem<>(cChild);
            newItem.getChildren().add(newChildItem);

          } else {
            break;
          }

          j++;
        } while (true);

      } else {
        break;
      }
      i++;
      j = 0;
    } while (true);

  }

  private void treeViewModified() {
    Settings.removeAllValues("TreeviewItem");
    int i = 0, j = 0;
    for (TreeItem<Command> child : rootItem.getChildren()) {
      Settings.setValue("TreeviewItemBase" + i, child.getValue().getName());
      Settings.setValue("TreeviewItemBase" + i + ".colapsed", "" + child.isExpanded());
      for (TreeItem<Command> grandchild : child.getChildren()) {
        Settings.setValue("TreeviewItemChild" + i + "_" + j + ".Name", grandchild.getValue().getName());
        Settings.setValue("TreeviewItemChild" + i + "_" + j + ".Value", grandchild.getValue().getValue());
        Settings.setValue("TreeviewItemChild" + i + "_" + j + ".Script", grandchild.getValue().getScript());
        Settings.setValue("TreeviewItemChild" + i + "_" + j + ".Linefeed", grandchild.getValue().getLinefeed());
        j++;
      }
      j = 0;
      i++;
    }
  }

  MonitorRunnable myRunnable = null;

  public void deviceDiscovery() {
    // Indicate Start Discovery
    logger.info("Start Discovery");

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
          devicesCombo1.getItems().addAll(c.getAddedSubList());
        } else {
          devicesCombo.getItems().removeAll(c.getRemoved());
          devicesCombo1.getItems().removeAll(c.getRemoved());
        }
        if (devicesCombo.getSelectionModel().isEmpty()) {
          devicesCombo.getSelectionModel().select(0);
        }
        if (devicesCombo1.getSelectionModel().isEmpty()) {
          devicesCombo1.getSelectionModel().select(0);
        }
      });
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

  private final class TextFieldTreeCellImpl extends TreeCell<Command> {

    private TextField textField;

    private final ContextMenu addMenu = new ContextMenu();

    private void createContextMenu(String str, boolean doParent) {

      addMenu.getItems().clear();

      MenuItem addMenuItem = new MenuItem("Add " + str);
      addMenuItem.setOnAction((ActionEvent t) -> {
        Command c = new Command();
        c.setName("New " + str);
        TreeItem<Command> newItem = new TreeItem<>(c);
        getTreeItem().getChildren().add(newItem);
        treeViewModified();
      });

      MenuItem removeMenuItem = new MenuItem("Remove");
      removeMenuItem.setOnAction((ActionEvent t) -> {
        logger.info("Remove " + getString());
        getTreeItem().getParent().getChildren().remove(getTreeItem());
        treeViewModified();
      });

      MenuItem exportMenuItem = new MenuItem("Export List");
      exportMenuItem.setOnAction((ActionEvent t) -> {
        logger.info("Export List");
        exportList(getItem().getName());
      });

      addMenu.getItems().addAll(addMenuItem, removeMenuItem, exportMenuItem);

    }

    private void createRemoveEditContextMenu() {

      addMenu.getItems().clear();

      MenuItem addMenuItem = new MenuItem("Edit");
      addMenuItem.setOnAction((ActionEvent t) -> {
        logger.info("Edit " + getString());
        DialogFactory.commandEditDialog(getItem());
        setText(getItem().getName());
        treeViewModified();
      });
      MenuItem removeMenuItem = new MenuItem("Remove");
      removeMenuItem.setOnAction((ActionEvent t) -> {
        if (DialogFactory.requestConfimation()) {
          logger.info("Remove " + getString());
          getTreeItem().getParent().getChildren().remove(getTreeItem());
          treeViewModified();
        }
      });

      addMenu.getItems().addAll(addMenuItem, removeMenuItem);

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
      setText((String) getItem().getName());
      setGraphic(getTreeItem().getGraphic());
      textField = null;
    }

    @Override
    public void updateItem(Command item, boolean empty) {
      super.updateItem(item, empty);
      treeViewModified();
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
          textField = null;
          if (getTreeItem().getParent().equals(rootItem)) {
            createContextMenu("Command", true);
          } else {
            createRemoveEditContextMenu();
//            addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, Event::consume);

          }
          setContextMenu(addMenu);
        }
      }
    }

    private void createTextField() {
      textField = new TextField(getString());
      textField.selectAll();
      Platform.runLater(() -> {
        textField.requestFocus();
      });
      textField.setOnKeyReleased((KeyEvent t) -> {
        if (t.getCode() == KeyCode.ENTER) {
          getItem().setName(textField.getText());
          commitEdit(getItem());
        } else if (t.getCode() == KeyCode.ESCAPE) {
          cancelEdit();
        }
      });
      textField.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
        // if focus lost        
        if (!newValue) {
          cancelEdit();
        }
      });
    }

    private String getString() {
      return getItem() == null ? "" : getItem().getName();
    }
  }

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

}
