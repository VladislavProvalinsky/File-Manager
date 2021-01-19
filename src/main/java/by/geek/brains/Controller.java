package by.geek.brains;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;

public class Controller {

    @FXML
    VBox leftPanel, rightPanel;

    public void btnExitAction(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void copyButtonAction(ActionEvent actionEvent) { // готово!
        PanelController leftPC = (PanelController) leftPanel.getProperties().get("ctrl");
        PanelController rightPC = (PanelController) rightPanel.getProperties().get("ctrl");

        if (leftPC.getSelectedFilename() == null && rightPC.getSelectedFilename() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Ни один файл не был выбран", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        PanelController srcPC = null, dstPC = null;
        if (leftPC.getSelectedFilename() != null) {
            srcPC = leftPC;
            dstPC = rightPC;
        }
        if (rightPC.getSelectedFilename() != null) {
            srcPC = rightPC;
            dstPC = leftPC;
        }
        Path srcPath = Paths.get(srcPC.getCurrentPath(), srcPC.getSelectedFilename());
        Path dstPath = Paths.get(dstPC.getCurrentPath(), srcPC.getSelectedFilename());
        try {
            if (srcPath.equals(dstPath)) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Попытка скопировать файл/директорию в этот же каталог!", ButtonType.OK);
                alert.showAndWait();
                return;
            }
            PanelController finalSrcPC = srcPC;
            if (dstPC.filesTable.getItems().stream().anyMatch(path -> path.getFilename().equals(finalSrcPC.getSelectedFilename()))) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Файл/директория с таким именем в каталоге уже существует!", ButtonType.OK);
                alert.showAndWait();
                return;
            }
            Alert alert = new Alert(Alert.AlertType.WARNING, "Скопировать " + srcPC.getSelectedFilename() + " в каталог " + dstPC.getCurrentPath() + "?", ButtonType.OK, ButtonType.CANCEL);
            alert.showAndWait();
            if (alert.getResult().equals(ButtonType.OK)) {
                Files.walkFileTree(srcPath, new MyFileCopyVisitor(srcPath, dstPath));
                dstPC.updateList(Paths.get(dstPC.getCurrentPath()));
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось скопировать указанный файл", ButtonType.OK);
            alert.showAndWait();
        }

    }

    public void deleteButtonAction(ActionEvent actionEvent) {

        PanelController leftPC = (PanelController) leftPanel.getProperties().get("ctrl");
        PanelController rightPC = (PanelController) rightPanel.getProperties().get("ctrl");

        if (leftPC.getSelectedFilename() == null && rightPC.getSelectedFilename() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Ни один файл не был выбран", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        PanelController srcPC = null, targetPC = null;
        if (leftPC.getSelectedFilename() != null) {
            srcPC = leftPC;
            targetPC = rightPC;
        }
        else {
            srcPC = rightPC;
            targetPC = leftPC;
        }

        Path srcPath = Paths.get(srcPC.getCurrentPath(), srcPC.getSelectedFilename());

        try {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Вы действительно хотите удалить " + srcPath.getFileName(), ButtonType.OK, ButtonType.CANCEL);
            alert.showAndWait();
            if (alert.getResult().equals(ButtonType.OK)) {
                if (Files.isDirectory(srcPath)) {
//                    Files.walkFileTree(srcPath, new MyFileDeleteVisitor());
                    Files.walk(srcPath)
                            .sorted(Comparator.reverseOrder())
                            .forEach(path -> {
                                try {
                                    System.out.println("Deleting: " + path);
                                    Files.delete(path);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                } else {
                    Files.delete(srcPath);
                }
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось удалить указанный файл", ButtonType.OK);
            alert.showAndWait();
        }
        srcPC.updateList(Paths.get(srcPC.getCurrentPath()));
        targetPC.updateList(Paths.get(targetPC.getCurrentPath()));
    }

    public void buttonMoveAction(ActionEvent actionEvent) {
        PanelController leftPC = (PanelController) leftPanel.getProperties().get("ctrl");
        PanelController rightPC = (PanelController) rightPanel.getProperties().get("ctrl");

        if (leftPC.getSelectedFilename() == null && rightPC.getSelectedFilename() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Ни один файл не был выбран", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        PanelController srcPC = null, dstPC = null;
        if (leftPC.getSelectedFilename() != null) {
            srcPC = leftPC;
            dstPC = rightPC;
        }
        if (rightPC.getSelectedFilename() != null) {
            srcPC = rightPC;
            dstPC = leftPC;
        }

        Path srcPath = Paths.get(srcPC.getCurrentPath(), srcPC.getSelectedFilename());
        Path dstPath = Paths.get(dstPC.getCurrentPath());

        PanelController finalSrcPC = srcPC;
        if (leftPC.getCurrentPath().equals(rightPC.getCurrentPath())) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Попытка переместить файл/директорию в этот же каталог!", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        if (dstPath.startsWith(srcPath)) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Нельзя переместить корневую директорию внутрь себя!", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        if (dstPC.filesTable.getItems().stream().anyMatch(path -> path.getFilename().equals(finalSrcPC.getSelectedFilename()))) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Файл/директория с таким именем в каталоге уже существует!", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        String text = String.format("Вы действительно хотите переместить %s в каталог %s?", srcPath.getFileName(), dstPath);
        Alert alert = new Alert(Alert.AlertType.WARNING, text, ButtonType.OK, ButtonType.CANCEL);
        alert.showAndWait();
        if (alert.getResult().equals(ButtonType.OK)) {
            try {
                Files.walkFileTree(srcPath, new MyFileMovingVisitor(srcPath, dstPath));
            } catch (IOException e) {
                Alert error = new Alert(Alert.AlertType.ERROR, "Не удалось переместить указанный файл/директорию", ButtonType.OK);
                error.showAndWait();
            }
            leftPC.updateList(Paths.get(leftPC.getCurrentPath()));
            rightPC.updateList(Paths.get(rightPC.getCurrentPath()));
        }
    }

    public void updateApi(ActionEvent actionEvent) {
        PanelController leftPC = (PanelController) leftPanel.getProperties().get("ctrl");
        PanelController rightPC = (PanelController) rightPanel.getProperties().get("ctrl");

        leftPC.updateList(Paths.get(leftPC.getCurrentPath()));
        rightPC.updateList(Paths.get(rightPC.getCurrentPath()));
    }

    class MyFileCopyVisitor extends SimpleFileVisitor<Path> {

        Path source;
        Path destination;

        public MyFileCopyVisitor(Path source, Path destination) {
            this.source = source;
            this.destination = destination;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Path newTarget = destination.resolve(source.relativize(dir));
            Files.copy(dir, newTarget, StandardCopyOption.REPLACE_EXISTING);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Path newTarget = destination.resolve(source.relativize(file));
            Files.copy(file, newTarget);
            return FileVisitResult.CONTINUE;
        }
    }

    class MyFileDeleteVisitor extends SimpleFileVisitor<Path> { // Только для пустых или не пустых директорий!

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }
    }

    class MyFileMovingVisitor extends SimpleFileVisitor<Path> {

        private Path source;
        private Path target;
        private boolean includeRootDir = true;

        public MyFileMovingVisitor(Path source, Path target) {
            this.source = source;
            this.target = target;
        }

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            Path newDir = target.resolve(source.getParent().relativize(file));
            Files.move(file, newDir, StandardCopyOption.REPLACE_EXISTING);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
            if (includeRootDir) {
                Files.copy(dir, target.resolve(dir.getFileName()), StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
                this.includeRootDir = false;
            } else {
                Path newDir = target.resolve(source.getParent().relativize(dir));
                Files.copy(dir, newDir, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
            Path newDir = target.resolve(source.getParent().relativize(dir));
            FileTime time = Files.getLastModifiedTime(dir);
            Files.setLastModifiedTime(newDir, time);
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
    }
}
