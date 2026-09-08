package com.senati.apptarea;

import java.io.Serializable;

/** Contrato compartido: acordar cambios entre los tres antes de modificarlo. */
public final class Task implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String[] STATES = {"Pendiente", "En progreso", "Completada"};
    public final long id;
    public final String title, description, state, dueDate, assignedTo;
    public final long createdAt;

    public Task(long id, String title, String description, String state,
                String dueDate, long createdAt, String assignedTo) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.state = state;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.assignedTo = assignedTo;
    }
}