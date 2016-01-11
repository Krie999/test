package io.fourcast.gae.model.common;

import com.google.appengine.api.datastore.ReadPolicy;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

import java.io.Serializable;

/**
 * Created by kevinnorek on 2/12/15.
 */

public abstract class CodeGenerator<T extends CodeGenerator> implements Serializable {

    private Integer counter;

    public Objectify ofy() {
        return ObjectifyService.ofy().cache(true).consistency(ReadPolicy.Consistency.STRONG);
    }

    protected Integer generateNewCode(Key<T> key, T newGen){
        T generator = getGenerator(key);
        if(generator == null){
            generator = newGen;

        }

        if(generator == null || generator.getCounter() == null){
            generator.setCounter(1);
        }else{
            generator.setCounter(generator.getCounter() + 1);
        }

       ofy().save().entity(generator).now();

        return generator.getCounter();

    };



    public T getGenerator(Key<T> key){
        return ofy().load().key(key).now();
    }




    public Integer getCounter() {
        return counter;
    }

    public void setCounter(Integer counter) {
        this.counter = counter;
    }


}
