//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.Date;

import ca.openosp.openo.commn.model.SecurityToken;

public interface SecurityTokenDao extends AbstractDao<SecurityToken> {
    SecurityToken getByTokenAndExpiry(String token, Date expiry);
}
