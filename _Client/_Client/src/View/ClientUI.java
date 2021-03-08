/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package View;

import Packages.ID;
import Packages.MyProcess;
import Packages.Packet;
import Packages.ScreenCapture;
import Utils.Utils;
import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Robot;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author Administrator
 */
public class ClientUI extends javax.swing.JFrame implements Runnable {

    private final boolean isContinued = true;
    private final String ipServer;
    private final Socket socketConnectServer;
    private Socket socketTracking;
    private Socket socketSendProcesses;
    private Socket socketReceiveFile;

    private ChatToServer chat;
    private ReceiveMessageFromServer rm;

    public ClientUI(Socket socketConnectServer, String ipServer) {
        initComponents();
        setVisible(true);
        setLocationRelativeTo(null);
        this.socketConnectServer = socketConnectServer;
        this.ipServer = ipServer;
        
        runThreadProcess();
    }
    
    // <editor-fold defaultstate="collapsed" desc="runThreadProcess">
    private void runThreadProcess() {
        new Thread(new Runnable() {
            public void run() {
                while (isContinued) {
                    try {
                        String msg = Utils.receiveMess(socketSendProcesses);
                        if (msg != null && !msg.isEmpty()) {
                            Packet pk = new Packet();
                            pk.analyzeMessage(msg);
                            if (pk.isId(ID.Kill)) {
                                procKillProcess(pk);
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }).start();
    }
    // </editor-fold>

    @Override
    public void run() {
        while (isContinued) {
            try {
                String msg = Utils.receiveMess(socketConnectServer);
                if (msg != null && !msg.isEmpty()) {
                    mainProc(msg);
                }
            } catch (Exception e) {
            }
        }
    }

    private void mainProc(String msg) throws Exception {
        Packet pk = new Packet();
        pk.analyzeMessage(msg);

        if (pk.isId(ID.Chat)) {
            procChat(pk);
        } else if (pk.isId(ID.Message)) {
            procNotify(pk);
        } else if (pk.isId(ID.Shell)) {
            procShell(pk);
        } else if (pk.isId(ID.FileTransfer)) {
            procReceiveFile(pk);
        } else if (pk.isId(ID.Tracking)) {
            procTrackingUser(pk);
        } else if (pk.isId(ID.Fullscreen)) {
            procLock();
        } else if (pk.isId(ID.OK)) {
            procOpen();
        } else if (pk.isId(ID.Process)) {
            procGetProcess(pk);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="procChat">
    private void procChat(Packet pk) {
        if (chat == null) {
            chat = new ChatToServer(socketConnectServer);
        }
        chat.addMess(pk.getMessage());
        if (!chat.isVisible()) {
            chat.setVisible(true);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="procNotify">
    private void procNotify(Packet pk) {
        if (rm == null) {
            rm = new ReceiveMessageFromServer(socketConnectServer);
        }
        rm.addMess(pk.getMessage());
        if (!rm.isVisible()) {
            rm.setVisible(true);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="procShell">
    private void procShell(Packet pk) {
        Packet pkShell = new Packet(ID.Shell);
        try {
            Process process = Runtime.getRuntime().exec("cmd /c " + pk.getMessage() + "\n");
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = input.readLine()) != null) {
                if (line.equals("")) {
                    continue;
                }
                pk.setMessage(line.trim());
                // wait 200ms
                Thread.sleep(200);
                Utils.sendMess(socketConnectServer, pk.toString());
            }
        } catch (Exception ex) {
            pk.setMessage("Error: " + ex.getMessage());
            Utils.sendMess(socketConnectServer, pkShell.toString());
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="procReceiveFile">
    private void procReceiveFile(Packet pk) throws IOException {
        int port = Integer.parseInt(pk.getMessage());
        socketReceiveFile = new Socket(ipServer, port);

        int bytesRead;
        DataInputStream clientData = new DataInputStream(socketReceiveFile.getInputStream());
        System.err.println("[Nhận File]: bắt đầu chờ nhận file....");
        String fileName = clientData.readUTF();
        System.err.println("[Nhận File]: đã thấy tên file: " + fileName);
        try {
            OutputStream output = new FileOutputStream(fileName);
            System.err.println("[Nhận File]: bắt đầu nhận file: " + fileName);
            long size = clientData.readLong();
            byte[] buffer = new byte[3024];
            while (size > 0 && (bytesRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                output.write(buffer, 0, bytesRead);
                size -= bytesRead;
            }
            output.close();
        } catch (Exception ex) {
        }
        System.err.println("[Nhận File]: đã nhận xong: " + fileName);
        JOptionPane.showMessageDialog(null, "Bạn đã nhận được file " + fileName + " từ server!");
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="procTrackingUser">   
    private void procTrackingUser(Packet pk) {
        int port = Integer.parseInt(pk.getMessage());
        // Dùng để xử lý màn hình
        Robot robot;

        try {
            // Tạo socket nhận remote với port đã gửi qua
            socketTracking = new Socket(ipServer, port);
            try {
                // Lấy màn hình mặc định của hệ thống
                GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
                GraphicsDevice gDev = gEnv.getDefaultScreenDevice();

                // Chuẩn bị robot thao tác màn hình
                robot = new Robot(gDev);

                // Gửi màn hình 
                sendScreenCapture(socketTracking, robot);
            } catch (AWTException | HeadlessException ex) {
            }

        } catch (IOException ex) {
        }
    }

    private void sendScreenCapture(Socket socket, Robot robot) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ObjectOutputStream oos = null;
                try {
                    //Chuẩn bị gửi cho server
                    oos = new ObjectOutputStream(socket.getOutputStream());
                    ScreenCapture sc = new ScreenCapture();
                    while (true) {
                        // Gửi hình đã chụp cho server
                        try {
                            oos.writeObject(sc.execute(robot));
                            oos.flush();

                            // Dừng 200ms để dữ liệu kịp chuyển đến 
                            Thread.sleep(200);
                        } catch (Exception ex) {
                            break;
                        }
                    }
                } catch (IOException ex) {
                }
            }

        }).start();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="procLock">
    private void procLock() {
        dispose();
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setAlwaysOnTop(true);
        setVisible(true);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="procOpen">
    private void procOpen() {
        dispose();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 300);
        setUndecorated(false);
        setAlwaysOnTop(false);
        setVisible(true);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="procGetProcess">  
    private void procGetProcess(Packet pk) throws IOException {
        int port = Integer.parseInt(pk.getMessage());
        socketSendProcesses = new Socket(ipServer, port);

        Vector<MyProcess> listProcess = new Vector<>();
        try {
            Process process = Runtime.getRuntime().exec("tasklist /fi \"STATUS eq RUNNING\" /nh /fo csv");
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line = "";
            while ((line = input.readLine()) != null) {
                MyProcess temp = new MyProcess();
                String[] substring = line.split("\",\"");
                String substr = substring[0].substring(1);

                temp.setImagename(substr);
                temp.setPid(substring[1]);

                listProcess.add(temp);
            }

            Utils.sendObject(socketSendProcesses, listProcess);
        } catch (Exception ex) {
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="procKillProcess">
    private void procKillProcess(Packet pk) {
        String pid = pk.getMessage();
        String cmd = "taskkill /PID " + pid + " /F";
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String res = input.readLine();
            Packet pkKill = new Packet(ID.Kill);
            pkKill.setMessage(res);
            Utils.sendMess(socketSendProcesses, pkKill.toString());
        } catch (IOException ex) {
        }
    }
    // </editor-fold>

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(new java.awt.Dimension(400, 300));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
