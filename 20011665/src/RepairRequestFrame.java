import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class RepairRequestFrame extends JFrame {
    private JComboBox<String> camperCombo;
    private JComboBox<String> shopCombo;
    private JTextArea detailsArea;
    private String username;

    public RepairRequestFrame(String username) {
        this.username = username;
        setTitle("정비 신청");
        setSize(500, 400);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        camperCombo = new JComboBox<>();
        shopCombo = new JComboBox<>();
        
        loadUserCampers();
        loadRepairShops();

        topPanel.add(new JLabel("대여한 캠핑카:"));
        topPanel.add(camperCombo);
        topPanel.add(new JLabel("정비소:"));
        topPanel.add(shopCombo);

        detailsArea = new JTextArea(10, 30);
        detailsArea.setBorder(BorderFactory.createTitledBorder("정비 내역"));

        JButton submitButton = new JButton("신청");
        submitButton.setBackground(new Color(51, 102, 153));
        submitButton.setForeground(Color.WHITE);
        submitButton.addActionListener(e -> submitRequest());

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(detailsArea), BorderLayout.CENTER);
        mainPanel.add(submitButton, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    private void loadUserCampers() {
        try (Connection conn = DBConnection.getUserConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT DISTINCT c.camper_id, c.name FROM Rental r " +
                 "JOIN Camper c ON r.camper_id = c.camper_id " +
                 "WHERE r.license_number = (SELECT license_number FROM Customer WHERE username = ?)")) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                camperCombo.addItem(rs.getString("name") + " (" + rs.getInt("camper_id") + ")");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "캠핑카 조회 실패: " + ex.getMessage());
        }
    }

    private void loadRepairShops() {
        try (Connection conn = DBConnection.getUserConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT repair_shop_id, name FROM ExternalRepairShop")) {
            
            while (rs.next()) {
                shopCombo.addItem(rs.getString("name") + " (" + rs.getInt("repair_shop_id") + ")");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "정비소 조회 실패: " + ex.getMessage());
        }
    }

    private void submitRequest() {
        if (camperCombo.getSelectedItem() == null || shopCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "캠핑카와 정비소를 선택하세요.");
            return;
        }

        String details = detailsArea.getText().trim();
        if (details.isEmpty()) {
            JOptionPane.showMessageDialog(this, "정비 내역을 입력하세요.");
            return;
        }

        try {
            String camperSelected = (String) camperCombo.getSelectedItem();
            String shopSelected = (String) shopCombo.getSelectedItem();
            
            int camperId = Integer.parseInt(camperSelected.split("\\(")[1].replace(")", ""));
            int shopId = Integer.parseInt(shopSelected.split("\\(")[1].replace(")", ""));

            try (Connection conn = DBConnection.getUserConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO ExternalRepair (camper_id, repair_shop_id, company_id, license_number, repair_details, repair_date, payment_due_date) " +
                     "VALUES (?, ?, 1, (SELECT license_number FROM Customer WHERE username = ?), ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY))")) {
                
                pstmt.setInt(1, camperId);
                pstmt.setInt(2, shopId);
                pstmt.setString(3, username);
                pstmt.setString(4, details);
                pstmt.executeUpdate();
                
                JOptionPane.showMessageDialog(this, "정비 신청이 완료되었습니다!");
                dispose();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "신청 실패: " + ex.getMessage());
        }
    }
}
