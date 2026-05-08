/**
 *
 * @author Leianna Cruz
 */

package model;

import java.time.LocalDate;

public interface PersonRecord {
    String getId();
    String getFirstName();
    String getLastName();
    String getFullName();
    LocalDate getBirthDate();
}