import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class RepairHistoryFrame extends JFrame {
    private JComboBox<String> repairTypeCombo;
    private JTable repairTable;

    public RepairHistoryFrame() {
        setTitle("정비 내역 조회");
        setSize(1200, 700);
        setLocationRelativeTo(null);

        // 색상 정의
        Color blueColor = new Color(51, 102, 153);
        Color darkGray = new Color(64, 64, 64);
        Color veryLightGray = new Color(245, 245, 245);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBackground(veryLightGray);
        
        repairTypeCombo = new JComboBox<>(new String[]{"자체 정비", "외부 정비"});
        JButton searchButton = new JButton("조회");
        searchButton.setBackground(blueColor);
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);
        searchButton.setPreferredSize(new Dimension(80, 30));
        
        JLabel typeLabel = new JLabel("정비 유형:");
        typeLabel.setForeground(darkGray);
        
        topPanel.add(typeLabel);
        topPanel.add(repairTypeCombo);
        topPanel.add(searchButton);

        JLabel infoLabel = new JLabel("💡 부품명/정비소명을 더블클릭하면 상세 정보를 볼 수 있습니다.");
        infoLabel.setForeground(darkGray);
        infoLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBackground(veryLightGray);
        infoPanel.add(infoLabel);

        repairTable = new JTable();
        repairTable.setBackground(Color.WHITE);
        repairTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        repairTable.setRowHeight(25);
        
        // 테이블 더블클릭 이벤트
        repairTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleTableDoubleClick();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(repairTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("정비 내역"));

        add(topPanel, BorderLayout.NORTH);
        add(infoPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);

        searchButton.addActionListener(e -> loadRepairData());
        repairTypeCombo.addActionListener(e -> loadRepairData());

        loadRepairData();
        setVisible(true);
    }

    private void loadRepairData() {
        try (Connection conn = DBConnection.getAdminConnection()) {
            String sql;
            if (repairTypeCombo.getSelectedItem().equals("자체 정비")) {
                sql = """
                    SELECT 
                        ir.repair_id AS '정비ID',
                        camp.name AS '캠핑카명', 
                        ir.repair_date AS '정비일자',
                        part.name AS '부품명',
                        part.unit_price AS '부품단가',
                        emp.name AS '담당직원명',
                        emp.department AS '부서',
                        ir.repair_duration_minutes AS '정비시간_분'
                    FROM InternalRepair ir 
                    JOIN Camper camp ON ir.camper_id = camp.camper_id 
                    JOIN Part part ON ir.part_id = part.part_id 
                    JOIN Employee emp ON ir.employee_id = emp.employee_id
                    ORDER BY ir.repair_id ASC
                    """;
            } else {
                sql = """
                    SELECT 
                        er.external_repair_id AS '수리ID',
                        camp.name AS '캠핑카명', 
                        er.repair_date AS '정비일자',
                        shop.name AS '정비소명',
                        shop.contact_name AS '정비소담당자',
                        er.cost AS '수리비용',
                        er.repair_details AS '수리내역',
                        er.status AS '진행상태'
                    FROM ExternalRepair er 
                    JOIN Camper camp ON er.camper_id = camp.camper_id 
                    JOIN ExternalRepairShop shop ON er.repair_shop_id = shop.repair_shop_id
                    ORDER BY er.external_repair_id ASC
                    """;
            }
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            ResultSetToTableModel model = new ResultSetToTableModel(rs);
            DefaultTableModel tableModel = new DefaultTableModel(
                model.getData(),
                model.getColumnNames()
            ) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // 읽기 전용
                }
            };
            
            repairTable.setModel(tableModel);
            
            for (int i = 0; i < repairTable.getColumnCount(); i++) {
                repairTable.getColumnModel().getColumn(i).setPreferredWidth(120);
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "데이터 조회 실패: " + ex.getMessage());
        }
    }

    private void handleTableDoubleClick() {
        int selectedRow = repairTable.getSelectedRow();
        if (selectedRow == -1) return;

        if (repairTypeCombo.getSelectedItem().equals("자체 정비")) {
            // 부품명 컬럼 (인덱스 3)에서 부품 정보 조회
            String partName = (String) repairTable.getValueAt(selectedRow, 3);
            showPartDetails(partName);
        } else {
            // 정비소명 컬럼 (인덱스 3)에서 정비소 정보 조회
            String shopName = (String) repairTable.getValueAt(selectedRow, 3);
            showRepairShopDetails(shopName);
        }
    }

    private void showPartDetails(String partName) {
        try (Connection conn = DBConnection.getAdminConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 """
                 SELECT 
                     part_id, name, unit_price, quantity_in_stock, 
                     received_date, supplier_name
                 FROM Part 
                 WHERE name = ?
                 """)) {
            
            pstmt.setString(1, partName);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                StringBuilder details = new StringBuilder();
                details.append("📦 부품 상세 정보\n\n");
                details.append("부품 ID: ").append(rs.getInt("part_id")).append("\n");
                details.append("부품명: ").append(rs.getString("name")).append("\n");
                details.append("단가: ").append(String.format("%,.0f원", rs.getDouble("unit_price"))).append("\n");
                details.append("재고 수량: ").append(rs.getInt("quantity_in_stock")).append("개\n");
                details.append("입고일: ").append(rs.getDate("received_date")).append("\n");
                details.append("공급업체: ").append(rs.getString("supplier_name")).append("\n");
                
                JOptionPane.showMessageDialog(this, details.toString(), 
                    "부품 정보", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "부품 정보를 찾을 수 없습니다.");
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "부품 정보 조회 실패: " + ex.getMessage());
        }
    }

    private void showRepairShopDetails(String shopName) {
        try (Connection conn = DBConnection.getAdminConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 """
                 SELECT 
                     ers.repair_shop_id, ers.name, ers.address, ers.phone,
                     ers.contact_name, ers.contact_email
                 FROM ExternalRepairShop ers
                 WHERE ers.name = ?
                 """)) {
            
            pstmt.setString(1, shopName);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                StringBuilder details = new StringBuilder();
                details.append("🔧 정비소 상세 정보\n\n");
                details.append("정비소 ID: ").append(rs.getInt("repair_shop_id")).append("\n");
                details.append("정비소명: ").append(rs.getString("name")).append("\n");
                details.append("주소: ").append(rs.getString("address")).append("\n");
                details.append("전화번호: ").append(rs.getString("phone")).append("\n");
                details.append("담당자: ").append(rs.getString("contact_name")).append("\n");
                details.append("이메일: ").append(rs.getString("contact_email")).append("\n");

                JOptionPane.showMessageDialog(this, details.toString(), 
                    "정비소 정보", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "정비소 정보를 찾을 수 없습니다.");
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "정비소 정보 조회 실패: " + ex.getMessage());
        }
    }
}
