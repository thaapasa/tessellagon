package fi.tuska.tessellagon.j3d;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.Texture;
import javax.media.j3d.Texture2D;
import javax.vecmath.Color4f;

public class CustomTexture implements ImageComponent2D.Updater {

    private Texture2D texture;
    private BufferedImage background;
    private BufferedImage image;
    private ImageComponent2D imageComponent;

    protected int width;
    protected int height;

    public CustomTexture(BufferedImage backgroundImage, String name) {
        setBackgroundImage(backgroundImage, name);
    }

    public CustomTexture(String backgroundFile) {
        setBackgroundImage(loadImage(backgroundFile), backgroundFile);
    }

    protected CustomTexture() {
    }

    protected static BufferedImage loadImage(String filename) {
        try {
            return ImageIO.read(new File(filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void setBackgroundImage(BufferedImage backgroundImage, String name) {
        this.background = backgroundImage;
        this.width = background.getWidth();
        this.height = background.getHeight();
        texture = new Texture2D(Texture2D.BASE_LEVEL, Texture2D.RGBA, width, height);

        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        image.getGraphics().drawImage(background, 0, 0, null);

        imageComponent = new ImageComponent2D(ImageComponent2D.FORMAT_RGBA, image, true, false);
        imageComponent.setCapability(ImageComponent2D.ALLOW_IMAGE_WRITE);
        imageComponent.setCapabilityIsFrequent(ImageComponent2D.ALLOW_IMAGE_WRITE);
        // texture.setCapability(Texture.ALLOW_IMAGE_WRITE);
        // texture.setCapabilityIsFrequent(Texture.ALLOW_IMAGE_WRITE);
        texture.setImage(0, imageComponent);

        texture.setBoundaryModeS(Texture.CLAMP);
        texture.setBoundaryModeT(Texture.CLAMP);
        texture.setBoundaryColor(new Color4f(0.0f, 0.0f, 0.0f, 0.0f));

        texture.setName(name);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Texture2D getTexture() {
        return texture;
    }

    public void updateTexture() {
        // texture.setImage(0, imageComponent);
        imageComponent.updateData(this, 0, 0, width, height);
    }

    protected Graphics getGraphics() {
        return image.getGraphics();
    }

    protected Graphics drawBackground(BufferedImage bgImage) {
        Graphics g = getGraphics();
        g.drawImage(bgImage, 0, 0, null);
        return g;
    }

    protected Graphics drawBackground() {
        Graphics g = getGraphics();
        g.drawImage(background, 0, 0, null);
        return g;
    }

    @Override
    public void updateData(ImageComponent2D imageComponent, int x, int y, int width, int height) {
        // What here..
    }

}
