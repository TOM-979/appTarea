package com.senati.apptarea;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public final class TaskAdapter extends BaseAdapter {

    private final List<Task> items = new ArrayList<>();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void submit(List<Task> tasks) {
        items.clear();
        if (tasks != null) items.addAll(tasks);
        notifyDataSetChanged();
    }

    @Override public int getCount() { return items.size(); }
    @Override public Task getItem(int position) { return items.get(position); }
    @Override public long getItemId(int position) { return getItem(position).id; }
    @Override public boolean hasStableIds() { return true; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_task, parent, false);
            holder = new ViewHolder();
            holder.title = convertView.findViewById(R.id.taskTitle);
            holder.chipContainer = convertView.findViewById(R.id.stateChip);
            holder.stateIcon = convertView.findViewById(R.id.taskStateIcon);
            holder.state = convertView.findViewById(R.id.taskState);
            holder.meta = convertView.findViewById(R.id.taskMeta);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Task task = getItem(position);
        Context context = convertView.getContext();

        holder.title.setText(task.title);
        holder.state.setText(task.state);

        LocalDate due = LocalDate.parse(task.dueDate);
        boolean overdue = due.isBefore(LocalDate.now()) && !Task.STATES[2].equals(task.state);

        applyStateStyle(context, holder, task.state, overdue);

        String assignedText = (task.assignedTo == null || task.assignedTo.isEmpty()) ? "Sin asignar" : task.assignedTo;
        holder.meta.setText(relativeDate(due, overdue) + " · " + assignedText);
        holder.meta.setTextColor(ContextCompat.getColor(context, overdue ? R.color.overdue_text : R.color.onsen_text_secondary));

        return convertView;
    }

    private void applyStateStyle(Context context, ViewHolder holder, String state, boolean overdue) {
        int cardBg, chipBg, iconRes;

        if (Task.STATES[0].equals(state)) { // Pendiente
            cardBg = overdue ? R.drawable.bg_card_pending_overdue : R.drawable.bg_card_pending;
            chipBg = R.drawable.bg_chip_pending;
            iconRes = R.drawable.ic_pending_dot;
        } else if (Task.STATES[1].equals(state)) { // En progreso
            cardBg = overdue ? R.drawable.bg_card_progress_overdue : R.drawable.bg_card_progress;
            chipBg = R.drawable.bg_chip_progress;
            iconRes = R.drawable.ic_clock_outline;
        } else { // Completada
            cardBg = R.drawable.bg_card_done;
            chipBg = R.drawable.bg_chip_done;
            iconRes = R.drawable.ic_check_circle;
        }

        ((View) holder.chipContainer.getParent()).setBackgroundResource(cardBg);
        holder.chipContainer.setBackgroundResource(chipBg);
        holder.stateIcon.setImageResource(iconRes);
    }

    private String relativeDate(LocalDate due, boolean overdue) {
        long days = Math.abs(ChronoUnit.DAYS.between(LocalDate.now(), due));
        if (overdue) return "Vencida hace " + days + (days == 1 ? " día" : " días");
        if (days == 0) return "Vence: Hoy";
        if (days == 1) return "Vence: Mañana";
        return "Vence: " + FORMATTER.format(due);
    }

    private static class ViewHolder {
        TextView title, state, meta;
        LinearLayout chipContainer;
        ImageView stateIcon;
    }
}