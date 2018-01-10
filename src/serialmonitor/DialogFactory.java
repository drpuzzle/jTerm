/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serialmonitor;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;

import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.log4j.Logger;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;


public class DialogFactory {

  private static final Logger logger = Logger.getLogger(DialogFactory.class);

  private static final String[] KEYWORDS = new String[]{
    "function", "print"
  };

  private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
  private static final String PAREN_PATTERN = "\\(|\\)";
  private static final String BRACE_PATTERN = "\\{|\\}";
  private static final String BRACKET_PATTERN = "\\[|\\]";
  private static final String SEMICOLON_PATTERN = "\\;";
  private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
  private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

  private static final Pattern PATTERN = Pattern.compile(
          "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
          + "|(?<PAREN>" + PAREN_PATTERN + ")"
          + "|(?<BRACE>" + BRACE_PATTERN + ")"
          + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
          + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
          + "|(?<STRING>" + STRING_PATTERN + ")"
          + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
  );

  /**
   * help from http://code.makery.ch/blog/javafx-dialogs-official/
   *
   * icons under creative Commons
   * http://creativecommons.org/licenses/by-nc-sa/4.0/ from
   * http://www.iconarchive.com/show/leaf-mimes-icons-by-untergunter.html
   *
   * @param script
   * @return
   */
  private static String localScript;
  private static CodeArea codeArea;

  public static String showJavascriptDialog(String script) {
    // store variable locally
    localScript = script;

    // Create the custom dialog.
    Dialog<String> dialog = new Dialog<>();
    dialog.setTitle("Javascript Editor Dialog");
    dialog.setHeaderText("Edit your script file");

    dialog.getDialogPane().getStylesheets().add(DialogFactory.class.getResource("/jsEditor.css").toExternalForm());

// Set the icon (must be included in the project).
    dialog.setGraphic(new ImageView(new Image(DialogFactory.class.getResource("/resources/pics/Mimes_White_text-x-javascript.png").toString(), 64, 64, true, true)));
// Set the button types.
    ButtonType doneButtonType = new ButtonType("Done", ButtonData.OK_DONE);

    Button defaultButtonType = new Button("Default Script");

    dialog.getDialogPane().getButtonTypes().addAll(doneButtonType, ButtonType.CANCEL);

    codeArea = new CodeArea();
    codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

    codeArea.richChanges()
            .filter(ch -> !ch.getInserted().equals(ch.getRemoved())) // XXX
            .subscribe(change -> {
              codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText()));
            });
    codeArea.replaceText(0, 0, localScript);
    StackPane p = new StackPane(new VirtualizedScrollPane<>(codeArea));
    p.setPrefSize(400, 320);

    codeArea.setStyle("-fx-font-size: 11;");
    dialog.getDialogPane().setContent(p);
// Enable/Disable login button depending on whether a username was entered.
    Node loginButton = dialog.getDialogPane().lookupButton(doneButtonType);
    loginButton.setDisable(false);
    ButtonBar bb = (ButtonBar) loginButton.getParent().getParent();
    bb.getButtons().add(defaultButtonType);

    defaultButtonType.setOnAction((ActionEvent event) -> {
      event.consume();
      logger.info("Use default Script");
      codeArea.clear();
      codeArea.replaceText(0, 0, ScriptEvaluator.defaultScript);
    });

// Request focus on the username field by default.
    Platform.runLater(() -> p.requestFocus());

// Convert the result to a username-password-pair when the login button is clicked.
    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == doneButtonType) {
        return codeArea.getText();
      }
      return null;
    });

    Optional<String> result = dialog.showAndWait();

    result.ifPresent(resultString -> {
      localScript = resultString;

    });
    return localScript;
  }

  private static StyleSpans<Collection<String>> computeHighlighting(String text) {
    Matcher matcher = PATTERN.matcher(text);
    int lastKwEnd = 0;
    StyleSpansBuilder<Collection<String>> spansBuilder
            = new StyleSpansBuilder<>();
    while (matcher.find()) {
      String styleClass
              = matcher.group("KEYWORD") != null ? "keyword"
              : matcher.group("PAREN") != null ? "paren"
              : matcher.group("BRACE") != null ? "brace"
              : matcher.group("BRACKET") != null ? "bracket"
              : matcher.group("SEMICOLON") != null ? "semicolon"
              : matcher.group("STRING") != null ? "string"
              : matcher.group("COMMENT") != null ? "comment"
              : null;
      /* never happens */ assert styleClass != null;
//      logger.info("" + styleClass + " from " + matcher.start() + " bis " + matcher.end());
      spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
//      codeArea.setStyleClass(matcher.start(), matcher.end(), styleClass);
      spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
      lastKwEnd = matcher.end();
    }
    spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
    return spansBuilder.create();
  }

  private static final String infoLabel = ""
          + "<b>jTerm </b><br>"          
          + "see on github <a href=\"https://github.com/drpuzzle/jTerm\">jTerm</a><br><br>"
          + "Serial Terminal written in Java using JavaFX  <br>"
          + "Used external libraries"
          + "<ul>"
          + "<li>modified <a href=\"https://github.com/scream3r/java-simple-serial-connector\">JSSC v2.8.0</a> </li>"
          + "<li><a href=\"https://github.com/Zubnix/udev-java-bindings\">udev library</a> </li>"
          + "<li><a href=\"https://github.com/FXMisc/RichTextFX\">RichtextFX</a> </li>"
          + "<li><a href = \"http://www.iconarchive.com/show/leaf-mimes-icons-by-untergunter.html\">Icons</a> under <a href=\"http://creativecommons.org/licenses/by-nc-sa/4.0/\">Creative Commons</a></li>"
          + "<li>Maskfield based on <a href=\"https://github.com/vas7n/VAMaskField\">VAMaskField</a></li>"          
          + "</ul>";

  public static void showInfoDialog() {
    // Create the custom dialog.
    Dialog<String> dialog = new Dialog<>();
    dialog.setTitle("jTerm Info Dialog");
    dialog.setHeaderText("Information about jTerm\n" + "Version: " + Settings.version);
    // Set the icon (must be included in the project).
    dialog.setGraphic(new ImageView(new Image(DialogFactory.class.getResource("/resources/pics/Mimes_White_info.png").toString(), 64, 64, true, true)));

    ButtonType doneButtonType = new ButtonType("Done", ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(doneButtonType);

    WebView webView = new WebView();
    webView.setPrefSize(400, 320);

    webView.getEngine().loadContent(infoLabel);

    webView.getEngine().getLoadWorker().stateProperty().addListener((ObservableValue<? extends State> observable, State oldValue, State newValue) -> {
      NodeList nodeList = webView.getEngine().getDocument().getElementsByTagName("a");
      for (int i = 0; i < nodeList.getLength(); i++) {
        org.w3c.dom.Node node = nodeList.item(i);
        EventTarget eventTarget = (EventTarget) node;
        eventTarget.addEventListener("click", (org.w3c.dom.events.Event evt) -> {
          EventTarget target = evt.getCurrentTarget();
          HTMLAnchorElement anchorElement = (HTMLAnchorElement) target;
          String href = anchorElement.getHref();
          //handle opening URL outside JavaFX WebView
          logger.info("Open " + href + " in Browser");
          if (Desktop.isDesktopSupported()) {
            Thread t = new Thread(() -> {
              try {
                Desktop.getDesktop().browse(new URI(href));
              } catch (IOException | URISyntaxException e1) {
                logger.warn(e1.getMessage());
              }
            });
            t.setName("Browser Opening Thread");
            t.start();

          }
          evt.preventDefault();
        }, false);
      }
    });

    dialog.getDialogPane().setContent(webView);
    dialog.showAndWait();
  }

  public static boolean requestConfimation() {
    Alert alert = new Alert(AlertType.CONFIRMATION);
    alert.setTitle("Confirmation Dialog");
    alert.setHeaderText("Confirmation");
    alert.setContentText("Are you sure?");

    Optional<ButtonType> result = alert.showAndWait();
    return result.get() == ButtonType.OK;

  }

  public static void commandEditDialog(Command c) {
    // Create the custom dialog.
    Dialog<Boolean> dialog = new Dialog<>();
    dialog.setTitle("Command Editor Dialog");
    dialog.setHeaderText("Edit Command: \"" + c.getName() + "\"");

    dialog.getDialogPane().getStylesheets().add(DialogFactory.class.getResource("/jsEditor.css").toExternalForm());

// Set the icon (must be included in the project).
    dialog.setGraphic(new ImageView(new Image(DialogFactory.class.getResource("/resources/pics/Mimes_White_application-x-executable.png").toString(), 64, 64, true, true)));
// Set the button types.
    ButtonType doneButtonType = new ButtonType("Done", ButtonData.OK_DONE);

    dialog.getDialogPane().getButtonTypes().addAll(doneButtonType, ButtonType.CANCEL);

    VBox mainVBox = new VBox();

    int labelwidth = 100;
    int labelheight = 30;

    HBox hbox1 = new HBox();
    Label nameLabel = new Label("Name:");
    nameLabel.setPrefSize(labelwidth, labelheight);

    TextField nameField = new TextField(c.getName());
    HBox.setHgrow(nameField, Priority.ALWAYS);
    nameField.setStyle("-fx-font-size:11;");
    hbox1.getChildren().addAll(nameLabel, nameField);

    Node doneButton = dialog.getDialogPane().lookupButton(doneButtonType);

// Do some validation (using the Java 8 lambda syntax).
    nameField.textProperty().addListener((observable, oldValue, newValue) -> {
      doneButton.setDisable(newValue.trim().isEmpty());
    });

    HBox hbox2 = new HBox();
    Label commLabel = new Label("Command:");
    commLabel.setPrefSize(labelwidth, labelheight);
    MaskField myMaskField = new MaskField();
    myMaskField.setStyle("-fx-font-size:11;-fx-font-family: FreeMono2; -fx-font-weight: bold;");

    myMaskField.setMask("Xc");
    myMaskField.setText(c.getValue());
    myMaskField.setPlainText(c.getValue());

    HBox.setHgrow(myMaskField, Priority.ALWAYS);
    hbox2.getChildren().addAll(commLabel, myMaskField);

    HBox hbox3 = new HBox();
    Label enterLabel = new Label("Send on Enter:");
    enterLabel.setPrefSize(labelwidth, labelheight);

    ChoiceBox<String> sendOnEnterCombo = new ChoiceBox<>();
    sendOnEnterCombo.getItems().clear();
    List<String> list = Arrays.asList("CR", "LF", "CR+LF", "NONE", "Custom...");
    sendOnEnterCombo.getItems().addAll(list);
    if (list.contains(c.getLinefeed())) {
      sendOnEnterCombo.getSelectionModel().select(c.getLinefeed());
    } else {
      sendOnEnterCombo.getSelectionModel().select("NONE");
    }
    sendOnEnterCombo.setStyle("-fx-font-size:11;");

    sendOnEnterCombo.setOnMouseClicked((MouseEvent event) -> {
//      logger.info("Event: "+event.toString());
      if (event.getTarget() instanceof Text) {
        Text text = (Text) event.getTarget();
        if (text.getText().equals("Custom...")) {
          event.consume();
          if (c.getScript().trim().isEmpty()) {
            c.setScript(ScriptEvaluator.defaultScript);
          }
          c.setScript(DialogFactory.showJavascriptDialog(c.getScript()));
        }
      }
    });
    sendOnEnterCombo.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
      Settings.setValue("SendOnEnter", sendOnEnterCombo.getSelectionModel().getSelectedItem());
      if (newValue.equals("Custom...")) {
        logger.info("Select custom Value");

        if (c.getScript().trim().isEmpty()) {
          c.setScript(ScriptEvaluator.defaultScript);
        }
        c.setScript(DialogFactory.showJavascriptDialog(c.getScript()));
      }
    });

    hbox3.getChildren().addAll(enterLabel, sendOnEnterCombo);

    mainVBox.getChildren().addAll(hbox1, hbox2, hbox3);

    dialog.getDialogPane().setContent(mainVBox);

// Request focus on the username field by default.
    Platform.runLater(() -> myMaskField.requestFocus());

// Convert the result to a username-password-pair when the login button is clicked.
    dialog.setResultConverter(dialogButton -> {
      return dialogButton == doneButtonType;
    });

    Optional<Boolean> result = dialog.showAndWait();

    result.ifPresent(resultString -> {
      if (resultString) {
//        c.valueProperty().unbind();
        c.setValue(myMaskField.getPlainText());
        logger.info(myMaskField.getPlainText());
        c.setLinefeed(sendOnEnterCombo.getSelectionModel().getSelectedItem());
        c.setName(nameField.getText());
      }
    });

  }

  public static String getFilenname(Window owner) {
    String rv = null;
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Open File");
    //Set extension filter
    FileChooser.ExtensionFilter extFilter
            = new FileChooser.ExtensionFilter(
                    "commands file (*" + ".cmd" + ")", "*" + ".cmd");
    fileChooser.getExtensionFilters().add(extFilter);

    File dir = new File(
            Settings.getValue("WorkingDirectory",
                    System.getProperty("user.home")));
    if (!dir.exists()) {
      dir = new File(System.getProperty("user.home"));
    }
    fileChooser.setInitialDirectory(dir);

    File file = fileChooser.showOpenDialog(owner);

    if ((file == null)) {
      rv = null;
    } else {
      rv = file.getAbsolutePath();
      Settings.setValue("WorkingDirectory", file.getParent());

    }
    if (rv != null) {
      if (!rv.endsWith(".cmd")) {
        rv += ".cmd";
      }
    }
    return rv;

//    return null;
  }
}
