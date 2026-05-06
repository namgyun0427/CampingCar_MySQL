import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class SQLQueryFrame extends JFrame {
    private JTextArea sqlArea;
    private JTable resultTable;
    private JComboBox<String> exampleCombo;

    public SQLQueryFrame() {
        setTitle("SQL 질의 실행");
        setSize(1400, 900);
        setLocationRelativeTo(null);

        Color blueColor = new Color(51, 102, 153);
        Color darkGray = new Color(64, 64, 64);
        Color veryLightGray = new Color(245, 245, 245);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(veryLightGray);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBackground(veryLightGray);
        
        JLabel exampleLabel = new JLabel("테스팅용 복잡 질의:");
        exampleLabel.setForeground(darkGray);
        
        exampleCombo = new JComboBox<>(new String[]{
            "직접 입력",
            "질의 1: 고객별 캠핑카 이용 패턴과 정비 내역",
            "질의 2: 정비소별 수리 실적과 평균 비용 분석",
            "질의 3: 직원별 정비 실적과 부품 사용량"
        });
        
        JButton loadExampleBtn = new JButton("예시 로드");
        loadExampleBtn.setBackground(blueColor);
        loadExampleBtn.setForeground(Color.WHITE);
        loadExampleBtn.setFocusPainted(false);
        
        topPanel.add(exampleLabel);
        topPanel.add(exampleCombo);
        topPanel.add(loadExampleBtn);

        sqlArea = new JTextArea(10, 100);
        sqlArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        sqlArea.setBackground(Color.WHITE);
        sqlArea.setLineWrap(true);
        sqlArea.setWrapStyleWord(true);
        
        JScrollPane sqlScroll = new JScrollPane(sqlArea);
        sqlScroll.setBorder(BorderFactory.createTitledBorder("SQL 쿼리 입력"));
        sqlScroll.setPreferredSize(new Dimension(1350, 250));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(veryLightGray);
        
        JButton executeButton = new JButton("쿼리 실행");
        JButton clearButton = new JButton("내용 지우기");
        
        executeButton.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        executeButton.setBackground(blueColor);
        executeButton.setForeground(Color.WHITE);
        executeButton.setPreferredSize(new Dimension(120, 35));
        executeButton.setFocusPainted(false);
        
        clearButton.setBackground(Color.GRAY);
        clearButton.setForeground(Color.WHITE);
        clearButton.setPreferredSize(new Dimension(120, 35));
        clearButton.setFocusPainted(false);
        
        buttonPanel.add(executeButton);
        buttonPanel.add(clearButton);

        resultTable = new JTable();
        resultTable.setBackground(Color.WHITE);
        resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane resultScroll = new JScrollPane(resultTable);
        resultScroll.setBorder(BorderFactory.createTitledBorder("실행 결과"));
        resultScroll.setPreferredSize(new Dimension(1350, 400));

        loadExampleBtn.addActionListener(e -> loadExampleQuery());
        executeButton.addActionListener(e -> executeQuery());
        clearButton.addActionListener(e -> {
            sqlArea.setText("");
            resultTable.setModel(new DefaultTableModel());
        });

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(sqlScroll, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.NORTH);
        add(resultScroll, BorderLayout.CENTER);

        setVisible(true);
    }

    private void loadExampleQuery() {
        String selected = (String) exampleCombo.getSelectedItem();
        if (selected == null || selected.equals("직접 입력")) return;

        String query = "";
        switch (selected) {
            case "질의 1: 고객별 캠핑카 이용 패턴과 정비 내역":
                query = """
                SELECT 
                    cust.name AS '고객명',
                    cust.preferred_type AS '선호_캠핑카유형',
                    COUNT(DISTINCT r.rental_id) AS '총_대여횟수',
                    AVG(r.duration_days) AS '평균_대여기간',
                    COUNT(DISTINCT ir.repair_id) AS '내부정비횟수',
                    SUM(p.unit_price) AS '총_부품비용',
                    emp.name AS '담당_정비직원'
                FROM Customer cust
                JOIN Rental r ON cust.license_number = r.license_number
                JOIN Camper camp ON r.camper_id = camp.camper_id
                LEFT JOIN InternalRepair ir ON camp.camper_id = ir.camper_id
                LEFT JOIN Part p ON ir.part_id = p.part_id
                LEFT JOIN Employee emp ON ir.employee_id = emp.employee_id
                WHERE r.start_date >= (
                    SELECT DATE_SUB(MAX(start_date), INTERVAL 6 MONTH)
                    FROM Rental
                )
                GROUP BY cust.customer_id, cust.name, cust.preferred_type, emp.name
                HAVING COUNT(DISTINCT r.rental_id) >= 1
                ORDER BY 총_대여횟수 DESC, 총_부품비용 DESC
                """;
                break;
                
            case "질의 2: 정비소별 수리 실적과 평균 비용 분석":
                query = """
                SELECT 
                    ers.name AS '정비소명',
                    ers.contact_name AS '담당자',
                    COUNT(er.external_repair_id) AS '총_수리건수',
                    AVG(er.cost) AS '평균_수리비용',
                    c.name AS '주요_고객회사',
                    COUNT(DISTINCT camp.camper_id) AS '수리한_캠핑카수',
                    SUM(r.base_charge) AS '관련_대여수익'
                FROM ExternalRepairShop ers
                JOIN ExternalRepair er ON ers.repair_shop_id = er.repair_shop_id
                JOIN Camper camp ON er.camper_id = camp.camper_id
                JOIN Company c ON camp.company_id = c.company_id
                JOIN Rental r ON camp.camper_id = r.camper_id AND er.license_number = r.license_number
                WHERE er.repair_date >= (
                    SELECT DATE_SUB(MAX(repair_date), INTERVAL 1 YEAR)
                    FROM ExternalRepair
                    WHERE status = 'Completed'
                )
                GROUP BY ers.repair_shop_id, ers.name, ers.contact_name, c.company_id, c.name
                HAVING 총_수리건수 >= 1 AND 평균_수리비용 > 50000
                ORDER BY 총_수리건수 DESC, 평균_수리비용 DESC
                """;
                break;
                
            case "질의 3: 직원별 정비 실적과 부품 사용량":
                query = """
                SELECT 
                    emp.name AS '직원명',
                    emp.department AS '부서',
                    c.name AS '소속회사',
                    COUNT(ir.repair_id) AS '총_정비건수',
                    AVG(ir.repair_duration_minutes) AS '평균_정비시간',
                    COUNT(DISTINCT p.part_id) AS '사용한_부품종류',
                    SUM(p.unit_price) AS '총_부품비용',
                    COUNT(DISTINCT camp.camper_id) AS '정비한_캠핑카수'
                FROM Employee emp
                JOIN Company c ON emp.company_id = c.company_id
                JOIN InternalRepair ir ON emp.employee_id = ir.employee_id
                JOIN Part p ON ir.part_id = p.part_id
                JOIN Camper camp ON ir.camper_id = camp.camper_id
                WHERE emp.role = '정비' 
                AND ir.repair_date >= (
                    SELECT DATE_SUB(MAX(repair_date), INTERVAL 6 MONTH)
                    FROM InternalRepair
                )
                GROUP BY emp.employee_id, emp.name, emp.department, c.name
                HAVING 총_정비건수 >= 1
                ORDER BY 총_정비건수 DESC, 총_부품비용 DESC
                """;
                break;
        }
        
        sqlArea.setText(query);
    }

    private void executeQuery() {
        String sql = sqlArea.getText().trim();
        if (sql.isEmpty()) {
            JOptionPane.showMessageDialog(this, "SQL 쿼리를 입력하세요.");
            return;
        }

        try (Connection conn = DBConnection.getAdminConnection();
             Statement stmt = conn.createStatement()) {
            
            // SELECT 쿼리만 허용
            if (!sql.toUpperCase().trim().startsWith("SELECT")) {
                JOptionPane.showMessageDialog(this, 
                    "SELECT 쿼리만 실행 가능합니다.",
                    "제한된 쿼리", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            long startTime = System.currentTimeMillis();
            ResultSet rs = stmt.executeQuery(sql);
            long endTime = System.currentTimeMillis();
            
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
            
            resultTable.setModel(tableModel);
            
            for (int i = 0; i < resultTable.getColumnCount(); i++) {
                resultTable.getColumnModel().getColumn(i).setPreferredWidth(150);
            }
            
            int rowCount = resultTable.getRowCount();
            long executionTime = endTime - startTime;
            
            JOptionPane.showMessageDialog(this, 
                String.format("쿼리 실행 완료!\n실행 시간: %d ms\n결과 행 수: %d", 
                    executionTime, rowCount),
                "실행 결과", JOptionPane.INFORMATION_MESSAGE);
                
        } catch (SQLException ex) {
            String errorMsg = ex.getMessage();
            String userFriendlyMsg = "";
            
            if (errorMsg.contains("Unknown column")) {
                userFriendlyMsg = "존재하지 않는 컬럼명입니다.";
            } else if (errorMsg.contains("Unknown table")) {
                userFriendlyMsg = "존재하지 않는 테이블명입니다.";
            } else if (errorMsg.contains("syntax error")) {
                userFriendlyMsg = "SQL 문법 오류입니다.";
            } else if (errorMsg.contains("ambiguous")) {
                userFriendlyMsg = "모호한 컬럼명입니다. 테이블 별칭을 사용하세요.";
            } else {
                userFriendlyMsg = "SQL 실행 오류입니다.";
            }
            
            JOptionPane.showMessageDialog(this, 
                userFriendlyMsg + "\n\n상세 오류:\n" + errorMsg,
                "쿼리 실행 오류", JOptionPane.ERROR_MESSAGE);
        }
    }
}
