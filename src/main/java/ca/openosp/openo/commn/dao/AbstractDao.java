//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.AbstractModel;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public interface AbstractDao<T extends AbstractModel<?>> {
    public static final int MAX_LIST_RETURN_SIZE = 5000;

    void merge(AbstractModel<?> o);

    void persist(AbstractModel<?> o);

    void batchPersist(List<T> oList);

    void batchPersist(List<T> oList, int batchSize);

    void remove(AbstractModel<?> o);

    void batchRemove(List<T> oList);

    void batchRemove(List<T> oList, int batchSize);

    void refresh(AbstractModel<?> o);

    T find(Object id);

    T find(int id);

    T findDetached(Object id);

    void detach(@Nonnull T t);

    boolean contains(AbstractModel<?> o);

    List<T> findAll(Integer offset, Integer limit);

    boolean remove(Object id);

    int getCountAll();

    List<Object[]> runParameterizedNativeQuery(String sql, Map<String, Object> params);

    T saveEntity(T entity);

    Class<T> getModelClass();

    void flush();

}
