package com.senati.apptarea;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * TOM: enlaza los datos con el diseño de tarjeta de May.
 */
public final class TaskAdapter extends BaseAdapter {

    private final List<Task> items = new ArrayList<>();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void submit(List<Task> tasks) {
        items.clear();
        if (tasks != null) {
            items.addAll(tasks);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Task getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).id;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_task, parent, false);

            holder = new ViewHolder();
            holder.title = convertView.findViewById(R.id.taskTitle);
            holder.state = convertView.findViewById(R.id.taskState);
            holder.meta = convertView.findViewById(R.id.taskMeta);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Task task = getItem(position);

        holder.title.setText(task.title);
        holder.state.setText(task.state);

        String formattedDate = LocalDate.parse(task.dueDate).format(FORMATTER);
        String assignedText = (task.assignedTo == null || task.assignedTo.isEmpty()) ? "Sin asignar" : task.assignedTo;

        holder.meta.setText("Vence: " + formattedDate + " · " + assignedText);

        return convertView;
    }

    private static class ViewHolder {
        TextView title;
        TextView state;
        TextView meta;
    }
}