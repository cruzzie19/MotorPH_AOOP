/**
 *
 * @author Leianna Cruz
 */

package service;

import model.Employee;
import service.auth.AccountService;

import java.io.IOException;
import java.sql.SQLException;

public class AuthenticationService {

    private final AccountService accountService;

    public AuthenticationService(AccountService accountService) {
        this.accountService = accountService;
    }

    public Employee login(String username, String password) throws IOException, SQLException{

        if (username == null || username.trim().isEmpty() || password == null) {
            return null;
        }

        boolean valid = accountService.validate(username.trim(), password);

        if (!valid) {
            return null;
        }

        return accountService.findEmployeeByUsername(username.trim());
    }
}