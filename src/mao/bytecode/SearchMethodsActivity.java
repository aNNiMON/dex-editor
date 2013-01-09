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
public class SearchMethodsActivity extends ListActivity {

    private MethodItemAdapter mAdapter;
    private static List<String> methodList;
    private static List<Boolean> isDirectes;
    private static List<Integer> methodIndexes;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(methodList ==null){
            methodList=new ArrayList<String>();
        }
        mAdapter=new MethodItemAdapter(getApplication());
        setListAdapter(mAdapter);

    }
    @Override
    public void onListItemClick(ListView list,View v,int position,long id){
       MethodListActivity.setMethodIndex(isDirectes.get(position),methodIndexes.get(position));
       Intent intent=new Intent(this,CodeEditorActivity.class);
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
        methodList=null;
        isDirectes=null;
        methodIndexes=null;
        mAdapter=null;
    }

    public static void initMethodList(List<String> list,List<Boolean> isDirect,List<Integer> methodIndex){
        methodList=list;
        isDirectes=isDirect;
        methodIndexes=methodIndex;
    }

    private class MethodItemAdapter extends BaseAdapter {

        protected final Context mContext;
        protected final LayoutInflater mInflater;

        public MethodItemAdapter(Context context) {
            mContext = context;
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return methodList.size();
        }

        public Object getItem(int position) {
            return methodList.get(position);
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
            icon.setImageResource(R.drawable.method);
            TextView text = (TextView) container.findViewById(R.id.list_item_title);
            text.setText(methodList.get(position));
            return container;
        }
    }
}
