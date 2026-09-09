package com.senati.apptarea;

/*
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** LIA: base en memoria; nunca borra la base real de la aplicación. */
/*
@RunWith(AndroidJUnit4.class)
public class TaskDbTest {
    @Test public void crudFilterAndCreationDate() {
        try (TaskDb db = new TaskDb(InstrumentationRegistry.getInstrumentation().getTargetContext(), null)) {
            long id = db.save(new Task(0, "Leer O'Reilly", "Capítulo 1", "Pendiente", "2026-10-12", 0, "Lia"));
            Task initial = db.list(null).get(0);
            assertEquals(id, initial.id);
            assertTrue(initial.createdAt > 0);
            assertEquals("Leer O'Reilly", initial.title);
            db.save(new Task(id, "Leer Java", "Capítulo 2", "En progreso", "2026-10-13", 0, "May"));
            assertTrue(db.list("Pendiente").isEmpty());
            Task edited = db.list("En progreso").get(0);
            assertEquals(initial.createdAt, edited.createdAt);
            assertEquals("May", edited.assignedTo);
            assertEquals("Capítulo 2", edited.description);
            assertEquals("2026-10-13", edited.dueDate);
            db.setState(id, "Completada");
            assertEquals(1, db.list("Completada").size());
            db.delete(id);
            assertTrue(db.list(null).isEmpty());
        }
    }
}
*/
