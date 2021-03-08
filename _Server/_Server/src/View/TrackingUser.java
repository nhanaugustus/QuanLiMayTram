package View;

import Utils.Utils;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;

public class TrackingUser extends javax.swing.JDialog implements Runnable {

    private Socket socket;
    private boolean isContinued;

    public TrackingUser(Socket socket) {
        this.socket = socket;
        isContinued = true;
        initComponents();
        setTitle("Đang theo dõi " + Utils.getSocketHostname(socket) + "(" + Utils.getSocketIP(socket) + ")...");
        setSize(960, 540);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    @Override
    public void run() {
        try {
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            while (isContinued) {
                // Nhận ảnh trả về từ client
                byte[] bytes = (byte[]) ois.readObject();
                System.out.println("New image is received");
                // Hiển thị ảnh lên panel
                showScreenShot(bytes);
            }
        } catch (Exception ex) {
        }
    }

    public void showScreenShot(byte[] bytes) {
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
            try {
                Image image = img.getScaledInstance(jPanel1.getWidth(), jPanel1.getHeight(), Image.SCALE_FAST);
                if (image == null) {
                    return;
                }
                // Hiển thị ảnh lên panel
                Graphics graphics = jPanel1.getGraphics();
                graphics.drawImage(image, 0, 0, jPanel1.getWidth(), jPanel1.getHeight(), jPanel1);
            } catch (Exception ex) {
            }
        } catch (Exception ex) {
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel1.setFocusable(false);
        jPanel1.setPreferredSize(new java.awt.Dimension(800, 600));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1105, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 494, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        isContinued = false;
    }//GEN-LAST:event_formWindowClosing

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables

}
