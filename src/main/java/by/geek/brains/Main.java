package by.geek.brains;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{  // стартер прилаги
        Parent root = FXMLLoader.load(getClass().getResource("/main.fxml")); // загрузчик сцены для интерфейса приложения
        primaryStage.setTitle("Java File Manager [GeekBrains]"); // заголовок окна
        primaryStage.setScene(new Scene(root, 1280, 600)); // экран
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
