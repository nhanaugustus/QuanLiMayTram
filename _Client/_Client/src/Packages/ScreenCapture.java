package Packages;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import javax.imageio.ImageIO;

public class ScreenCapture {

    // Thread lưu trữ số byte trước đó đã chụp và gửi
    private final ThreadLocal<byte[]> previous = new ThreadLocal();

    public ScreenCapture() {
    }

    public Object execute(Robot robot) throws IOException {
        // Chụp màn hình
        Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
        Rectangle shotArea = new Rectangle(defaultToolkit.getScreenSize());
        BufferedImage image = robot.createScreenCapture(shotArea);
        // Chuyển ảnh sang bytes để gửi cho server
        byte[] bytes = toBytes(image);
        // Chỉ gửi nếu số byte thật sự thay đổi
        // Bằng cách so sánh với số byte trước đó
        byte[] prev = previous.get();
        if (prev != null && Arrays.equals(bytes, prev)) {
            return null;
        }
        // Thêm số byte lần chụp này để so sánh lần tiếp theo
        previous.set(bytes);
        return bytes;
    }

    private byte[] toBytes(BufferedImage img) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "jpg", baos);
        baos.flush();
        byte[] bytes = baos.toByteArray();
        baos.close();
        return bytes;
    }
}
