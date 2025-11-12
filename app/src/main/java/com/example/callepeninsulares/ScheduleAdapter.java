package com.example.callepeninsulares;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class ScheduleAdapter extends BaseAdapter {

    private Context context;
    private List<Schedule> scheduleList;

    public ScheduleAdapter(Context context, List<Schedule> scheduleList) {
        this.context = context;
        this.scheduleList = scheduleList;
    }

    @Override
    public int getCount() {
        return scheduleList.size();
    }

    @Override
    public Object getItem(int position) {
        return scheduleList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return scheduleList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.schedule_item, parent, false);
        }

        Schedule schedule = scheduleList.get(position);

        TextView subject = convertView.findViewById(R.id.textSubject);
        TextView building = convertView.findViewById(R.id.textBuilding);
        TextView date = convertView.findViewById(R.id.textDate);
        TextView time = convertView.findViewById(R.id.textTime);
        TextView room = convertView.findViewById(R.id.textRoom);
        TextView reminder = convertView.findViewById(R.id.textReminder);


        subject.setText(schedule.getSubject());



        if (schedule.isOnlineClass()) {
            building.setText("Online Class");
            room.setVisibility(View.GONE);
        } else {
            building.setText(schedule.getBuilding());
            room.setText("Room: " + schedule.getRoom());
        }


        date.setText(schedule.getDate());
        time.setText(schedule.getStartTime() + " - " + schedule.getEndTime());



        if (schedule.getMinutesBefore() > 0) {
            reminder.setVisibility(View.VISIBLE);
            reminder.setText("Reminder: " + schedule.getMinutesBefore() + " min early");
        } else {
            reminder.setVisibility(View.GONE);
        }

        return convertView;
    }
}
