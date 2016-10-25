package fr.mines.event_manager.framework.repository;

import fr.mines.event_manager.framework.entity.AbstractSelfManagedEntity;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import java.lang.reflect.ParameterizedType;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class DatabaseManager<T extends AbstractSelfManagedEntity> {
    protected enum Action {CREATE, READ, UPDATE, DELETE}

    protected CriteriaBuilder cb = null;

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

    public void performRequest(Consumer<EntityManager> request) {
        begin();
        request.accept(this.getEntityManager());
        commit();
    }

    public T create(T object) {

        this.performRequest(entityManager -> {
            entityManager.persist(object);
            entityManager.flush();
        });

        return object;
    }

    public boolean update(Optional<T> object) {
        if (object.isPresent()) {
            this.performRequest(em -> em.merge(object.get()));
            return true;
        }

        return false;
    }

    public boolean remove(Optional<T> object) {
        if (object.isPresent()) {
            this.performRequest(em -> em.remove(object.get()));
            return true;
        }

        return false;
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
            switch (field.getFilter()) {
                case EQUAL:
                    predicates.add(cb.equal(entry.getKey().get(field.getLabel()), field.getValue()));
                    break;
                case LIKE:
                    predicates.add(cb.like(entry.getKey().get(field.getLabel()), (String) field.getValue()));
                    break;
            }
        }

        (((CriteriaQuery<T>) entry.getValue()))
                .where(cb.and(predicates.toArray(new Predicate[predicates.size()])));

        return getEntityManager().createQuery((((CriteriaQuery<T>) entry.getValue())));
    }
}
