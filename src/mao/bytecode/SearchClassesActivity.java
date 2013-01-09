package mao.bytecode;

import java.util.ArrayList;
import java.util.List;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
public class SearchClassesActivity extends ListActivity {

    private ClassItemAdapter mAdapter;
    private static List<String> classList;




    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(classList ==null){
            classList=new ArrayList<String>();
        }
        mAdapter=new ClassItemAdapter(getApplication());
        setListAdapter(mAdapter);

    }
    @Override
    public void onListItemClick(ListView list,View v,int position,long id){
        ClassListActivity.setCurrnetClass(classList.get(position));
       Intent intent=new Intent(this,ClassItemActivity.class);
       startActivity(intent);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode,event);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        clearAll();
    }
    private void clearAll(){
        classList=null;
        mAdapter=null;
    }

    public static void initClassList(List<String> list){
        classList=list;
    }

    private class ClassItemAdapter extends BaseAdapter {

        protected final Context mContext;
        protected final LayoutInflater mInflater;

        public ClassItemAdapter(Context context) {
            mContext = context;
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return classList.size();
        }

        public Object getItem(int position) {
            return classList.get(position);
        }

        public long getItemId(int position) {
            return position;
        }



        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout container;
            if(convertView ==null){
            container= (LinearLayout) mInflater.inflate(R.layout.list_item, null);
            }else{
                container=(LinearLayout)convertView;
            }
            ImageView icon = (ImageView) container.findViewById(R.id.list_item_icon);
            icon.setImageResource(R.drawable.clazz);
            TextView text = (TextView) container.findViewById(R.id.list_item_title);
            text.setText(classList.get(position));
            return container;
        }
    }
}
