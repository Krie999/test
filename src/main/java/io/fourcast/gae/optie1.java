package io.fourcast.bnppf.model.common;

import com.google.appengine.api.datastore.ReadPolicy;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;

/**
 * Created by kevinnorek on 2/12/15.
 */

@Entity
public abstract class CodeGenerator<T extends CodeGenerator> implements Serializable {

    @Id
    String name = this.getClass().getName();

    Integer counter;

    public Objectify ofy() {
        return ObjectifyService.ofy().cache(true).consistency(ReadPolicy.Consistency.STRONG);
    }


    protected abstract String generate();

    private final Class<T> typeClass = ((Class<T>) ((ParameterizedType) getClass()
            .getGenericSuperclass()).getActualTypeArguments()[0]);

    public CodeGenerator generateNewCode() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Key<CodeGenerator> key = Key.create(this.getClass(), name);
        CodeGenerator gen = ofy().load().key(key).now();

        //create if does not exist
        if (gen == null) {
            gen = (T) Class.forName(typeClass.getName()).newInstance();
        }
        
        gen.generate();
        ofy().save().entity(gen).now();


        return gen;

    }

    public Integer getCounter() {
        return counter;
    }

    public void setCounter(Integer counter) {
        this.counter = counter;
    }


}
