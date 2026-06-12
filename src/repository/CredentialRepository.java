/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package repository;

import model.CredentialRecord;
import java.time.OffsetDateTime;

import java.sql.SQLException;
import java.io.IOException;

import java.util.List;

/**
 *
 * @author Elizabeth
 */
public interface CredentialRepository{
    CredentialRecord findByUsername(String username) throws SQLException;
    void upsert(String username, byte[] newHash, byte[] newSalt, OffsetDateTime timestampCurrent) throws IOException;
    boolean hasAccounts() throws SQLException;
    
    List<CredentialRecord> retrieveAll() throws SQLException;
}
