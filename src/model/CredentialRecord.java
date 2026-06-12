/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.time.OffsetDateTime;

/**
 *
 * @author Elizabeth
 */
public class CredentialRecord {
    private String username;
    private byte[] passwordHash;
    private byte[] passwordSalt;
    private OffsetDateTime lastPaswordChange;

    public CredentialRecord(String username, byte[] passwordHash, byte[] passwordSalt, OffsetDateTime lastPaswordChange) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
        this.lastPaswordChange = lastPaswordChange;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public byte[] getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(byte[] passwordHash) {
        this.passwordHash = passwordHash;
    }

    public byte[] getPasswordSalt() {
        return passwordSalt;
    }

    public void setPasswordSalt(byte[] passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    public OffsetDateTime getLastPaswordChange() {
        return lastPaswordChange;
    }

    public void setLastPaswordChange(OffsetDateTime lastPaswordChange) {
        this.lastPaswordChange = lastPaswordChange;
    }

    
    
}
