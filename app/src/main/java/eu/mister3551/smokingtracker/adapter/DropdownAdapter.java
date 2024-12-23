package eu.mister3551.smokingtracker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import eu.mister3551.smokingtracker.R;

public class DropdownAdapter extends android.widget.ArrayAdapter<CharSequence> {

    private final Context context;
    private final CharSequence[] items;

    public DropdownAdapter(Context context, int resource, CharSequence[] items) {
        super(context, resource, items);
        this.context = context;
        this.items = items;
    }

    private static class ViewHolder {
        TextView textViewOption;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, parent);
    }

    private View getCustomView(int position, ViewGroup parent) {
        View convertView = LayoutInflater.from(context).inflate(R.layout.fragment_settings_dropdown, parent, false);
        ViewHolder viewHolder = new ViewHolder();

        viewHolder.textViewOption = convertView.findViewById(R.id.text_view_option);
        convertView.setTag(viewHolder);

        viewHolder.textViewOption.setText(items[position]);

        return convertView;
    }
}