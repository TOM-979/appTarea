package com.senati.apptarea;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.text.DateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Date;

/**
 * TOM: mismo formulario para crear y editar; conserva borrador al rotar.
 */
public final class TaskEditorDialog extends DialogFragment {

    private static final String ARG_TASK = "task";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_DUE = "due";
    private static final String KEY_ASSIGNED = "assigned";
    private static final String KEY_STATE = "state";

    private EditText editTitle;
    private EditText editDescription;
    private EditText editDue;
    private EditText editAssigned;
    private Spinner spinnerState;

    private Task original;
    private boolean saving;

    public static TaskEditorDialog newInstance(Task task) {
        TaskEditorDialog editor = new TaskEditorDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TASK, task);
        editor.setArguments(args);
        return editor;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            original = (Task) getArguments().getSerializable(ARG_TASK);
        }

        View form = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_task, null);

        editTitle = form.findViewById(R.id.editTitle);
        editDescription = form.findViewById(R.id.editDescription);
        editDue = form.findViewById(R.id.editDue);
        editAssigned = form.findViewById(R.id.editAssigned);
        spinnerState = form.findViewById(R.id.editState);
        TextView txtCreationDate = form.findViewById(R.id.creationDate);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                Task.STATES
        );
        spinnerState.setAdapter(adapter);

        // Restaurar estado (prioridad: savedInstanceState -> original -> valor por defecto)
        if (savedInstanceState != null) {
            editTitle.setText(savedInstanceState.getString(KEY_TITLE, ""));
            editDescription.setText(savedInstanceState.getString(KEY_DESCRIPTION, ""));
            editDue.setText(savedInstanceState.getString(KEY_DUE, LocalDate.now().toString()));
            editAssigned.setText(savedInstanceState.getString(KEY_ASSIGNED, ""));
            spinnerState.setSelection(savedInstanceState.getInt(KEY_STATE, 0));
        } else if (original != null) {
            editTitle.setText(original.title);
            editDescription.setText(original.description);
            editDue.setText(original.dueDate);
            editAssigned.setText(original.assignedTo);

            int initialState = Arrays.asList(Task.STATES).indexOf(original.state);
            spinnerState.setSelection(Math.max(initialState, 0));
        } else {
            editDue.setText(LocalDate.now().toString());
        }

        // Formato de fecha de creación
        if (original == null) {
            txtCreationDate.setText("Creación: se registra al guardar");
        } else {
            String formattedCreation = DateFormat.getDateTimeInstance().format(new Date(original.createdAt));
            txtCreationDate.setText("Creada: " + formattedCreation);
        }

        // DatePicker para la fecha limite
        editDue.setOnClickListener(v -> showDatePicker());

        return new AlertDialog.Builder(requireContext(), R.style.OnsenDialog)
                .setTitle(original == null ? "Nueva tarea" : "Editar tarea")
                .setView(form)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", null)
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) requireDialog();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (saving) return;

            String titleText = editTitle.getText().toString().trim();
            if (titleText.isEmpty()) {
                editTitle.setError("Escribe un título");
                return;
            }

            Task task = new Task(
                    original == null ? 0 : original.id,
                    titleText,
                    editDescription.getText().toString(),
                    Task.STATES[spinnerState.getSelectedItemPosition()],
                    editDue.getText().toString(),
                    original == null ? 0 : original.createdAt,
                    editAssigned.getText().toString()
            );

            setSaving(true);
            dismiss(); // Evita restaurar un formulario ya enviado al rotar.

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).saveTask(task, this);
            }
        });
    }

    public void setSaving(boolean value) {
        saving = value;
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null && dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!value);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (editTitle != null) {
            outState.putString(KEY_TITLE, editTitle.getText().toString());
            outState.putString(KEY_DESCRIPTION, editDescription.getText().toString());
            outState.putString(KEY_DUE, editDue.getText().toString());
            outState.putString(KEY_ASSIGNED, editAssigned.getText().toString());
            outState.putInt(KEY_STATE, spinnerState.getSelectedItemPosition());
        }
    }

    private void showDatePicker() {
        LocalDate date;
        try {
            date = LocalDate.parse(editDue.getText().toString());
        } catch (DateTimeParseException e) {
            date = LocalDate.now();
        }

        new DatePickerDialog(
                requireContext(),
                (picker, year, month, day) -> editDue.setText(LocalDate.of(year, month + 1, day).toString()),
                date.getYear(),
                date.getMonthValue() - 1,
                date.getDayOfMonth()
        ).show();
    }
}