package by.geek.brains;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class PanelController implements Initializable {


    @FXML
    TableView<FileInfo> filesTable;

    @FXML
    ComboBox<String> discsBox;

    @FXML
    TextField pathField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        TableColumn<FileInfo, String> fileTypeColumn = new TableColumn<>("Тип");
        fileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
        fileTypeColumn.setPrefWidth(60);

        TableColumn<FileInfo, String> filenameColumn = new TableColumn<>("Имя");
        filenameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFilename()));
        filenameColumn.setPrefWidth(200);

        TableColumn<FileInfo, Long> fileSizeColumn = new TableColumn<>("Размер");
        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        fileSizeColumn.setPrefWidth(240);
        fileSizeColumn.setCellFactory(column -> {
            return new TableCell<FileInfo, Long>() {
                @Override
                protected void updateItem(Long item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        String text = String.format("%,d bytes", item);
                        if (item == -1L)
                            text = "[DIR]";
                        setText(text);
                    }
                }
            };
        });

        TableColumn<FileInfo, String> fileDateColumn = new TableColumn<>("Дата изменения");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
        fileDateColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified().format(formatter)));
        fileDateColumn.setPrefWidth(240);

        filesTable.getColumns().addAll(fileTypeColumn, filenameColumn, fileSizeColumn, fileDateColumn);
        filesTable.getSortOrder().add(fileTypeColumn);

        discsBox.getItems().clear();
        for (Path p: FileSystems.getDefault().getRootDirectories()) {
            discsBox.getItems().add(p.toString());
        }
        discsBox.getSelectionModel().select(0);

        filesTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2){
                    Path path = Paths.get(pathField.getText()).resolve(filesTable.getSelectionModel().getSelectedItem().getFilename());
                    if (Files.isDirectory(path)){
                        updateList(path);
                    }
                }
            }
        });

        updateList(Paths.get("."));
    }

    public void updateList(Path path) {
        try {
            Path newPath = path.normalize().toAbsolutePath();
            pathField.setText(newPath.toString());
            System.out.println(Arrays.toString(filesTable.getItems().toArray()));
            filesTable.getItems().clear();
            System.out.println(Arrays.toString(filesTable.getItems().toArray()));
            System.out.println("----------------------");
            filesTable.getItems().addAll(Files.list(newPath).map(FileInfo::new).collect(Collectors.toList()));
            filesTable.sort();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "По какой-то причине не удалось обновить список файлов", ButtonType.OK);
            alert.showAndWait();
        }
    }

//    public void updateListForDeleteAndMove(Path path) {
//        try {
//            Path newPath = path.getParent().normalize().toAbsolutePath();
//            pathField.setText(newPath.toString());
//            System.out.println(Arrays.toString(filesTable.getItems().toArray()));
//            filesTable.getItems().clear();
//            System.out.println(Arrays.toString(filesTable.getItems().toArray()));
//            List<String> collect = Files.list(newPath).map(String::valueOf).filter(el ->
//                    !el.equals(String.valueOf(path)))
//                    .collect(Collectors.toList());
//            System.out.println(collect);
//            System.out.println("----------------------");
//            filesTable.getItems().addAll(Files.list(newPath)
//                    .map(String::valueOf)
//                    .filter(el -> !el.equals(String.valueOf(path)))
//                    .map (Paths::get)
//                    .map(FileInfo::new)
//                    .collect(Collectors.toList()));
//            filesTable.sort();
//        } catch (IOException e) {
//            Alert alert = new Alert(Alert.AlertType.WARNING, "По какой-то причине не удалось обновить список файлов", ButtonType.OK);
//            alert.showAndWait();
//        }
//    }


    public void btnPathUpAction(ActionEvent actionEvent) {
        Path upperPath = Paths.get(pathField.getText()).getParent();
        if (upperPath != null) {
            updateList(upperPath);
        }
    }

    public void selectDiscAction(ActionEvent actionEvent) {
        ComboBox<String> element = (ComboBox<String>) actionEvent.getSource();
        updateList(Paths.get(element.getSelectionModel().getSelectedItem()));
    }

    public String getSelectedFilename(){
        if (!filesTable.isFocused() || filesTable.getSelectionModel().getSelectedItem() == null){
            return null;
        }
        return filesTable.getSelectionModel().getSelectedItem().getFilename();
    }

    public String getCurrentPath(){
        return pathField.getText();
    }
}
