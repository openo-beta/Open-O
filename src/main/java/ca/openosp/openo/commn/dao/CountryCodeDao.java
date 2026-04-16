//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.CountryCode;

public interface CountryCodeDao extends AbstractDao<CountryCode> {
    List<CountryCode> findAll();

    List<CountryCode> getAllCountryCodes();

    List<CountryCode> getAllCountryCodes(String locale);

    CountryCode getCountryCode(String countryCode);

    CountryCode getCountryCode(String countryCode, String locale);
}
