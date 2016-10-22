package fr.mines.event_manager.framework.repository;

import fr.mines.event_manager.framework.entity.AbstractEntity;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import java.lang.reflect.ParameterizedType;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class DatabaseManager<T extends AbstractEntity> {
    enum Action {CREATE, READ, UPDATE, DELETE}

    CriteriaBuilder cb = null;

    protected Class classType;

    public DatabaseManager() {
        try {
            this.classType = this.getClassType();
            this.cb = this.getEntityManager().getCriteriaBuilder();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    protected EntityManager getEntityManager() {
        return BaseEntityManagerWrapper.getInstance().getEntityManager();
    }

    protected Class<T> getClassType() throws ClassNotFoundException {
        return ((Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
    }

    public boolean updateDatabase(Action action, Optional<T> object, boolean withTransactionSelfManaged) {
        if (!object.isPresent()) {
            return false;
        }

        if (withTransactionSelfManaged) {
            begin();
        }

        switch (action) {
            case CREATE:
                getEntityManager().merge(object.get());
                break;
            case UPDATE:
                getEntityManager().merge(object.get());
                break;
            case DELETE:
                getEntityManager().remove(object.get());
                break;
        }

        if (withTransactionSelfManaged) {
            commit();
        }

        return true;
    }

    public void begin() {
        getEntityManager().getTransaction().begin();
    }

    public void commit() {
        getEntityManager().getTransaction().commit();
    }

    public AbstractMap.SimpleEntry<Root<T>, CommonAbstractCriteria> getBaseQuery(Action action) {
        if (action.equals(Action.READ)) {
            CriteriaQuery<T> c = cb.createQuery(this.classType);
            Root<T> root = c.from(this.classType);

            return new AbstractMap.SimpleEntry<>(root, c);
        }

        if (action.equals(Action.UPDATE)) {
            CriteriaUpdate<T> c = cb.createCriteriaUpdate(this.classType);
            Root<T> root = c.from(this.classType);

            return new AbstractMap.SimpleEntry<>(root, c);
        }

        if (action.equals(Action.DELETE)) {
            CriteriaDelete<T> c = cb.createCriteriaDelete(this.classType);
            Root<T> root = c.from(this.classType);

            return new AbstractMap.SimpleEntry<>(root, c);
        }

        return new AbstractMap.SimpleEntry<>(null, null);
    }

    public Query getQueryBy(Field... fields) {
        AbstractMap.SimpleEntry<Root<T>, CommonAbstractCriteria> entry = this.getBaseQuery(Action.READ);

        List<Predicate> predicates = new ArrayList<>();
        for (Field field : fields) {
            switch (field.getFilter())
            {
                case EQUAL:
                    predicates.add(cb.equal(entry.getKey().get(field.getLabel()), field.getValue()));
                    break;
                case LIKE:
                    predicates.add(cb.like(entry.getKey().get(field.getLabel()), (String)field.getValue()));
                    break;
            }
        }

        (((CriteriaQuery<T>) entry.getValue()))
                .where(cb.and(predicates.toArray(new Predicate[predicates.size()])));

        return getEntityManager().createQuery((((CriteriaQuery<T>) entry.getValue())));
    }
}