/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package repository;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import model.CredentialRecord;

import java.time.OffsetDateTime;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;

import java.util.List;

/**
 *
 * @author Elizabeth
 */
public class DbCredentialRepository implements CredentialRepository{
    
    @Override
    public CredentialRecord findByUsername(String username) throws SQLException{
        String sql = """
                SELECT employee_id,
                     password_hash,
                     password_salt,
                     last_password_change
                FROM credential
                WHERE employee_id = ?
                """;
        
        try (Connection connection = DbConnectionFactory.open(); PreparedStatement preparedStatement = connection.prepareStatement(sql);) {
            
            preparedStatement.setString(1, username);
            
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                if (resultSet.next()) {
                    byte[] hash = resultSet.getBytes("password_hash");
                    byte[] salt = resultSet.getBytes("password_salt");
                    OffsetDateTime lastChange = resultSet.getObject("last_password_change", OffsetDateTime.class);

                    return new CredentialRecord(username, hash, salt, lastChange);

                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to retrieve credential record from database.", ex);
        }
        
        return null;
    }
    
    @Override
    public void upsert(String username, byte[] hash, byte[] salt, OffsetDateTime lastChange) throws IOException{
        String upsertSql = """
                INSERT INTO credential (
                    employee_id,
                    password_hash,
                    password_salt,
                    last_password_change
                ) VALUES (?, ?, ?, ?)
                ON CONFLICT (employee_id) DO UPDATE SET
                    password_hash = EXCLUDED.password_hash,
                    password_salt = EXCLUDED.password_salt,
                    last_password_change = EXCLUDED.last_password_change
                """;
        
        try (Connection connection = DbConnectionFactory.open(); PreparedStatement statement = connection.prepareStatement(upsertSql)) {
            
            statement.setString(1, username);
            statement.setBytes(2, hash);
            statement.setBytes(3, salt);
            statement.setObject(4, lastChange);

            statement.executeUpdate();
            
        } catch (SQLException ex) {
            throw new IOException("Failed to save credential records to database", ex);
        }
    }
    
    @Override
    public boolean hasAccounts() throws SQLException{
        String sql = "SELECT 1 FROM credential LIMIT 1";
        
        try (Connection connection = DbConnectionFactory.open();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql)) {

            return resultSet.next();
            
        } catch (SQLException ex) {
            throw new SQLException("Failed to load employees from the database", ex);
        }
    }
    
    @Override
    public List<CredentialRecord> retrieveAll() throws SQLException {
        List<CredentialRecord> list = new java.util.ArrayList<>();
        String sql = "SELECT * FROM credential";

        try (Connection connection = DbConnectionFactory.open(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String username = resultSet.getString("employee_id");
                byte[] hash = resultSet.getBytes("password_hash");
                byte[] salt = resultSet.getBytes("password_salt");
                OffsetDateTime lastChange = resultSet.getObject("last_password_change", OffsetDateTime.class);

                list.add(new CredentialRecord(username, hash, salt, lastChange));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to fetch credential data for display.", ex);
        }
        return list;
    }
    
}
