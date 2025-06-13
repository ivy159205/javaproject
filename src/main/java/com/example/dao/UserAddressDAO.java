package com.example.dao;

import com.example.model.UserAddress;
import com.example.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserAddressDAO {

    public boolean addUserAddress(UserAddress address) {
        String sql = "INSERT INTO user_addresses (user_id, address, city, postal_code) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, address.getUserId());
            pstmt.setString(2, address.getAddress());
            pstmt.setString(3, address.getCity());
            pstmt.setString(4, address.getPostalCode());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<UserAddress> getAllUserAddresses() {
        List<UserAddress> addresses = new ArrayList<>();
        String sql = "SELECT * FROM user_addresses";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                UserAddress address = new UserAddress();
                address.setId(rs.getInt("id"));
                address.setUserId(rs.getInt("user_id"));
                address.setAddress(rs.getString("address"));
                address.setCity(rs.getString("city"));
                address.setPostalCode(rs.getString("postal_code"));
                addresses.add(address);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return addresses;
    }

    public UserAddress getAddressById(int id) {
        String sql = "SELECT * FROM user_addresses WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                UserAddress address = new UserAddress();
                address.setId(id);
                address.setUserId(rs.getInt("user_id"));
                address.setAddress(rs.getString("address"));
                address.setCity(rs.getString("city"));
                address.setPostalCode(rs.getString("postal_code"));
                return address;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateUserAddress(UserAddress address) {
        String sql = "UPDATE user_addresses SET user_id = ?, address = ?, city = ?, postal_code = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, address.getUserId());
            pstmt.setString(2, address.getAddress());
            pstmt.setString(3, address.getCity());
            pstmt.setString(4, address.getPostalCode());
            pstmt.setInt(5, address.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteUserAddress(int id) {
        String sql = "DELETE FROM user_addresses WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
