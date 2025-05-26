
// MainController.java
package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.net.InetAddress;

public class MainController {

    @FXML private Button btnUsuario;
    @FXML private Button btnServidor;
    @FXML private Text ipText;

    public void initialize() {
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            if (ipText != null) {
                ipText.setText("IP: " + ip);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void abrirLogin() {
        try {
            Stage stage = (Stage) btnUsuario.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/Login.fxml"));
            stage.setScene(new Scene(loader.load()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void abrirServidor() {
        try {
            Stage stage = (Stage) btnServidor.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/Servidor.fxml"));
            stage.setScene(new Scene(loader.load()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}