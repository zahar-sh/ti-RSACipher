package Long.sample;

import Long.RSA;
import Long.StreamCoder;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.*;
import java.util.function.LongConsumer;

public class Main {
    private static final String errorStyle = "-fx-background-color: red; -fx-text-fill: #fff";
    private static final Effect def = new DropShadow(3, 1, 1, Color.BLACK);
    private static final Effect active = new DropShadow(5, 2, 2,  Color.rgb(255, 195, 0));

    public static void completeAlert(String message, Alert.AlertType type) {
        alert("Complete!", message, "", type);
    }
    public static void errorAlert(String message) {
        alert("Error", message, "An error occurred during execution.", Alert.AlertType.ERROR);
    }
    public static void alert(String title, String message, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private static File fileFromTF(TextField f) {
        return new File(f.getText().trim());
    }

    @FXML
    private TextField pTF, qTF, fTF, dTF, eTF, rTF;
    @FXML
    private TextField encodeIn, encodeOut;
    @FXML
    private TextField decodeIn, decodeOut;
    @FXML
    private Label label;
    private Timeline labelTr;
    private boolean fix;

    private long p, q, d;
    private StreamCoder encoder, decoder;

    private void setLabelText(String text) {
        label.setVisible(true);
        label.setText(text);
        labelTr.playFromStart();
    }

    private final FileChooser fileChooser = new FileChooser();
    private void setIn(TextField in) {
        File f = fileChooser.showOpenDialog(null);
        in.setText(f == null ? "" : f.getAbsolutePath());
    }
    private void setOut(TextField out) {
        File f = fileChooser.showSaveDialog(null);
        out.setText(f == null ? "" : f.getAbsolutePath());
    }

    private void err(TextField f) {
        f.setStyle(errorStyle);
        fTF.setText("");
        eTF.setText("");
        rTF.setText("");
    }
    private void normal(TextField f) {
        f.setStyle("");
    }

    private void build() throws ArithmeticException {
        if (p < 256) throw new ArithmeticException("p should be > 256");
        if (RSA.isNotPrime(p)) throw new ArithmeticException("p should be prime");
        if (RSA.isNotPrime(q)) throw new ArithmeticException("q should be prime");

        long f = (p - 1) * (q - 1);
        long e =  RSA.modInverse(d, f);
        long r = p * q;

        fTF.setText(Long.toString(f));
        eTF.setText(Long.toString(e));
        rTF.setText(Long.toString(r));

        encoder = RSA.createEncoder(e, r);
        decoder = RSA.createDecoder(d, r);
    }
    private void code(TextField fIn, TextField fOut, StreamCoder coder, String msg) throws Exception {
        try (InputStream in = new BufferedInputStream(new FileInputStream(fileFromTF(fIn)));
             OutputStream out = new BufferedOutputStream(new FileOutputStream(fileFromTF(fOut)))) {
            long l = System.currentTimeMillis();
            coder.code(in, out);
            setLabelText(msg + (System.currentTimeMillis() - l) + "ms");
        }
    }

    private EventHandler<KeyEvent> create(TextField f, LongConsumer setter) {
        return keyEvent -> {
            try {
                setter.accept(Long.parseLong(f.getText().trim()));
                normal(f);
                build();
            } catch (Exception e){
                err(f);
                errorAlert(e.getMessage());
            }
        };
    }

    @FXML
    private void initialize() {
        labelTr = new Timeline(new KeyFrame(Duration.seconds(3), event -> label.setVisible(false)));
        labelTr.setCycleCount(1);
        label.setEffect(def);
        label.setOnMousePressed(keyEvent -> {
            if (!fix) {
                labelTr.stop();
                label.setEffect(active);
                fix = true;
            }
        });
        label.setOnMouseReleased(keyEvent -> {
            if (fix) {
                labelTr.playFromStart();
                label.setEffect(def);
                fix = false;
            }
        });

        p = Long.parseLong(pTF.getText().trim());
        q = Long.parseLong(qTF.getText().trim());
        d = Long.parseLong(dTF.getText().trim());

        build();

        pTF.setOnKeyReleased(create(pTF, bi -> p = bi));
        qTF.setOnKeyReleased(create(qTF, bi -> q = bi));
        dTF.setOnKeyReleased(create(dTF, bi -> d = bi));
    }

    @FXML
    private void selectEncodeIn() {
        setIn(encodeIn);
    }
    @FXML
    private void selectEncodeOut() {
        setOut(encodeOut);
    }
    @FXML
    private void selectDecodeIn() {
        setIn(decodeIn);
    }
    @FXML
    private void selectDecodeOut() {
        setOut(decodeOut);
    }

    @FXML
    private void encode() {
        try {
            build();
            code(encodeIn, encodeOut, encoder, "Encoded at ");
        } catch (Exception e) {
            errorAlert(e.getMessage());
        }
    }
    @FXML
    private void decode() {
        try {
            build();
            code(decodeIn, decodeOut, decoder, "Decoded at ");
        } catch (Exception e) {
            errorAlert(e.getMessage());
        }
    }
}