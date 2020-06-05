package net.tangly.commons.vaadin;

import com.vaadin.flow.component.formlayout.FormLayout;

public interface CrudForm<T> {
    enum Operation {
        VIEW, UPDATE, CREATE, DELETE;

        static boolean isReadWrite(Operation operation) {
            return (operation == Operation.CREATE) || ((operation == Operation.UPDATE));
        }

        static boolean isReadOnly(Operation operation) {
            return (operation == Operation.VIEW) || ((operation == Operation.DELETE));
        }
    }

    FormLayout createForm(Operation operation, T entity);

    default T formCompleted(Operation operation, T entity) {
        return entity;
    }

    default void formCancelled(Operation operation, T entity) {
    }
}
