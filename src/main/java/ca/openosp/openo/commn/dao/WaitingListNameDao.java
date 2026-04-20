//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.MyGroup;
import ca.openosp.openo.commn.model.WaitingListName;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface WaitingListNameDao extends AbstractDao<WaitingListName> {
    long countActiveWatingListNames();

    List<WaitingListName> findCurrentByNameAndGroup(String name, String group);

    List<WaitingListName> findByMyGroups(String providerNo, List<MyGroup> myGroups);

    List<WaitingListName> findCurrentByGroup(String group);
}
