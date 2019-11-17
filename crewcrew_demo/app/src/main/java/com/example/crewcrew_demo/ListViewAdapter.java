package com.example.crewcrew_demo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class ListViewAdapter extends BaseAdapter {

    private ArrayList<ListViewItem> listViewItemList = new ArrayList<ListViewItem>();

    public ListViewAdapter(){

    }
    public int getCount(){
        return listViewItemList.size();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_item, parent, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        TextView nameTextView = (TextView)convertView.findViewById(R.id.crewName);
        TextView levelTextView = (TextView)convertView.findViewById(R.id.crewLevel);
        TextView numTextView = (TextView)convertView.findViewById(R.id.crewNum);
        TextView runTimeTextView = (TextView)convertView.findViewById(R.id.crewRuntime);
        Button contactButton = (Button)convertView.findViewById(R.id.contactBtn);
        Button joinButton = (Button)convertView.findViewById(R.id.joinBtn);

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        ListViewItem listViewItem = listViewItemList.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        nameTextView.setText(listViewItem.getCrewName());
        levelTextView.setText(listViewItem.getCrewLevel());
        numTextView.setText(listViewItem.getCrewNum());
        runTimeTextView.setText(listViewItem.getCrewRuntime());

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position ;
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    @Override
    public Object getItem(int position) {
        return listViewItemList.get(position) ;
    }

    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
    public void addItem(String name, String level, String num, String runtime) {
        ListViewItem item = new ListViewItem();

        item.setCrewName(name);
        item.setCrewLevel(level);
        item.setCrewNum(num);
        item.setCrewRuntime(runtime);

        listViewItemList.add(item);
    }
}
