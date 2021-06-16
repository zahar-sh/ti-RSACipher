package BigInt.sample;

import BigInt.RSA;
import BigInt.StreamCoder;
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
import java.math.BigInteger;
import java.util.function.Consumer;

public class Main {
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

    private static final BigInteger I256 = BigInteger.valueOf(256);
    private BigInteger p, q, d;
    private StreamCoder encoder, decoder;


    private final FileChooser fileChooser = new FileChooser();
    private void setIn(TextField in) {
        File f = fileChooser.showOpenDialog(null);
        in.setText(f == null ? "" : f.getAbsolutePath());
    }
    private void setOut(TextField out) {
        File f = fileChooser.showSaveDialog(null);
        out.setText(f == null ? "" : f.getAbsolutePath());
    }

    private void labelInfo(String text) {
        label.setVisible(true);
        label.setText(text);
        label.setTextFill(Color.WHITE);
        labelTr.playFromStart();
    }
    private void labelErr(String text) {
        labelTr.stop();
        label.setTextFill(Color.RED);
        label.setText(text);
    }
    private void labelClear() {
        label.setText("");
    }

    private void build() {
        if (p.compareTo(I256) < 0) throw new ArithmeticException("p should be > 256");
        if (RSA.isNotPrime(p)) throw new ArithmeticException("p should be prime");
        if (RSA.isNotPrime(q)) throw new ArithmeticException("q should be prime");

        BigInteger f = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        BigInteger e = RSA.modInverse(d, f);
        BigInteger r = p.multiply(q);

        fTF.setText(f.toString());
        eTF.setText(e.toString());
        rTF.setText(r.toString());

        encoder = RSA.createEncoder(e, r);
        decoder = RSA.createDecoder(d, r);
    }
    private void code(TextField fIn, TextField fOut, StreamCoder coder, String msg) throws Exception {
        try (InputStream in = new BufferedInputStream(new FileInputStream(fileFromTF(fIn)));
             OutputStream out = new BufferedOutputStream(new FileOutputStream(fileFromTF(fOut)))) {
            long l = System.currentTimeMillis();
            coder.code(in, out);
            labelInfo(msg + (System.currentTimeMillis() - l) + "ms");
        }
    }

    private EventHandler<KeyEvent> create(TextField f, Consumer<BigInteger> setter) {
        return keyEvent -> {
            try {
                String s = f.getText().trim();
                if (s.isEmpty()) {
                    setter.accept(BigInteger.ZERO);
                    buildErr("Fill in the field");
                    return;
                }
                setter.accept(new BigInteger(s));
                build();
                labelClear();
            } catch (NumberFormatException e) {
                setter.accept(BigInteger.ZERO);
                buildErr("Illegal characters");
            } catch (Exception e) {
                buildErr(e.getMessage());
            }
        };
    }

    private void buildErr(String s2) {
        labelErr(s2);

        fTF.setText("");
        eTF.setText("");
        rTF.setText("");
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

        p = new BigInteger(pTF.getText().trim());
        q = new BigInteger(qTF.getText().trim());
        d = new BigInteger(dTF.getText().trim());

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