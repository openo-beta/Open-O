//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import ca.openosp.openo.commn.model.Queue;

public interface QueueDao extends AbstractDao<Queue> {
    HashMap getHashMapOfQueues();

    List<Hashtable> getQueues();

    String getLastId();

    String getQueueName(int id);

    String getQueueid(String name);

    boolean addNewQueue(String qn);
}
