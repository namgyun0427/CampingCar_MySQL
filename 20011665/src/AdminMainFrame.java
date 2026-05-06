import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AdminMainFrame extends JFrame {
    public AdminMainFrame() {
        setTitle("관리자 메인 화면");
        setSize(600, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(6, 1, 15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        JLabel titleLabel = new JLabel("관리자 메인 메뉴", JLabel.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));

        JButton btnInitDB = new JButton("데이터베이스 초기화");
        JButton btnManage = new JButton("테이블 관리");
        JButton btnSQL = new JButton("SQL 질의 실행");
        JButton btnRepair = new JButton("정비 내역 조회");
        JButton btnLogout = new JButton("로그아웃");

        Font btnFont = new Font("맑은 고딕", Font.BOLD, 16);
        Color btnColor = new Color(51, 102, 153);
        
        JButton[] buttons = {btnInitDB, btnManage, btnSQL, btnRepair, btnLogout};
        for (JButton btn : buttons) {
            btn.setFont(btnFont);
            btn.setBackground(btnColor);
            btn.setForeground(Color.WHITE);
            btn.setPreferredSize(new Dimension(200, 50));
        }

        btnInitDB.addActionListener(e -> initializeDatabase());
        btnManage.addActionListener(e -> new TableManageFrame().setVisible(true));
        btnSQL.addActionListener(e -> new SQLQueryFrame().setVisible(true));
        btnRepair.addActionListener(e -> new RepairHistoryFrame().setVisible(true));
        btnLogout.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        panel.add(titleLabel);
        panel.add(btnInitDB);
        panel.add(btnManage);
        panel.add(btnSQL);
        panel.add(btnRepair);
        panel.add(btnLogout);

        add(panel);
    }

    private void initializeDatabase() {
        int result = JOptionPane.showConfirmDialog(this, 
            "데이터베이스를 초기 상태로 복원하시겠습니까?\n" +
            "모든 변경사항이 사라지고 원래 샘플 데이터로 되돌아갑니다.", 
            "데이터베이스 초기화", JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getAdminConnection()) {
                conn.setAutoCommit(false);
                
                try (Statement stmt = conn.createStatement()) {
                    // 1. 외래키 제약조건 비활성화 (execute 사용)
                    stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
                    
                    // 2. 기존 데이터 삭제 (executeUpdate 사용)
                    String[] deleteQueries = {
                        "DELETE FROM ExternalRepair",
                        "DELETE FROM InternalRepair", 
                        "DELETE FROM Rental",
                        "DELETE FROM Part",
                        "DELETE FROM ExternalRepairShop",
                        "DELETE FROM Camper",
                        "DELETE FROM Employee",
                        "DELETE FROM Customer",
                        "DELETE FROM Company"
                    };
                    
                    for (String sql : deleteQueries) {
                        stmt.executeUpdate(sql);
                    }
                    
                    // 3. 초기 데이터 삽입
                    DatabaseInitializer.insertAllInitialData(conn);
                    
                    // 4. 외래키 제약조건 활성화 (execute 사용)
                    stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
                }
                
                conn.commit();
                
                JOptionPane.showMessageDialog(this, "데이터베이스 초기화가 완료되었습니다!");
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, 
                    "초기화 실패: " + ex.getMessage(), 
                    "오류", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace(); // 디버깅용
            }
        }
    }
}
