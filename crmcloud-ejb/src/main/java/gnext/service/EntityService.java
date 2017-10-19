package gnext.service;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 * 
 * @author daind
 * @param <T> 
 */
public interface EntityService<T> extends java.io.Serializable {
    List<T> findAll();
    T find(int id);
    
    T create(T t) throws Exception;
    T edit(T t) throws Exception;
    void remove(T t) throws Exception;
    
    EntityTransaction beginTransaction(final EntityManager entityManager);
    void commitAndCloseTransaction(EntityTransaction tx);
    void rollbackAndCloseTransaction(EntityTransaction tx);
}
