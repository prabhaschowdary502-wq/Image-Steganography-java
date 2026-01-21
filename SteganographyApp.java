import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import com.sun.speech.freetts.*;

public class SteganographyApp extends JFrame {

    BufferedImage image;
    JLabel imageLabel = new JLabel();
    JTextField textField = new JTextField(20);
    JLabel resultLabel = new JLabel(" ");

    public SteganographyApp() {
        setTitle("Image Steganography with AI Speech");
        setSize(700, 500);
        setLayout(new FlowLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JButton selectBtn = new JButton("Select Image");
        JButton hideBtn = new JButton("Hide Text");
        JButton extractBtn = new JButton("Extract Text");

        add(selectBtn);
        add(new JLabel("Enter Text:"));
        add(textField);
        add(hideBtn);
        add(extractBtn);
        add(imageLabel);
        add(resultLabel);

        selectBtn.addActionListener(e -> selectImage());
        hideBtn.addActionListener(e -> hideText());
        extractBtn.addActionListener(e -> extractText());

        setVisible(true);
    }

    // 1️⃣ Select & Resize Image
    void selectImage() {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.showOpenDialog(this);
            File file = chooser.getSelectedFile();

            image = ImageIO.read(file);
            image = resizeImage(image, 300, 300);
            imageLabel.setIcon(new ImageIcon(image));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 2️⃣ Resize to Grayscale
    BufferedImage resizeImage(BufferedImage original, int w, int h) {
        BufferedImage resized = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = resized.createGraphics();
        g.drawImage(original, 0, 0, w, h, null);
        g.dispose();
        return resized;
    }

    // 3️⃣ Hide STRING using LSB
    void hideText() {
        try {
            String text = textField.getText();
            text += "#"; // delimiter

            int index = 0;

            for (char ch : text.toCharArray()) {
                int ascii = (int) ch;

                for (int i = 0; i < 8; i++) {
                    int bit = (ascii >> i) & 1;

                    int x = index % image.getWidth();
                    int y = index / image.getWidth();

                    int pixel = image.getRGB(x, y);
                    int gray = pixel & 0xFF;
                    gray = (gray & 0xFE) | bit;

                    int newPixel = new Color(gray, gray, gray).getRGB();
                    image.setRGB(x, y, newPixel);

                    index++;
                }
            }

            ImageIO.write(image, "jpg", new File("stego_image.jpg"));
            JOptionPane.showMessageDialog(this, "Text Hidden Successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 4️⃣ Extract STRING
    void extractText() {
        try {
            StringBuilder extractedText = new StringBuilder();
            int index = 0;

            while (true) {
                int ascii = 0;

                for (int i = 0; i < 8; i++) {
                    int x = index % image.getWidth();
                    int y = index / image.getWidth();

                    int pixel = image.getRGB(x, y);
                    int gray = pixel & 0xFF;
                    int bit = gray & 1;

                    ascii |= (bit << i);
                    index++;
                }

                char ch = (char) ascii;
                if (ch == '#') break;

                extractedText.append(ch);
            }

            resultLabel.setText("Hidden Text: " + extractedText.toString());
            speak(extractedText.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 5️⃣ AI Text-to-Speech
    void speak(String text) {
        VoiceManager vm = VoiceManager.getInstance();
        Voice voice = vm.getVoice("kevin16");
        voice.allocate();
        voice.speak(text);
        voice.deallocate();
    }

    public static void main(String[] args) {
        new SteganographyApp();
    }
}