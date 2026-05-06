import java.sql.*;

public class CustomerDAO {
    public boolean checkLogin(String username, String password) throws SQLException {
        String sql = "SELECT customer_id FROM Customer WHERE username=? AND password=?";
        try (Connection conn = DBConnection.getUserConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Customer getCustomerByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM Customer WHERE username=?";
        try (Connection conn = DBConnection.getUserConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Customer customer = new Customer();
                    customer.setCustomerId(rs.getString("customer_id"));
                    customer.setUsername(rs.getString("username"));
                    customer.setName(rs.getString("name"));
                    customer.setLicenseNumber(rs.getString("license_number"));
                    customer.setPhone(rs.getString("phone"));
                    customer.setEmail(rs.getString("email"));
                    return customer;
                }
            }
        }
        return null;
    }
}

class Customer {
    private String customerId;
    private String username;
    private String name;
    private String licenseNumber;
    private String phone;
    private String email;

    
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
