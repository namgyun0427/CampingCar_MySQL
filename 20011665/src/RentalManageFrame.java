import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.Date;

public class RentalManageFrame extends JFrame {
    private JTable rentalTable;
    private String username;
    private JComboBox<String> cbNewCamper;
    private JSpinner spStartDate;
    private JSpinner spDuration;

    public RentalManageFrame(String username) {
        this.username = username;
        setTitle("대여 관리 - " + username);
        setSize(1000, 750);
        setLocationRelativeTo(null);

        // 색상 정의 - 기존과 동일한 무채색+파랑색 조합
        Color blueColor = new Color(51, 102, 153);
        Color darkGray = new Color(64, 64, 64);
        Color lightGray = new Color(192, 192, 192);
        Color veryLightGray = new Color(245, 245, 245);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(veryLightGray);

        JLabel titleLabel = new JLabel("내 대여 내역", JLabel.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        titleLabel.setForeground(darkGray);

        rentalTable = new JTable();
        rentalTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        rentalTable.setRowHeight(25);
        rentalTable.setBackground(Color.WHITE);
        rentalTable.setGridColor(lightGray);

        JScrollPane scrollPane = new JScrollPane(rentalTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("대여 목록"));
        scrollPane.setPreferredSize(new Dimension(950, 300));

        // 하단 변경 패널
        JPanel changePanel = new JPanel(new GridLayout(4, 2, 10, 10));
        changePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("대여 정보 변경"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        changePanel.setBackground(Color.WHITE);

        JLabel lblNewCamper = new JLabel("새 캠핑카 선택:");
        lblNewCamper.setForeground(darkGray);
        cbNewCamper = new JComboBox<>();

        JLabel lblStartDate = new JLabel("시작 날짜 변경:");
        lblStartDate.setForeground(darkGray);
        spStartDate = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spStartDate, "yyyy-MM-dd");
        spStartDate.setEditor(dateEditor);

        JLabel lblDuration = new JLabel("대여 기간(일) 변경:");
        lblDuration.setForeground(darkGray);
        spDuration = new JSpinner(new SpinnerNumberModel(1, 1, 30, 1));

        JButton btnUpdate = new JButton("변경 적용");
        btnUpdate.setBackground(blueColor);
        btnUpdate.setForeground(Color.WHITE);
        btnUpdate.setFocusPainted(false);
        btnUpdate.setPreferredSize(new Dimension(100, 35));

        changePanel.add(lblNewCamper);
        changePanel.add(cbNewCamper);
        changePanel.add(lblStartDate);
        changePanel.add(spStartDate);
        changePanel.add(lblDuration);
        changePanel.add(spDuration);
        changePanel.add(new JLabel());
        changePanel.add(btnUpdate);

        // 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(veryLightGray);
        
        JButton refreshButton = new JButton("새로고침");
        JButton cancelButton = new JButton("대여 취소");
        
        refreshButton.setBackground(blueColor);
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.setPreferredSize(new Dimension(100, 35));
        
        cancelButton.setBackground(Color.GRAY);
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.setPreferredSize(new Dimension(100, 35));

        buttonPanel.add(refreshButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(changePanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // 이벤트 리스너
        refreshButton.addActionListener(e -> {
            loadRentals();
            loadAvailableCampers();
        });
        
        cancelButton.addActionListener(e -> cancelRental());
        
        // 테이블 선택 시 변경 필드 업데이트
        rentalTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateChangeFields();
            }
        });

        btnUpdate.addActionListener(e -> updateRental());

        // 초기 데이터 로드
        loadRentals();
        loadAvailableCampers();
        
        setVisible(true);
    }

    private void loadRentals() {
        try (Connection conn = DBConnection.getUserConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 """
                 SELECT r.rental_id AS '대여ID', c.name AS '캠핑카명', r.start_date AS '시작일', 
                        r.duration_days AS '기간_일', r.base_charge AS '비용', r.status AS '상태' 
                 FROM Rental r 
                 JOIN Camper c ON r.camper_id = c.camper_id 
                 WHERE r.license_number = (SELECT license_number FROM Customer WHERE username = ?)
                 ORDER BY r.rental_id ASC
                 """)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
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
            
            rentalTable.setModel(tableModel);
            
            // 컬럼 너비 조정
            for (int i = 0; i < rentalTable.getColumnCount(); i++) {
                rentalTable.getColumnModel().getColumn(i).setPreferredWidth(120);
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "대여 내역 조회 실패: " + ex.getMessage());
        }
    }

    private void loadAvailableCampers() {
        cbNewCamper.removeAllItems();
        try (Connection conn = DBConnection.getUserConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT name FROM Camper WHERE status = 'Available' ORDER BY name")) {
            
            while (rs.next()) {
                cbNewCamper.addItem(rs.getString("name"));
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "캠핑카 목록 조회 실패: " + ex.getMessage());
        }
    }

    private void updateChangeFields() {
        int selectedRow = rentalTable.getSelectedRow();
        if (selectedRow == -1) return;

        try {
            // 시작일 설정
            Object startDateObj = rentalTable.getValueAt(selectedRow, 2);
            if (startDateObj instanceof Date) {
                spStartDate.setValue(startDateObj);
            } else if (startDateObj instanceof java.sql.Date) {
                spStartDate.setValue(new Date(((java.sql.Date) startDateObj).getTime()));
            } else {
                spStartDate.setValue(new Date());
            }

            // 기간 설정
            Object durationObj = rentalTable.getValueAt(selectedRow, 3);
            if (durationObj instanceof Integer) {
                spDuration.setValue(durationObj);
            } else {
                spDuration.setValue(1);
            }

            // 현재 캠핑카를 콤보박스에서 선택
            String currentCamperName = (String) rentalTable.getValueAt(selectedRow, 1);
            
            // 현재 대여 중인 캠핑카도 선택 가능하도록 임시로 추가
            boolean found = false;
            for (int i = 0; i < cbNewCamper.getItemCount(); i++) {
                if (cbNewCamper.getItemAt(i).equals(currentCamperName)) {
                    cbNewCamper.setSelectedIndex(i);
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                cbNewCamper.addItem(currentCamperName);
                cbNewCamper.setSelectedItem(currentCamperName);
            }
            
        } catch (Exception ex) {
            System.err.println("필드 업데이트 오류: " + ex.getMessage());
        }
    }

    private void updateRental() {
        int selectedRow = rentalTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "변경할 대여를 선택하세요.");
            return;
        }

        int rentalId = (int) rentalTable.getValueAt(selectedRow, 0);
        String currentStatus = (String) rentalTable.getValueAt(selectedRow, 5);
        
        // Reserved 상태만 변경 가능
        if (!"Reserved".equals(currentStatus)) {
            JOptionPane.showMessageDialog(this, "예약 상태인 대여만 변경할 수 있습니다.");
            return;
        }

        String selectedCamperName = (String) cbNewCamper.getSelectedItem();
        Date newStartDate = (Date) spStartDate.getValue();
        int newDuration = (Integer) spDuration.getValue();

        if (selectedCamperName == null) {
            JOptionPane.showMessageDialog(this, "새 캠핑카를 선택하세요.");
            return;
        }

        // 새 캠핑카 ID 조회
        int newCamperId = getCamperIdByName(selectedCamperName);
        if (newCamperId == -1) {
            JOptionPane.showMessageDialog(this, "선택한 캠핑카 정보를 찾을 수 없습니다.");
            return;
        }

        // 변경 확인
        int confirm = JOptionPane.showConfirmDialog(this, 
            String.format("대여 ID: %d\n새 캠핑카: %s\n시작 날짜: %tF\n대여 기간: %d일\n\n변경을 적용하시겠습니까?",
                rentalId, selectedCamperName, newStartDate, newDuration),
            "변경 확인", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // 데이터베이스 업데이트
        try (Connection conn = DBConnection.getUserConnection()) {
            conn.setAutoCommit(false);
            
            try {
                int oldCamperId = getCamperIdByRentalId(rentalId);
                
                String updateRentalSQL = """
                    UPDATE Rental 
                    SET camper_id = ?, start_date = ?, duration_days = ?, 
                        base_charge = (SELECT rental_price FROM Camper WHERE camper_id = ?) * ?
                    WHERE rental_id = ?
                    """;
                    
                try (PreparedStatement pstmt = conn.prepareStatement(updateRentalSQL)) {
                    pstmt.setInt(1, newCamperId);
                    pstmt.setDate(2, new java.sql.Date(newStartDate.getTime()));
                    pstmt.setInt(3, newDuration);
                    pstmt.setInt(4, newCamperId);
                    pstmt.setInt(5, newDuration);
                    pstmt.setInt(6, rentalId);
                    
                    int updatedRows = pstmt.executeUpdate();
                    if (updatedRows == 0) {
                        throw new SQLException("대여 정보 변경에 실패했습니다.");
                    }
                }
                
                if (oldCamperId != newCamperId) {
                    String updateOldCamperSQL = "UPDATE Camper SET status = 'Available' WHERE camper_id = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(updateOldCamperSQL)) {
                        pstmt.setInt(1, oldCamperId);
                        pstmt.executeUpdate();
                    }
                    
                    String updateNewCamperSQL = "UPDATE Camper SET status = 'Rented' WHERE camper_id = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(updateNewCamperSQL)) {
                        pstmt.setInt(1, newCamperId);
                        pstmt.executeUpdate();
                    }
                }
                
                conn.commit();
                
                JOptionPane.showMessageDialog(this, "대여 정보가 성공적으로 변경되었습니다!");
                loadRentals();
                loadAvailableCampers();
                
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "변경 실패: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void cancelRental() {
        int selectedRow = rentalTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "취소할 대여를 선택하세요.");
            return;
        }

        int rentalId = (int) rentalTable.getValueAt(selectedRow, 0);
        String status = (String) rentalTable.getValueAt(selectedRow, 5);

        if (!"Reserved".equals(status)) {
            JOptionPane.showMessageDialog(this, "예약 상태인 대여만 취소할 수 있습니다.");
            return;
        }

        int result = JOptionPane.showConfirmDialog(this, "정말 취소하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getUserConnection()) {
                conn.setAutoCommit(false);
                
                try {
                    int camperId = getCamperIdByRentalId(rentalId);
                    String updateCamperSQL = "UPDATE Camper SET status = 'Available' WHERE camper_id = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(updateCamperSQL)) {
                        pstmt.setInt(1, camperId);
                        pstmt.executeUpdate();
                    }
                    
                    String updateRentalSQL = "UPDATE Rental SET status = 'Cancelled' WHERE rental_id = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(updateRentalSQL)) {
                        pstmt.setInt(1, rentalId);
                        pstmt.executeUpdate();
                    }
                    
                    conn.commit();
                    
                    JOptionPane.showMessageDialog(this, "대여가 취소되었습니다.");
                    loadRentals();
                    loadAvailableCampers();
                    
                } catch (SQLException ex) {
                    conn.rollback();
                    throw ex;
                }
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "취소 실패: " + ex.getMessage());
            }
        }
    }

    private int getCamperIdByName(String name) {
        try (Connection conn = DBConnection.getUserConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT camper_id FROM Camper WHERE name = ?")) {
            
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("camper_id");
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "캠핑카 ID 조회 실패: " + ex.getMessage());
        }
        return -1;
    }

    private int getCamperIdByRentalId(int rentalId) {
        try (Connection conn = DBConnection.getUserConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT camper_id FROM Rental WHERE rental_id = ?")) {
            
            pstmt.setInt(1, rentalId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("camper_id");
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "대여 캠핑카 ID 조회 실패: " + ex.getMessage());
        }
        return -1;
    }
}
