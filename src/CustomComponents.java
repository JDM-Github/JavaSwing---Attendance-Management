import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.io.File;
import java.awt.geom.*;
import javax.swing.border.*;


public class CustomComponents {
	public static String openFileManager(String origPath) {
		JFileChooser fileChooser = new JFileChooser();
		String currentDirectory = System.getProperty("user.dir");
		fileChooser.setCurrentDirectory(new File(currentDirectory));
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Image files", "jpg", "png", "gif");
		fileChooser.setFileFilter(filter);

		int result = fileChooser.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION) return fileChooser.getSelectedFile().getAbsolutePath();
		return origPath;
	}

	public static class CustomField extends JTextField {
		private static final long serialVersionUID = 1L;
		CustomField(String str, int x, int y, int w, int h) {
			super(str);
			setBounds(x, y, w, h);
			setBorder(new EmptyBorder(0, 10, 0, 5));
			setOpaque(false);
			setFont  (new Font("Impact", Font.PLAIN, 18));

			setHorizontalAlignment(JLabel.LEFT);
			setSelectedTextColor(new Color(0x02, 0x88, 0xe1));
			setSelectionColor   (new Color(0x62, 0xc8, 0xff, 0x68));
			setCaretColor       (new Color(0x02, 0xc8, 0xf1));
			setForeground       (new Color(0x02, 0x88, 0xe1));
		}

		@Override protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setColor     (new Color(250, 250, 250));
			g2.fillRoundRect(2, 2, getWidth()-4, getHeight()-4, 20, 20);
			g2.setColor     (new Color(49, 182, 253, 150));
			g2.setStroke    (new BasicStroke(2));
			g2.drawRoundRect(2, 2, getWidth()-4, getHeight()-4, 20, 20);

			g2.dispose();
			super.paintComponent(g);
		}
	}

	public static class ImagePanel extends JLayeredPane {
		private static final long serialVersionUID = 1L;
		private ImageIcon image         = null;
		private ImageIcon originalImage = null;

		public String  imagePath;
		public boolean isPanelImage = false;

		public ImagePanel(String path) {
			this.setPanelImage(path);
		}
		public void setPanelImage(String path) {
			this.imagePath = path;
			originalImage  = new ImageIcon(path);
			image          = new ImageIcon(originalImage.getImage());
		}

		@Override protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			if (this.isPanelImage) {
				Shape clip = new RoundRectangle2D.Double(17, 2, getWidth()-34, getHeight()-4, 20, 20);
				g2.setClip(clip);
			}

			int imageWidth  = image.getIconWidth();
			int imageHeight = image.getIconHeight();
			if (imageWidth == 0 || imageHeight == 0) { return; }
			double widthScale  = (double)getWidth () / (double)imageWidth;
			double heightScale = (double)getHeight() / (double)imageHeight;

			g2.drawImage(image.getImage(), AffineTransform.getScaleInstance(widthScale, heightScale), this);
			g2.dispose();
		}
	}

	public static class CustomButton extends JButton {
		private static final long serialVersionUID = 1L;

		public int   radius     = 20;
		public Color mainColor  = new Color(9, 91, 192);
		public Color pressColor = new Color(0, 51, 152);
		public boolean isTogglable = false;
		public boolean isActive    = false;
		public Runnable runnable = null;

		public CustomButton(String string, int x, int y, int width, int height, int radius, boolean toggle) {
			this(string, x, y, width, height, radius);
			this.isTogglable = toggle;
		}

		public CustomButton(String string, int x, int y, int width, int height, int radius) {
			this(string, x, y, width, height);
			this.radius = radius;
		}

		public CustomButton(String string, int x, int y, int width, int height) {
			super(string);
			this.setBorder             (null);
			this.setContentAreaFilled  (false);
			this.setOpaque             (false);
			this.setFocusable          (false);
			this.setHorizontalAlignment(JLabel.CENTER);
			this.setForeground         (Color.WHITE);
			this.setFont(new Font("Calibre", Font.BOLD, 20));
			this.setFocusable(false);
			
			this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			this.setBounds(x, y, width, height);

			this.addActionListener(new ActionListener(){
				@Override public void actionPerformed(ActionEvent e) {
					if (isTogglable) isActive = !isActive;
					if (runnable != null) runnable.run();
				}
			});
		}

		@Override protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			if (isTogglable) {
				if (this.isActive) g2.setColor(this.pressColor);
				else g2.setColor(this.mainColor);
			} else {
				if (this.getModel().isPressed()) g2.setColor(this.pressColor);
				else g2.setColor(this.mainColor);
			}

			g2.fillRoundRect(0, 0, getWidth(), getHeight(), this.radius, this.radius);
			g2.setStroke    (new BasicStroke(1));
			g2.setColor     (Color.WHITE);
			g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, this.radius, this.radius);

			g2.dispose();
			super.paintComponent(g);
		}
	}

	public static class CircleImageButton extends JLayeredPane {
		private static final long serialVersionUID = 1L;
		private ImageIcon image         = null;
		private ImageIcon originalImage = null;
		public  String imagePath;

		public Color normalColor = new Color(255, 255, 255, 150);
		public Color hoverColor  = Color.WHITE;

		public Runnable runnable = null;
		public boolean hovered = false;

		public CircleImageButton(String path, Rectangle bounds) {
			this.setBounds(bounds);
			this.setBackground(normalColor);
			this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			this.setPanelImage(path);

			this.addMouseListener(new MouseListener() {
				@Override public void mouseClicked (MouseEvent e) { if (runnable != null) runnable.run(); }
				@Override public void mousePressed (MouseEvent e) {}
				@Override public void mouseReleased(MouseEvent e) {}
				@Override public void mouseEntered (MouseEvent e) { hovered = true;  setBackground(hoverColor ); }
				@Override public void mouseExited  (MouseEvent e) { hovered = false; setBackground(normalColor); }
			});
		}

		public void setPanelImage(String path) {
			this.imagePath = path;
			originalImage  = new ImageIcon(path);
			image          = new ImageIcon(originalImage.getImage());
		}

		@Override protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setClip(new Ellipse2D.Double(2, 2, getWidth()-4, getHeight()-4));

			int imageWidth  = image.getIconWidth();
			int imageHeight = image.getIconHeight();
			if (imageWidth == 0 || imageHeight == 0) { return; }
			double widthScale  = (double)getWidth () / (double)imageWidth;
			double heightScale = (double)getHeight() / (double)imageHeight;

			if (hovered) g2.rotate(0.1, getWidth()/2, getHeight()/2);

			g2.drawImage(image.getImage(), AffineTransform.getScaleInstance(widthScale, heightScale), this);

			g2.setClip  (null);
			g2.setColor (this.getBackground());
			g2.setStroke(new BasicStroke(2));
			g2.drawOval (2, 2, getWidth()-4, getHeight()-4);
			g2.dispose  ();
		}
	}
}