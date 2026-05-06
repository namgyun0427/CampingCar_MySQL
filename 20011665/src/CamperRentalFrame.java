import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Date;

public class CamperRentalFrame extends JFrame {
    private JTable camperTable;
    private JTextField searchField;
    private JComboBox<String> filterCombo;
    private JSpinner startDateSpinner;
    private JSpinner durationSpinner;
    private String username;

    public CamperRentalFrame(String username) {
        this.username = username;
        setTitle("캠핑카 검색 및 대여 - " + username);
        setSize(1200, 700);
        setLocationRelativeTo(null);

        Color blueColor = new Color(51, 102, 153);
        Color darkGray = new Color(64, 64, 64);
        Color lightGray = new Color(192, 192, 192);
        Color veryLightGray = new Color(245, 245, 245);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(veryLightGray);

        // 상단 검색 패널
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBackground(veryLightGray);
        
        searchField = new JTextField(20);
        filterCombo = new JComboBox<>(new String[]{"전체", "차량명", "승차인원", "대여비용"});
        JButton searchButton = new JButton("검색");
        searchButton.setBackground(blueColor);
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);
        searchButton.setPreferredSize(new Dimension(80, 30));

        JLabel searchLabel = new JLabel("검색:");
        searchLabel.setForeground(darkGray);

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(filterCombo);
        searchPanel.add(searchButton);

        // 캠핑카 테이블
        camperTable = new JTable();
        camperTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        camperTable.setBackground(Color.WHITE);
        camperTable.setGridColor(lightGray);
        camperTable.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(camperTable);
        scrollPane.setPreferredSize(new Dimension(1150, 400));
        scrollPane.setBorder(BorderFactory.createTitledBorder("대여 가능한 캠핑카"));

        // 하단 대여 패널
        JPanel rentalPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        rentalPanel.setBorder(BorderFactory.createTitledBorder("대여 신청"));
        rentalPanel.setBackground(Color.WHITE);
        rentalPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("대여 신청"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        startDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd");
        startDateSpinner.setEditor(dateEditor);
        
        durationSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 30, 1));
        
        JButton rentButton = new JButton("대여 신청");
        rentButton.setBackground(blueColor);
        rentButton.setForeground(Color.WHITE);
        rentButton.setFocusPainted(false);
        rentButton.setPreferredSize(new Dimension(100, 35));

        JLabel startLabel = new JLabel("대여 시작일:");
        JLabel durationLabel = new JLabel("대여 기간(일):");
        startLabel.setForeground(darkGray);
        durationLabel.setForeground(darkGray);

        rentalPanel.add(startLabel);
        rentalPanel.add(startDateSpinner);
        rentalPanel.add(durationLabel);
        rentalPanel.add(durationSpinner);
        rentalPanel.add(new JLabel());
        rentalPanel.add(rentButton);

        searchButton.addActionListener(e -> searchCampers());
        rentButton.addActionListener(e -> processRental());

        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(rentalPanel, BorderLayout.SOUTH);

        add(mainPanel);
        loadCampers();
        setVisible(true);
    }

    private void loadCampers() {
        try (Connection conn = DBConnection.getUserConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT camper_id, name, plate_number, capacity, rental_price, status FROM Camper WHERE status = 'Available'")) {
            
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
            
            camperTable.setModel(tableModel);
            
            for (int i = 0; i < camperTable.getColumnCount(); i++) {
                camperTable.getColumnModel().getColumn(i).setPreferredWidth(150);
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "데이터 조회 실패: " + ex.getMessage());
        }
    }

    private void searchCampers() {
        String searchText = searchField.getText().trim();
        String filter = (String) filterCombo.getSelectedItem();

        StringBuilder sql = new StringBuilder(
            "SELECT camper_id, name, plate_number, capacity, rental_price, status FROM Camper WHERE status = 'Available'");
        
        try (Connection conn = DBConnection.getUserConnection()) {
            if (!searchText.isEmpty() && !filter.equals("전체")) {
                switch (filter) {
                    case "차량명":
                        sql.append(" AND name LIKE ?");
                        break;
                    case "승차인원":
                        sql.append(" AND capacity = ?");
                        break;
                    case "대여비용":
                        sql.append(" AND rental_price <= ?");
                        break;
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
                if (!searchText.isEmpty() && !filter.equals("전체")) {
                    switch (filter) {
                        case "차량명":
                            pstmt.setString(1, "%" + searchText + "%");
                            break;
                        case "승차인원":
                            pstmt.setInt(1, Integer.parseInt(searchText));
                            break;
                        case "대여비용":
                            pstmt.setBigDecimal(1, new BigDecimal(searchText));
                            break;
                    }
                }

                try (ResultSet rs = pstmt.executeQuery()) {
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
                    camperTable.setModel(tableModel);
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "숫자를 올바르게 입력해주세요.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "검색 실패: " + ex.getMessage());
        }
    }

    private void processRental() {
        int selectedRow = camperTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "캠핑카를 선택하세요.");
            return;
        }

        int camperId = (int) camperTable.getValueAt(selectedRow, 0);
        String camperName = (String) camperTable.getValueAt(selectedRow, 1);

        Object priceObj = camperTable.getValueAt(selectedRow, 4);
        double rentalPrice;
        if (priceObj instanceof BigDecimal) {
            rentalPrice = ((BigDecimal) priceObj).doubleValue();
        } else if (priceObj instanceof Double) {
            rentalPrice = (Double) priceObj;
        } else {
            rentalPrice = Double.parseDouble(priceObj.toString());
        }
        
        Date startDate = (Date) startDateSpinner.getValue();
        int duration = (Integer) durationSpinner.getValue();

        double totalCost = rentalPrice * duration;

        int result = JOptionPane.showConfirmDialog(this,
            String.format("캠핑카: %s\n대여 기간: %d일\n총 비용: %.0f원\n\n대여하시겠습니까?", 
                camperName, duration, totalCost),
            "대여 확인", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getUserConnection()) {
                conn.setAutoCommit(false); // 트랜잭션 시작
                
                try {
                    // 1. 대여 정보 삽입
                    String insertRentalSQL = """
                        INSERT INTO Rental (camper_id, license_number, company_id, start_date, duration_days, base_charge, payment_due_date, status) 
                        VALUES (?, (SELECT license_number FROM Customer WHERE username = ?), 
                               (SELECT company_id FROM Camper WHERE camper_id = ?), ?, ?, ?, 
                               DATE_ADD(?, INTERVAL ? DAY), 'Reserved')
                        """;
                    
                    try (PreparedStatement pstmt1 = conn.prepareStatement(insertRentalSQL)) {
                        pstmt1.setInt(1, camperId);
                        pstmt1.setString(2, username);
                        pstmt1.setInt(3, camperId);
                        pstmt1.setDate(4, new java.sql.Date(startDate.getTime()));
                        pstmt1.setInt(5, duration);
                        pstmt1.setBigDecimal(6, BigDecimal.valueOf(totalCost));
                        pstmt1.setDate(7, new java.sql.Date(startDate.getTime()));
                        pstmt1.setInt(8, duration);
                        
                        int rowsInserted = pstmt1.executeUpdate();
                        if (rowsInserted == 0) {
                            throw new SQLException("대여 정보 삽입에 실패했습니다.");
                        }
                    }
                    
                    // 2. 캠핑카 상태를 'Rented'로 변경
                    String updateCamperSQL = "UPDATE Camper SET status = 'Rented' WHERE camper_id = ?";
                    try (PreparedStatement pstmt2 = conn.prepareStatement(updateCamperSQL)) {
                        pstmt2.setInt(1, camperId);
                        int rowsUpdated = pstmt2.executeUpdate();
                        if (rowsUpdated == 0) {
                            throw new SQLException("캠핑카 상태 업데이트에 실패했습니다.");
                        }
                    }
                    
                    // 3. 고객의 최근 대여일 업데이트
                    String updateCustomerSQL = "UPDATE Customer SET recent_rental = ? WHERE username = ?";
                    try (PreparedStatement pstmt3 = conn.prepareStatement(updateCustomerSQL)) {
                        pstmt3.setDate(1, new java.sql.Date(startDate.getTime()));
                        pstmt3.setString(2, username);
                        pstmt3.executeUpdate();
                    }
                    
                    conn.commit(); // 모든 작업 완료 시 커밋
                    
                    JOptionPane.showMessageDialog(this, 
                        "대여 신청이 완료되었습니다!\n\n" +
                        "캠핑카: " + camperName + "\n" +
                        "대여 기간: " + duration + "일\n" +
                        "총 비용: " + String.format("%,.0f원", totalCost),
                        "대여 완료", JOptionPane.INFORMATION_MESSAGE);
                    
                    loadCampers(); // 대여 후 캠핑카 목록 새로고침
                    
                } catch (SQLException ex) {
                    conn.rollback(); // 오류 발생 시 롤백
                    throw ex;
                }
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, 
                    "대여 실패: " + ex.getMessage(), 
                    "오류", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace(); // 디버깅용
            }
        }
    }
}
