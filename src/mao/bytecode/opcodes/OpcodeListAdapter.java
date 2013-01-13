package mao.bytecode.opcodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;


public class OpcodeListAdapter extends ArrayAdapter<String> implements Filterable {

    private Filter filter;
    private LayoutInflater inflater;
    private List<String> objects, origObjects;
    
    public OpcodeListAdapter(List<String> objects, Context context) {
        super(context, android.R.layout.simple_list_item_1, objects);
        this.objects = objects;
        this.origObjects = objects;
        
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    
    public int getCount() {
        return objects.size();
    }

    public String getItem(int position) {
        return objects.get(position);
    }

    public long getItemId(int position) {
        return position;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder holder;
        
        if (convertView == null) {
            v = inflater.inflate(android.R.layout.simple_list_item_1, null);

            holder = new ViewHolder();
            holder.name = (TextView) v.findViewById(android.R.id.text1);

            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }
        
        holder.name.setText(objects.get(position));

        return v;
    }

    static class ViewHolder {
        TextView name;
    }
    
    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new ProcessListFilter();
        }

        return filter;
    }

    private class ProcessListFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults newFilterResults = new FilterResults();

            if (constraint != null && constraint.length() > 0) {
                constraint = constraint.toString().toLowerCase(Locale.ENGLISH);
                ArrayList<String> auxData = new ArrayList<String>();

                for (int i = 0; i < objects.size(); i++) {
                    String str = objects.get(i).toLowerCase(Locale.ENGLISH);  
                    if (str.contains(constraint)) {
                        auxData.add(objects.get(i));
                    }
                }

                newFilterResults.count = auxData.size();
                newFilterResults.values = auxData;
            } else {
                newFilterResults.count = origObjects.size();
                newFilterResults.values = origObjects;
            }

            return newFilterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results.count == 0) {
                notifyDataSetInvalidated();
            } else {
                objects = (List<String>) results.values;
                notifyDataSetChanged();
            }
        }

    }
}
