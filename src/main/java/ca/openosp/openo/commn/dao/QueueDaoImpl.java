//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.Queue;
import ca.openosp.openo.utility.MiscUtils;
import org.springframework.stereotype.Repository;

@Repository
public class QueueDaoImpl extends AbstractDaoImpl<Queue> implements QueueDao {
    public QueueDaoImpl() {
        super(Queue.class);
    }

    @Override
    public HashMap getHashMapOfQueues() {
        String q = "select q from Queue q";
        Query query = entityManager.createQuery(q);
        List<Queue> result = new ArrayList<Queue>();
        result = query.getResultList();
        HashMap<Integer, String> hm = new HashMap<Integer, String>();
        for (Queue que : result) {
            hm.put(que.getId(), que.getName());
        }
        return hm;
    }

    @Override
    public List<Hashtable> getQueues() {
        String q = "select q from Queue q";
        Query query = entityManager.createQuery(q);
        List<Queue> result = new ArrayList<Queue>();
        result = query.getResultList();
        List<Hashtable> r = new ArrayList();
        for (Queue que : result) {
            Hashtable ht = new Hashtable();
            ht.put("id", que.getId());
            ht.put("queue", que.getName());
            r.add(ht);
        }
        return r;
    }

    @Override
    public String getLastId() {
        String r = "";
        try {
            Query query = entityManager.createQuery("select MAX(q.id) from Queue q");
            Integer ri = (Integer) query.getSingleResult();
            r = ri.toString();
        } catch (NoResultException e) {
            //ignore
        }

        return r;
    }

    @Override
    public String getQueueName(int id) {

        String q = "select q from Queue q where q.id=" + id;
        Query query = entityManager.createQuery(q);
        try {
            Queue result = (Queue) query.getSingleResult();
            return result.getName();
        } catch (NoResultException e) {
            //ignore
        }
        return "";
    }

    @Override
    public String getQueueid(String name) {
        String q = "select q from Queue q where q.name=" + name;
        Query query = entityManager.createQuery(q);
        try {
            Queue result = (Queue) query.getSingleResult();
            return result.getId().toString();
        } catch (NoResultException e) {
            //ignore
        }
        return "";
    }

    @Override
    public boolean addNewQueue(String qn) {
        try {
            Queue q = new Queue();
            q.setName(qn);
            entityManager.persist(q);
        } catch (Exception e) {
            MiscUtils.getLogger().error("Error", e);
            return false;
        }
        return true;
    }
}
