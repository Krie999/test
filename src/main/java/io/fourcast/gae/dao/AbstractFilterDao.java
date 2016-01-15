package io.fourcast.gae.dao;

import com.googlecode.objectify.cmd.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nbuekers on 15/01/16.
 */

/**
 * Use this abstract class if you want to use the generic kn0r3k-filter possibilities with your Entity.
 * In that case, your Entity should extend AbstractFilterDao instead of directly extending AbstractDao.
 * @param <T>
 */
public abstract class AbstractFilterDao<T> extends AbstractDao<T>{
    /**
     * TODO I don't know this code. Kevin?
     * @param project
     * @param fieldName
     * @return
     * @throws Exception
     */
    private Object getValue(T project, String fieldName) throws Exception {
        try {
            java.lang.reflect.Field field = project.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(project);
        } catch (IllegalAccessException e) {
            throw new Exception(e);
        } catch (NoSuchFieldException e) {
            throw new Exception(e);
        }
    }


    /**
     * TODO I don't know this code. Kevin?
     * @param q
     * @param key
     * @param values
     * @return
     */
    private Query<T> applyFilter(Query<T> q, String key, List<? extends Object> values) {
        if (values == null || values.size() == 0) {
            return q;
        }
        return q.filter(key + " in ", values);
    }

    /**
     * TODO I don't know this code. Kevin?
     * @param q
     * @param key
     * @param values
     * @return
     */
    private Query<T> applyFilter(Query<T> q, String key, Object values) {

        return q.filter(key, values);
    }



    /**
     * TODO I don't know this code. Kevin?
     * @param Ids
     * @return
     */
    public List<T> getByIds(List<Long> Ids) {
        return new ArrayList<>(ofy().load().keys(createKeys(Ids)).values());
    }


    /**
     * TODO I don't know this code. Kevin?
     * @param Ids
     * @param collectParent
     * @param collectChildren
     * @return
     */
    public List<T> getByIds(List<Long> Ids, Boolean collectParent, Boolean collectChildren) {
        return this.getByIds(Ids);
    }


    /**
     * TODO I don't know this code. Kevin?
     * @param parentIds
     * @return
     */
    public List<T> getByParentIds(List<Long> parentIds) {
        if (parentIds == null || parentIds.size() == 0) {
            return null;
        }
        return query().filter("parentId in ", parentIds).list();
    }
}
