package com.senati.apptarea;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * TOM: acciones y coordinación. SQL queda en TaskDb; diseños en XML.
 */
public class MainActivity extends AppCompatActivity {
    private static final String KEY_FILTER = "filter";
    private static final String TAG_EDITOR = "editor";
    // Compartido: una recarga después de rotar espera a la escritura anterior.
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor();
    private TaskDb db;
    private TaskAdapter adapter;
    private Spinner filter;
    private TextView empty;
    private ProgressBar progressBar;
    private TextView progressLabel;
    private int requestVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets bars = insets.getInsets(
                        WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime()
                );
                v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                return insets;
            });
        }

        db = new TaskDb(this);
        adapter = new TaskAdapter();

        ListView list = findViewById(R.id.taskList);
        empty = findViewById(R.id.emptyText);
        progressBar = findViewById(R.id.progressBar);
        progressLabel = findViewById(R.id.progressLabel);

        list.setAdapter(adapter);
        list.setEmptyView(empty);
        list.setOnItemClickListener((parent, view, position, id) -> actions(adapter.getItem(position)));

        filter = findViewById(R.id.filterState);
        String[] filterOptions = new String[]{"Todas", "Pendiente", "En progreso", "Completada"};
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                filterOptions
        );
        filter.setAdapter(filterAdapter);

        if (savedInstanceState != null) {
            filter.setSelection(savedInstanceState.getInt(KEY_FILTER, 0));
        }

        filter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                reload();
            }

            @Override
            public void onNothingSelected(AdapterView<?> p) {
                // Sin acción requerida
            }
        });

        findViewById(R.id.addTask).setOnClickListener(v -> edit(null));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (db != null) {
            reload();
        }
    }

    private void reload() {
        final int version = ++requestVersion;
        final int selectedPos = filter.getSelectedItemPosition();
        final String state = (selectedPos == 0) ? null : Task.STATES[selectedPos - 1];

        WORKER.execute(() -> {
            try {
                List<Task> all = db.list(null);
                List<Task> result = (state == null) ? all
                        : all.stream().filter(t -> state.equals(t.state)).collect(Collectors.toList());
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed() || version != requestVersion) return;
                    empty.setText(R.string.no_tasks);
                    adapter.submit(result);
                    updateProgress(all);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed() || version != requestVersion) return;
                    empty.setText(R.string.load_error);
                    showError();
                });
            }
        });
    }

    private void updateProgress(List<Task> all) {
        int total = all.size();
        long done = all.stream().filter(t -> Task.STATES[2].equals(t.state)).count();
        int percent = total == 0 ? 0 : (int) (done * 100 / total);
        progressBar.setProgress(percent);
        progressLabel.setText(total == 0 ? "Sin tareas todavía"
                : done + " de " + total + " tareas completadas (" + percent + "%)");
    }

    private void actions(Task t) {
        String[] options = new String[]{"Ver / editar", "Cambiar estado", "Eliminar"};

        new AlertDialog.Builder(this, R.style.OnsenDialog)
                .setTitle(t.title)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        edit(t);
                    } else if (which == 1) {
                        showChangeStateDialog(t);
                    } else if (which == 2) {
                        showDeleteConfirmationDialog(t);
                    }
                })
                .show();
    }

    private void showChangeStateDialog(Task t) {
        new AlertDialog.Builder(this, R.style.OnsenDialog)
                .setTitle("Cambiar estado")
                .setItems(Task.STATES, (dialog, index) -> {
                    String newState = Task.STATES[index];
                    mutate(() -> db.setState(t.id, newState));
                    if (Task.STATES[2].equals(newState)) {
                        Toast.makeText(this, "¡Tarea completada! 🎉", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showDeleteConfirmationDialog(Task t) {
        new AlertDialog.Builder(this, R.style.OnsenDialog)
                .setTitle("¿Eliminar tarea?")
                .setMessage("Se eliminará “" + t.title + "”. Esta acción no se puede deshacer.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (d, w) -> mutate(() -> db.delete(t.id)))
                .show();
    }

    private void edit(Task task) {
        if (getSupportFragmentManager().findFragmentByTag(TAG_EDITOR) == null) {
            TaskEditorDialog.newInstance(task).show(getSupportFragmentManager(), TAG_EDITOR);
        }
    }

    public void saveTask(Task task, TaskEditorDialog editor) {
        WORKER.execute(() -> {
            try {
                db.save(task);
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    if (editor.isAdded()) {
                        editor.dismissAllowingStateLoss();
                    }
                    reload();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    edit(task); // Recuperar los campos si no se pudo guardar.
                    showError();
                });
            }
        });
    }

    private void mutate(Runnable operation) {
        WORKER.execute(() -> {
            try {
                operation.run();
                runOnUiThread(() -> {
                    if (!isFinishing() && !isDestroyed()) {
                        reload();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(this::showError);
            }
        });
    }

    private void showError() {
        if (!isFinishing() && !isDestroyed()) {
            Toast.makeText(this, R.string.operation_error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (filter != null) {
            outState.putInt(KEY_FILTER, filter.getSelectedItemPosition());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        WORKER.execute(db::close);
        super.onDestroy();
    }
}