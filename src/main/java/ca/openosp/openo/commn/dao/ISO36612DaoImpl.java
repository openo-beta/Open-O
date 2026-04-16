//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.Iterator;

import javax.persistence.Query;

import org.apache.commons.io.IOUtils;
import ca.openosp.openo.commn.model.ISO36612;
import ca.openosp.openo.utility.MiscUtils;
import org.springframework.stereotype.Repository;

@Repository
public class ISO36612DaoImpl extends AbstractDaoImpl<ISO36612> implements ISO36612Dao {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public ISO36612DaoImpl() {
        super(ISO36612.class);
    }

    public ISO36612 findByCode(String code) {
        String sqlCommand = "select i from ISO36612 where code=?1";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, code);

        return this.getSingleResultOrNull(query);
    }

    public String findProvinceByCode(String code) {
        ISO36612 result = findByCode(code);
        if (result != null) {
            return result.getProvince();
        }
        return null;
    }

    public String findCountryByCode(String code) {
        ISO36612 result = findByCode(code);
        if (result != null) {
            return result.getCountry();
        }
        return null;
    }

    public boolean reloadTable() {

        InputStream in = null;
        ObjectNode topLevelObj = null;
        try {
            in = this.getClass().getClassLoader().getResourceAsStream("iso-3166-2.json");
            String theString = IOUtils.toString(in, "UTF-8");
            topLevelObj = (ObjectNode) objectMapper.readTree(theString);
        } catch (Exception e) {
            MiscUtils.getLogger().warn("Warning", e);
            return false;
        } finally {
            IOUtils.closeQuietly(in);
        }

        deleteAll();

        try {
            Iterator<String> iter = topLevelObj.fieldNames();
            while (iter.hasNext()) {
                String countryCode = iter.next();
                ObjectNode country = (ObjectNode) topLevelObj.get(countryCode);
                String countryName = country.get("name").asText();
                ObjectNode divisions = (ObjectNode) country.get("divisions");
                Iterator<String> iter2 = divisions.fieldNames();
                while (iter2.hasNext()) {
                    String divisionCode = iter2.next();
                    String divisionName = divisions.get(divisionCode).asText();

                    ISO36612 item = new ISO36612();
                    item.setCode(divisionCode);
                    item.setProvince(divisionName);
                    item.setCountry(countryName);
                    persist(item);

                }
            }
        } catch (Exception e) {
            MiscUtils.getLogger().warn("Warning", e);
        }

        return true;
    }

    protected void deleteAll() {
        Query query = entityManager.createQuery("DELETE FROM ISO36612");
        query.executeUpdate();
    }

}
