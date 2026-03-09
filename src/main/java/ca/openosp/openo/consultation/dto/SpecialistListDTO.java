package ca.openosp.openo.consultation.dto;

import java.io.Serializable;

/**
 * Lightweight DTO for specialist list views (Edit Specialists, Display Service).
 * <p>
 * Uses JPQL constructor projection to fetch only the columns needed for list display,
 * avoiding full {@code ProfessionalSpecialist} entity hydration which includes heavy
 * text fields ({@code eDataOscarKey}, {@code eDataServiceKey}, {@code annotation}).
 * With 10k+ specialist records, this eliminates significant unnecessary data transfer.
 * </p>
 *
 * @since 2026-03-09
 */
public class SpecialistListDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Integer id;
    private final String firstName;
    private final String lastName;
    private final String professionalLetters;
    private final String streetAddress;
    private final String phoneNumber;
    private final String faxNumber;

    /**
     * JPQL constructor projection target.
     *
     * @param id Integer the specialist ID (maps to {@code specId} column)
     * @param firstName String the specialist's first name
     * @param lastName String the specialist's last name
     * @param professionalLetters String professional designation letters (e.g. "MD, FRCPC")
     * @param streetAddress String the specialist's street address
     * @param phoneNumber String the specialist's phone number
     * @param faxNumber String the specialist's fax number
     */
    public SpecialistListDTO(Integer id, String firstName, String lastName,
                             String professionalLetters, String streetAddress,
                             String phoneNumber, String faxNumber) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.professionalLetters = professionalLetters;
        this.streetAddress = streetAddress;
        this.phoneNumber = phoneNumber;
        this.faxNumber = faxNumber;
    }

    public Integer getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getProfessionalLetters() { return professionalLetters; }
    public String getStreetAddress() { return streetAddress; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getFaxNumber() { return faxNumber; }
}
