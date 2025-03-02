import java.awt.Rectangle;
import java.awt.Dimension;

public class Utility {
	public static String addPath(final String path) { return ((Main.runInEclipse) ? "assets/" : "../assets/") + path; }
	public static Rectangle createRectangle(final int[] size) { return new Rectangle(0, 0, size[0], size[1]); }
	public static Rectangle createRectangle(final int x, final int y, final int[] size) { return new Rectangle(x, y, size[0], size[1]); }
	public static Rectangle createRectangle(final int x, final int y, final int w, final int h) { return new Rectangle(x, y, w, h); }
	public static Dimension createDimension(final int[] dimension) { return new Dimension(dimension[0], dimension[1]); }
}