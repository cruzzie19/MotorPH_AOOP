/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author Leianna Cruz
 */
package RBAC;

import java.util.Collections;
import java.util.Set;


public class Role {

    private final String name;
    private final Set<Permission> permissions;

    public Role(String name, Set<Permission> permissions) {
        this.name = name;
        this.permissions = permissions;
    }

    public boolean hasPermission(Permission permission) {
        if (permission == null) {
            return false;
        }
        return permissions.contains(permission);
    }

    public String getName() {
        return name;
    }

    public Set<Permission> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

    @Override
    public String toString() {
        return name;
    }
}