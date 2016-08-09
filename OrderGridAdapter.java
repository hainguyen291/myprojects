package com.example.elizaxviii.myapplication.UI.Order;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.elizaxviii.myapplication.Activities.OrderListActivity;
import com.example.elizaxviii.myapplication.R;
import com.example.elizaxviii.myapplication.RestaurantObjects.Product;
import com.example.elizaxviii.myapplication.RestaurantObjects.Utilities;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Hai Nguyen on 9/15/2015.
 */
public class OrderGridAdapter extends BaseAdapter {

    private final static int ROW_HIGHT = 250;
    //private final static int ROW_HIGHT_PHONE = 550;
    private final static int ITEM_HIGHT = 50;
    private final static int ITEM_HIGHT_PHONE = 80;
    //private final static int WAITTING = 1;
    //private final static int SERVING = 2;
    private final Activity context;
    ArrayList<String> orderTitle;
    ArrayList<Integer[]> orderQuantityList;
    ArrayList<Integer[]> orderProductIDList;
    ArrayList<Product[]> orderProductList;
    ArrayList<Integer> orderStateList;
    ArrayList<Integer> idOrderList;
    ArrayList<Integer[]> idTableList;
    private boolean isTablet;

    public ArrayList<Integer> getOrderStateList() {
        return orderStateList;
    }

    public ArrayList<Product[]> getOrderProductList() {
        return orderProductList;
    }

    public ArrayList<String> getOrderTitle() {
        return orderTitle;
    }

    public ArrayList<Integer[]> getOrderQuantityList() {
        return orderQuantityList;
    }

    private static LayoutInflater inflater = null;

    public OrderGridAdapter(Activity A, ArrayList<String> orderTitle, ArrayList<Integer[]> orderQuantityList, ArrayList<Integer[]> orderProductIDList, ArrayList<Product[]> orderProductList,
                             ArrayList<Integer> orderStateList, ArrayList<Integer> idOrderList, ArrayList<Integer[]> idTableList, boolean isTablet) {
        // TODO Auto-generated constructor stub
        this.isTablet = isTablet;
        this.orderTitle = orderTitle;
        this.orderQuantityList = orderQuantityList;
        this.orderProductIDList = orderProductIDList;
        this.orderProductList = orderProductList;
        this.orderStateList = orderStateList;
        this.idOrderList = idOrderList;
        this.idTableList = idTableList;
        context = A;

        inflater = (LayoutInflater) context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return orderTitle.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder {
        TextView title;
        ListView product;
        TextView discount;
        TextView total;
    }

    private float getTotalOfOrder(int position)
    {
        float total = 0;
        for (int i=0; i<orderQuantityList.get(position).length; i++)
            total += orderQuantityList.get(position)[i]
                    * orderProductList.get(position)[i].getProductPrice();

        return total;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        final Holder holder = new Holder();

        View mView = inflater.inflate(R.layout.order_state, parent, false);
        holder.title = (TextView) mView.findViewById(R.id.textTableName);
        holder.product = (ListView) mView.findViewById(R.id.listView);
        holder.discount = (TextView) mView.findViewById(R.id.textDiscount);
        holder.total = (TextView) mView.findViewById(R.id.textTotal);

        holder.title.setText(orderTitle.get(position));
        holder.total.setText(Float.toString(getTotalOfOrder(position)));

        // Set height of order state item
        if (isTablet)
            mView.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, (getMaxItems()*ITEM_HIGHT + ROW_HIGHT)));
        else
            mView.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, (getMaxItems()*ITEM_HIGHT_PHONE + ROW_HIGHT + 250)));
        //1. Initiate data source
        //2. Get listview object via id
        //3. Set Data source for ArrayAdapter
        OrderItemAdapter adapter = new OrderItemAdapter(context, new ArrayList<Integer>(Arrays.asList(orderQuantityList.get(position))),
                new ArrayList<Product>(Arrays.asList(orderProductList.get(position))), new ArrayList<Integer>(Arrays.asList(orderProductIDList.get(position))), idOrderList.get(position));
        //4. Add Data source in ListView
        holder.product.setAdapter(adapter);
        adapter.setListView(holder.product);
        //int l = holder.product.getAdapter().getCount();
        //5. Set an event when touch on listviewitem
        holder.product.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> arg0,
                                            View arg1,
                                            int arg2,
                                            long arg3) {
                        //Show the position and value in Data Source (arr)
                        String st = "";
                        for (int i = 0; i < holder.product.getAdapter().getCount(); i++) {
                            st = st + " ;" + orderProductList.get(position)[i].getProductName();
                        }
                        Toast.makeText(context, st,
                                Toast.LENGTH_SHORT).show();
                    }
                });

        // Set color as state of orders
        /*switch (orderStateList.get(position))
        {
            case 1:
                mView.setBackgroundColor(context.getResources().getColor(R.color.ColorGreen));
                break;
            case 2:
                mView.setBackgroundColor(context.getResources().getColor(R.color.ColorOrange));
                break;
            default:
                //mView.setBackgroundColor(context.getResources().getColor(R.color.ColorGreen));
        }*/

        mView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //Do stuff here
                Intent intent = new Intent(context, OrderListActivity.class);
                //Create the bundle
                Bundle bundle = new Bundle();

                //Add your data to bundle
                bundle.putInt(OrderListActivity.CURRENT_ORDER, idOrderList.get(position));
                int[] temp = new int[idTableList.get(position).length];
                for (int i=0; i<idTableList.get(position).length; i++)
                    temp[i] = idTableList.get(position)[i];
                bundle.putIntArray(OrderListActivity.CURRENT_TABLES, temp);

                //Add the bundle to the intent
                intent.putExtras(bundle);

                //Fire that second activity
                context.startActivity(intent);
            }
        });

        return mView;
    }

    private int getMaxItems(){
        int max = -1;
        if (orderProductList.size() > 0)
            for (Product[] p: orderProductList)
                if (max < p.length)
                    max = p.length;
        return max;
    }
}
