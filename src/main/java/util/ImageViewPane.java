package util;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;


public class ImageViewPane extends Region {

    private ObjectProperty<ImageView> imageViewProperty = new SimpleObjectProperty<>();

    public ObjectProperty<ImageView> imageViewProperty() {
        return imageViewProperty;
    }

    public ImageView getImageView() {
        return imageViewProperty.get();
    }

    public void setImageView(ImageView imageView) {
        this.imageViewProperty.set(imageView);
    }


    private ObjectProperty<Pane> drawPaneProperty = new SimpleObjectProperty<>();

    public ObjectProperty<Pane> drawPaneProperty() {
        return drawPaneProperty;
    }

    public Pane getDrawPane() {
        return drawPaneProperty.get();
    }

    public void setDrawPane(Pane drawPane) {
        drawPaneProperty.set(drawPane);
    }


    private ObjectProperty<Rectangle2D> viewportProperty = new SimpleObjectProperty<>();

    public ObjectProperty<Rectangle2D> viewportProperty() {
        return viewportProperty;
    }

    public Rectangle2D getViewport() {
        return viewportProperty.get();
    }

    public void setViewport(Rectangle2D viewport) {
        viewportProperty.set(viewport);
    }


    @Override
    protected void layoutChildren() {
        ImageView imageView = imageViewProperty.get();
        if (imageView != null) {
            imageView.setFitWidth(getWidth());
            imageView.setFitHeight(getHeight());
            layoutInArea(imageView, 0, 0, getWidth(), getHeight(), 0, HPos.CENTER, VPos.CENTER);
        }
        Pane drawPane = getDrawPane();
        if (drawPane != null) {
            positionInArea(drawPane, 0, 0, getWidth(), getHeight(), 0, HPos.CENTER, VPos.CENTER);
        }
        super.layoutChildren();
    }

    public ImageViewPane(Image image) {
        imageViewProperty.addListener((prop, oldImageView, newImageView) -> {
            if (oldImageView != null) {
                getChildren().remove(oldImageView);
            }
            if (newImageView != null) {
                getChildren().add(newImageView);
                setupImageViewListeners();
            }
        });
        drawPaneProperty.addListener((prop, oldDrawPane, newDrawPane) -> {
            if (oldDrawPane != null) {
                getChildren().remove(oldDrawPane);
            }
            if (newDrawPane != null) {
                getChildren().add(newDrawPane);
            }
        });

        this.drawPaneProperty.set(new Pane());
        this.imageViewProperty.set(new ImageView(image));
    }

    private void clipChildren() {
        final Rectangle clip = new Rectangle();
        this.setClip(clip);

        layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
            clip.setWidth(newValue.getWidth());
            clip.setHeight(newValue.getHeight());
        });
    }

    private static final int MIN_PIXELS = 400;
    private void setupImageViewListeners() {
        ObjectProperty<Point2D> mouseDown = new SimpleObjectProperty<>();

        getImageView().setOnMousePressed(e -> {
            Point2D mousePress = imageViewToImageCoordinate(getImageView(), new Point2D(e.getX(), e.getY()));
            mouseDown.set(mousePress);
        });

        getImageView().setOnMouseDragged(e -> {
            Point2D dragPoint = imageViewToImageCoordinate(getImageView(), new Point2D(e.getX(), e.getY()));
            shift(dragPoint.subtract(mouseDown.get()));
            mouseDown.set(imageViewToImageCoordinate(getImageView(), new Point2D(e.getX(), e.getY())));
        });

        getImageView().setOnScroll(e -> {
            Rectangle2D viewport = getImageView().getViewport();
            double width = getImageView().getImage().getWidth();
            double height = getImageView().getImage().getHeight();
            double delta = -e.getDeltaY();
            double scale = clamp(Math.pow(1.01, delta),
                    // don't scale so we're zoomed in to fewer than MIN_PIXELS in any direction:
                    Math.min(MIN_PIXELS / viewport.getWidth(), MIN_PIXELS / viewport.getHeight()),
                    // don't scale so that we're bigger than image dimensions
                    Math.max(width / viewport.getWidth(), height / viewport.getHeight()));

            Point2D mouse = imageViewToImageCoordinate(getImageView(), new Point2D(e.getX(), e.getY()));
            double newWidth = viewport.getWidth() * scale;
            double newHeight = viewport.getHeight() * scale;
            // To keep the visual point under the mouse from moving, we need
            // (x - newViewportMinX) / (x - currentViewportMinX) = scale
            // where x is the mouse X coordinate in the image

            // solving this for newViewportMinX gives

            // newViewportMinX = x - (x - currentViewportMinX) * scale

            // we then clamp this value so the image never scrolls out
            // of the floorView
            double newMinX = clamp(
                    mouse.getX() - scale*(mouse.getX() - viewport.getMinX()),
                    0,
                    width - newWidth);
            double newMinY = clamp(
                    mouse.getY() - scale*(mouse.getY() - viewport.getMinY()),
                    0,
                    height - newHeight
            );

            setViewport(new Rectangle2D(newMinX, newMinY, newWidth, newHeight));
        });

        getImageView().setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) resetImageView();
        });
    }


    private void resetImageView() {
        double width = getImageView().getImage().getWidth();
        double height = getImageView().getImage().getHeight();
        if (getViewport() == null) {
            setViewport(new Rectangle2D(0, 0, width, height));
        } else {
            setViewport(getViewport());
        }
    }

    private void shift(Point2D delta) {
        Rectangle2D viewport = getImageView().getViewport();

        double width = getImageView().getImage().getWidth();
        double height = getImageView().getImage().getHeight();

        double maxX = width - viewport.getWidth();
        double maxY = height - viewport.getHeight();

        double minX = clamp(viewport.getMinX() - delta.getX(), 0, maxX);
        double minY = clamp(viewport.getMinY() - delta.getY(), 0, maxY);

        setViewport(new Rectangle2D(minX, minY, viewport.getWidth(), viewport.getHeight()));
    }

    private static Point2D imageViewToImageCoordinate(ImageView floorView, Point2D floorViewCoordinates) {
        // Transform mouse coordinates on the floorView to the pixel coordinates of the image.
        double xProp = floorViewCoordinates.getX() / floorView.getBoundsInLocal().getWidth();
        double yProp = floorViewCoordinates.getY() / floorView.getBoundsInLocal().getHeight();

        Rectangle2D viewport = floorView.getViewport();
        return new Point2D(
                viewport.getMinX() + xProp*viewport.getWidth(),
                viewport.getMinY() + yProp*viewport.getHeight()
        );
    }

    private static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }
}