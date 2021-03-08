package Packages;

import Utils.Utils;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

public class ComputerTableModel extends AbstractTableModel {
    String columnNames[] = {"STT", "Tên client", "Địa chỉ ip", "Cổng", "Tình trạng"};
    private List<Socket> list;
    private List<Computer> listComputer;

    private Computer createComputer(Socket s) {
        Computer com = new Computer();
        com.setName(s.getInetAddress().getHostName());
        com.setIp(s.getInetAddress().getHostAddress());
        com.setPort(s.getPort());
        com.setState("Đang hoạt động");
        return com;
    }

    public ComputerTableModel(List<Socket> list) {
        this.list = list;
        this.listComputer = new ArrayList();
        for (Socket s : list) {
            listComputer.add(createComputer(s));
        }
    }
    
    public void updateState(Socket s, String state) {
        for(int i = 0; i < list.size(); i++) {
            if(list.get(i).equals(s)) {
                listComputer.get(i).setState(state);
                break;
            }
        }
    }

    public Socket getItem(int rowIndex) {
        return this.list.get(rowIndex);
    }

    public void updateAllElement() {
        int i = 0;
        try {
            for (Socket s : list) {
                if (Utils.isDisconnected(s)) {
                    list.remove(i);
                    listComputer.remove(i);
                    fireTableRowsDeleted(i, i);
                } else {
                    i++;
                }
            }
        } catch (Exception e) {
        }
    }

    public void addElement(Socket e) {
        list.add(e);
        listComputer.add(createComputer(e));
        fireTableRowsInserted(list.size() - 1, list.size() - 1);
    }

    @Override
    public int getRowCount() {
        return list.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int index) {
        return columnNames[index];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        try {
            switch (columnIndex) {
                case 0:
                    return rowIndex + 1;
                case 1:
                    return listComputer.get(rowIndex).getName();
                case 2:
                    return listComputer.get(rowIndex).getIp();
                case 3:
                    return listComputer.get(rowIndex).getPort();
                case 4:
                    return listComputer.get(rowIndex).getState();
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
