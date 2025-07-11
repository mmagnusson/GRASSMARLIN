package ui.fingerprint.tree;


import core.fingerprint.FingerprintState;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

public class FPItem extends TreeItem<String> {

    private String name;
    private String author;
    private String description;
    private List<String> tags;

    private BooleanProperty dirtyProperty;
    private BooleanProperty enabledProperty;
    private ObjectProperty<Path> pathProperty;

    public FPItem(FingerprintState fpState) {
        super(fpState.getFingerprint().getHeader().getName());
        this.pathProperty = new SimpleObjectProperty<>();
        this.pathProperty.bind(fpState.pathProperty());
        this.setName(fpState.getFingerprint().getHeader().getName());
        this.author = fpState.getFingerprint().getHeader().getAuthor();
        this.description = fpState.getFingerprint().getHeader().getDescription();
        this.dirtyProperty = new SimpleBooleanProperty();
        this.dirtyProperty.bind(fpState.dirtyProperty());
        this.enabledProperty = new SimpleBooleanProperty();
        this.enabledProperty.bind(fpState.enabledProperty());

        HBox graphicsBox = new HBox(3);
        Image checkImage;
        InputStream checkStream = getClass().getResourceAsStream("/images/microsoft/112_Tick_Green_64x64_72.png");
        if (checkStream != null) {
            checkImage = new Image(checkStream);
        } else {
            // Create a simple fallback image programmatically
            checkImage = createFallbackImage();
        }
        ImageView enabledView = new ImageView();
        enabledView.setFitHeight(16);
        enabledView.setFitWidth(16);
        enabledView.imageProperty().bind(Bindings.when(enabledProperty).then(checkImage).otherwise((Image)null));
        Text dirtyText = new Text();
        dirtyText.textProperty().bind(Bindings.when(dirtyProperty()).then("*").otherwise(" "));
        graphicsBox.getChildren().addAll(enabledView, dirtyText);

        this.setGraphic(graphicsBox);
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.setValue(name);
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String toString() {
        return this.getName();
    }

    public BooleanProperty dirtyProperty() {
        return this.dirtyProperty;
    }

    public ObjectProperty<Path> pathProperty() {
        return this.pathProperty;
    }

    private Image createFallbackImage() {
        // Create a simple colored rectangle for fallback
        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(16, 16);
        javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(javafx.scene.paint.Color.GREEN);
        gc.fillRect(0, 0, 16, 16);
        gc.setFill(javafx.scene.paint.Color.WHITE);
        gc.fillText("✓", 4, 12);
        
        return canvas.snapshot(null, null);
    }
}
