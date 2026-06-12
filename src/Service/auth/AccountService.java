/**
 *
 * @author Leianna Cruz
 */

package service.auth;

import model.CredentialRecord;
import model.Employee;
import repository.CredentialRepository;
import repository.EmployeeRepository;
import service.EmployeeService;

import java.io.IOException;
import java.sql.SQLException;


import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import repository.DbCredentialRepository;
import repository.DbEmployeeRepository;

public class AccountService {

    private final PasswordManager passwordManager;
    private final CredentialRepository credentialRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeService employeeService;

    public AccountService(CredentialRepository credentialRepository, EmployeeRepository employeeRepository) {
        this.passwordManager = new PasswordManager();
        this.credentialRepository = credentialRepository;
        
        this.employeeRepository = employeeRepository;
        this.employeeService = new EmployeeService(employeeRepository);
    }

    public boolean registerOrUpdate(String username, char[] password) {
        if (password == null || password.length == 0) {
            throw new IllegalArgumentException("Employee ID and password cannot be empty.");
        }
        
        Employee validEmployee = employeeService.findById(username);
        if (validEmployee == null) {
            throw new IllegalArgumentException("Registration failed: Employee ID does not exist in company records.");
        }
        
        try{
            OffsetDateTime currentTimestamp = OffsetDateTime.now();

            byte[] newSalt = passwordManager.generateSalt();
            byte[] newHash = passwordManager.hashPassword(password, newSalt);
            credentialRepository.upsert(username, newHash, newSalt, currentTimestamp);
            
            return true;
        } catch (IOException e){
            return false;
        }
        
    }
    
    public boolean validate(String username, String password) throws IOException,SQLException {
        CredentialRecord record = credentialRepository.findByUsername(username);
        
        if (record==null){
            return false;
        }
        
        return passwordManager.verifyPassword(password.toCharArray(), record.getPasswordSalt(), record.getPasswordHash());
    }
    
    public Employee findEmployeeByUsername(String username) {
        return employeeService.findById(username);
    }
    
    public CredentialRecord findByUsername(String username) throws SQLException{        
        return credentialRepository.findByUsername(username);
    }
    
    public boolean hasAccounts() throws SQLException{
        return credentialRepository.hasAccounts();
    }
    
    public List<CredentialRecord> retrieveAll() throws IOException {
        try {
            List<Employee> allEmployees = employeeRepository.loadAll();
            List<CredentialRecord> credentialRecords = credentialRepository.retrieveAll();

            Map<String, CredentialRecord> credMap = new HashMap<>();
            for (CredentialRecord cred : credentialRecords) {
                credMap.put(cred.getUsername().toLowerCase(), cred);
            }

            List<CredentialRecord> combinedList = new ArrayList<>();

            for (Employee emp : allEmployees) {
                String empId = emp.getId();
                CredentialRecord match = credMap.get(empId.toLowerCase());

                if (match != null) {
                    combinedList.add(match);
                } else {
                    combinedList.add(new CredentialRecord(empId, null, null, null));
                }
            }

            return combinedList;
        
        } catch (SQLException ex) {
            throw new IOException("Failed to cross-reference system credential records.", ex);
        }
    }
    
    public static AccountService createDefault() {
        return new AccountService(new DbCredentialRepository(), new DbEmployeeRepository());
    }

}