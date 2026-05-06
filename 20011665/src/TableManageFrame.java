import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class TableManageFrame extends JFrame {
    private JComboBox<String> tableCombo;
    private JTable dataTable;
    private JTextField operationField;

    public TableManageFrame() {
        setTitle("테이블 관리");
        setSize(1400, 800);
        setLocationRelativeTo(null);

        Color blueColor = new Color(51, 102, 153);
        Color darkGray = new Color(64, 64, 64);
        Color lightGray = new Color(192, 192, 192);
        Color veryLightGray = new Color(245, 245, 245);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBackground(veryLightGray);
        
        tableCombo = new JComboBox<>(new String[]{
            "Company", "Customer", "Employee", "Camper", "Rental", 
            "Part", "ExternalRepairShop", "InternalRepair", "ExternalRepair"
        });
        
        operationField = new JTextField(50);
        
        JLabel tableLabel = new JLabel("테이블 선택:");
        JLabel operationLabel = new JLabel("작업 내용:");
        tableLabel.setForeground(darkGray);
        operationLabel.setForeground(darkGray);
        
        topPanel.add(tableLabel);
        topPanel.add(tableCombo);
        topPanel.add(operationLabel);
        topPanel.add(operationField);


        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(veryLightGray);
        
        JButton refreshBtn = new JButton("새로고침");
        JButton insertBtn = new JButton("데이터 입력");
        JButton updateBtn = new JButton("데이터 변경");
        JButton deleteBtn = new JButton("데이터 삭제");
        
        refreshBtn.setBackground(blueColor);
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setPreferredSize(new Dimension(120, 35));
        refreshBtn.setFocusPainted(false);
        
        insertBtn.setBackground(blueColor);
        insertBtn.setForeground(Color.WHITE);
        insertBtn.setPreferredSize(new Dimension(120, 35));
        insertBtn.setFocusPainted(false);
        
        updateBtn.setBackground(darkGray);
        updateBtn.setForeground(Color.WHITE);
        updateBtn.setPreferredSize(new Dimension(120, 35));
        updateBtn.setFocusPainted(false);
        
        deleteBtn.setBackground(Color.GRAY);
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setPreferredSize(new Dimension(120, 35));
        deleteBtn.setFocusPainted(false);

        btnPanel.add(refreshBtn);
        btnPanel.add(insertBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);

        dataTable = new JTable();
        dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        dataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dataTable.setBackground(Color.WHITE);
        dataTable.setGridColor(lightGray);
        
        JScrollPane scrollPane = new JScrollPane(dataTable);
        scrollPane.setPreferredSize(new Dimension(1380, 550));
        scrollPane.setBorder(BorderFactory.createTitledBorder("테이블 데이터"));

        JPanel topCombinedPanel = new JPanel(new BorderLayout());
        topCombinedPanel.add(topPanel, BorderLayout.NORTH);

        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(veryLightGray);
        
        add(topCombinedPanel, BorderLayout.NORTH);
        add(btnPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);

        tableCombo.addActionListener(e -> loadTableData());
        refreshBtn.addActionListener(e -> loadTableData());
        insertBtn.addActionListener(e -> openInsertDialog());
        updateBtn.addActionListener(e -> updateRecord());
        deleteBtn.addActionListener(e -> deleteRecord());

        loadTableData();
        setVisible(true);
    }

    private void loadTableData() {
        String tableName = (String) tableCombo.getSelectedItem();
        if (tableName == null) return;

        try (Connection conn = DBConnection.getAdminConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName)) {
            
            ResultSetToTableModel model = new ResultSetToTableModel(rs);
            dataTable.setModel(new DefaultTableModel(
                model.getData(),
                model.getColumnNames()
            ));
            
            for (int i = 0; i < dataTable.getColumnCount(); i++) {
                dataTable.getColumnModel().getColumn(i).setPreferredWidth(120);
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "데이터 조회 실패: " + ex.getMessage());
        }
    }

    private void openInsertDialog() {
        String tableName = (String) tableCombo.getSelectedItem();
        if (tableName == null) return;

        InsertDialog dialog = new InsertDialog(this, tableName);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            loadTableData();
        }
    }

    private void updateRecord() {
        String tableName = (String) tableCombo.getSelectedItem();
        String operation = operationField.getText().trim();

        if (operation.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "변경 작업을 입력하세요\n\n" +
                "형식: 조건 SET 변경내용\n");
            return;
        }

        if (!operation.toUpperCase().contains(" SET ")) {
            JOptionPane.showMessageDialog(this, 
                "변경 작업에는 SET 키워드가 필요합니다\n\n" +
                "형식: 조건 SET 변경내용\n");
            return;
        }

        String[] parts = operation.split("(?i)\\s+SET\\s+", 2);
        if (parts.length != 2) {
            JOptionPane.showMessageDialog(this, "올바른 형식으로 입력하세요");
            return;
        }

        String condition = parts[0].trim();
        String setClause = parts[1].trim();

        int result = JOptionPane.showConfirmDialog(this, 
            "다음 조건에 해당하는 데이터를 변경하시겠습니까?\n\n" +
            "테이블: " + tableName + "\n" +
            "조건: " + condition + "\n" +
            "변경 내용: " + setClause, 
            "데이터 변경 확인", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getAdminConnection()) {
                conn.setAutoCommit(false);
                
                try (Statement stmt = conn.createStatement()) {
                    String sql = "UPDATE " + tableName + " SET " + setClause + " WHERE " + condition;
                    int count = stmt.executeUpdate(sql);
                    
                    conn.commit();
                    
                    if (count > 0) {
                        JOptionPane.showMessageDialog(this, count + "건 변경 완료");
                        operationField.setText("");
                    } else {
                        JOptionPane.showMessageDialog(this, "조건에 해당하는 데이터가 없습니다.");
                    }
                    
                    loadTableData();
                    
                } catch (SQLException ex) {
                    conn.rollback();
                    throw ex;
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "변경 실패: " + ex.getMessage());
            }
        }
    }

    private void deleteRecord() {
        String tableName = (String) tableCombo.getSelectedItem();
        String condition = operationField.getText().trim();

        if (condition.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "삭제 조건을 입력하세요\n\n" +
                "예시: customer_id='CUST0001' 또는 company_id=1");
            return;
        }

        // SET 키워드가 포함되어 있으면 삭제 작업이 아님을 안내
        if (condition.toUpperCase().contains(" SET ")) {
            JOptionPane.showMessageDialog(this, 
                "삭제 작업에는 조건만 입력하세요");
            return;
        }

        int result = JOptionPane.showConfirmDialog(this, 
            "다음 조건에 해당하는 데이터를 삭제하시겠습니까?\n\n" +
            "테이블: " + tableName + "\n" +
            "조건: " + condition + "\n\n" +
            "삭제된 데이터는 복구할 수 없습니다!", 
            "데이터 삭제 확인", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getAdminConnection()) {
                conn.setAutoCommit(false);
                
                try (Statement stmt = conn.createStatement()) {
                    String sql = "DELETE FROM " + tableName + " WHERE " + condition;
                    int count = stmt.executeUpdate(sql);
                    
                    conn.commit();
                    
                    if (count > 0) {
                        JOptionPane.showMessageDialog(this, count + "건 삭제 완료");
                        operationField.setText("");
                    } else {
                        JOptionPane.showMessageDialog(this, "조건에 해당하는 데이터가 없습니다.");
                    }
                    
                    loadTableData();
                    
                } catch (SQLException ex) {
                    conn.rollback();
                    throw ex;
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "삭제 실패: " + ex.getMessage());
            }
        }
    }
}
