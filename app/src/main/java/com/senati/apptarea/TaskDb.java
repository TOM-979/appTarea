package com.senati.apptarea;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** LIA: llamar estos métodos desde un hilo de trabajo. */
public final class TaskDb extends SQLiteOpenHelper {
    public TaskDb(Context context) {
        this(context, "tasks.db");
    }

    TaskDb(Context context, String databaseName) {
        super(context.getApplicationContext(), databaseName, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE tasks (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "title TEXT NOT NULL CHECK(length(trim(title)) > 0), "
                + "description TEXT NOT NULL DEFAULT '', "
                + "state TEXT NOT NULL CHECK(state IN ('Pendiente','En progreso','Completada')), "
                + "due_date TEXT NOT NULL, created_at INTEGER NOT NULL, "
                + "assigned_to TEXT NOT NULL DEFAULT '')");
        db.execSQL("CREATE INDEX idx_tasks_state_due ON tasks(state, due_date)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // La versión 1 no tiene migraciones. Añadir ALTER TABLE al incrementar versión.
        throw new IllegalStateException("Falta una migración de la base de datos");
    }

    public List<Task> list(String state) {
        List<Task> tasks = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query("tasks", null,
                state == null ? null : "state = ?",
                state == null ? null : new String[]{state}, null, null,
                "due_date ASC, id DESC")) {
            while (c.moveToNext()) {
                tasks.add(new Task(c.getLong(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("title")),
                        c.getString(c.getColumnIndexOrThrow("description")),
                        c.getString(c.getColumnIndexOrThrow("state")),
                        c.getString(c.getColumnIndexOrThrow("due_date")),
                        c.getLong(c.getColumnIndexOrThrow("created_at")),
                        c.getString(c.getColumnIndexOrThrow("assigned_to"))));
            }
        }
        return tasks;
    }

    public long save(Task t) {
        if (t.title.trim().isEmpty() || !Arrays.asList(Task.STATES).contains(t.state))
            throw new IllegalArgumentException("Título o estado inválido");
        LocalDate.parse(t.dueDate); // Exige una fecha real ISO yyyy-MM-dd.
        ContentValues v = new ContentValues();
        v.put("title", t.title.trim());
        v.put("description", t.description.trim());
        v.put("state", t.state);
        v.put("due_date", t.dueDate);
        v.put("assigned_to", t.assignedTo.trim());
        if (t.id == 0) {
            v.put("created_at", System.currentTimeMillis());
            return getWritableDatabase().insertOrThrow("tasks", null, v);
        }
        // created_at se conserva al editar.
        requireOne(getWritableDatabase().update("tasks", v, "id = ?",
                new String[]{Long.toString(t.id)}));
        return t.id;
    }

    public void setState(long id, String state) {
        if (!Arrays.asList(Task.STATES).contains(state))
            throw new IllegalArgumentException("Estado inválido");
        ContentValues v = new ContentValues();
        v.put("state", state);
        requireOne(getWritableDatabase().update("tasks", v, "id = ?",
                new String[]{Long.toString(id)}));
    }

    public void delete(long id) {
        requireOne(getWritableDatabase().delete("tasks", "id = ?",
                new String[]{Long.toString(id)}));
    }

    private void requireOne(int count) {
        if (count != 1) throw new IllegalStateException("La tarea ya no existe");
    }
}
