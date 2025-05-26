// App.java
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class App extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        try {
            // Configuração do stage inicial
            primaryStage = stage;
            Parent root = FXMLLoader.load(getClass().getResource("/gui/Main.fxml"));
            Scene scene = new Scene(root);
            
            // Definir título da janela
            stage.setTitle("Tela de Login");
            
            // Impedir redimensionamento da janela
            stage.setResizable(false);
            
            // Configurar cena e mostrar a janela
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erro ao carregar a interface inicial.");
        }
    }

    public static void changeScene(String fxml) {
        try {
            // Carregar a nova cena
            Parent pane = FXMLLoader.load(App.class.getResource("/gui/" + fxml));
            primaryStage.getScene().setRoot(pane);
        } catch (Exception e) {
            e.printStackTrace();
            // Exibir um alerta caso ocorra um erro ao carregar a cena
            showErrorAlert("Erro ao mudar a cena: " + fxml);
        }
    }

    // Método para mostrar alertas de erro
    private static void showErrorAlert(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText("Algo deu errado!");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
