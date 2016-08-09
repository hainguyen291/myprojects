package com.example.elizaxviii.myapplication.UI.Order;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.elizaxviii.myapplication.Activities.JSONParser;
import com.example.elizaxviii.myapplication.Activities.OrderState;
import com.example.elizaxviii.myapplication.Activities.SplitBillActivity;
import com.example.elizaxviii.myapplication.Configuration.Lib;
import com.example.elizaxviii.myapplication.R;
import com.example.elizaxviii.myapplication.RestaurantObjects.MyApp;
import com.example.elizaxviii.myapplication.RestaurantObjects.Order;
import com.example.elizaxviii.myapplication.RestaurantObjects.OrderHasProduct;
import com.example.elizaxviii.myapplication.RestaurantObjects.OtherProduct;
import com.example.elizaxviii.myapplication.RestaurantObjects.Product;
import com.example.elizaxviii.myapplication.RestaurantObjects.ProductCombo;
import com.example.elizaxviii.myapplication.RestaurantObjects.ProductHasComment;
import com.example.elizaxviii.myapplication.RestaurantObjects.Sections;
import com.example.elizaxviii.myapplication.RestaurantObjects.Table;
import com.example.elizaxviii.myapplication.RestaurantObjects.TableHasOrder;
import com.example.elizaxviii.myapplication.RestaurantObjects.Utilities;
import com.example.elizaxviii.myapplication.SQLite.OrderHandler;
import com.example.elizaxviii.myapplication.SQLite.OrderHasProductHandler;
import com.example.elizaxviii.myapplication.SQLite.ProductComboHandler;
import com.example.elizaxviii.myapplication.SQLite.ProductHandler;
import com.example.elizaxviii.myapplication.SQLite.ProductHasCommentHandler;
import com.example.elizaxviii.myapplication.SQLite.SectionHandler;
import com.example.elizaxviii.myapplication.SQLite.TableHandler;
import com.example.elizaxviii.myapplication.SQLite.TableHasOrderHandler;
import com.example.elizaxviii.myapplication.UI.Restaurant.BloothPrinterActivity;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class OrderListFragment extends Fragment {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    public static final String PRODUCT_ID = "product_id";

    /**
     * The current activated item position. Only used on tablets.
     */
    private final int CANCEL_ORDER = -1;
    private final int AVAILABLE_TABLE = 5;
    public static final int ID_ORDER = 0;
    public static final int TIME = 1;
    public static final int ORDER_STATE = 2;
    public static final int NUM_OF_CUSTOMER = 3;
    public static final int DISCOUNT = 4;
    public static final int ID_RES = 5;
    public static final int TABLE_ID_TABLE = 0;
    public static final int ORDER_ID_ORDER = 1;
    public static final int PRODUCT_ID_PRODUCT = 1;
    public static final int PRODUCT_QUANTITY = 2;
    public static final int PRODUCT_PRICE = 3;
    public static final int PRODUCT_STATE = 4;
    public static final int PRODUCT_BILL = 5;
    public static final int PRODUCT_COMMENT = 6;

    private int mActivatedPosition = ListView.INVALID_POSITION;
    public int idOrder = -1;
    public int[] idTables;
    private String orderURL= "", orderHasProductURL= "", orderHasTableURL= "", orderStateURL="", tableorderupdateURL = "";;
    private String tableURL= "";
    private EditText editNumOfCustomer;
    private ArrayList<HashMap<String, String>> tableorderInf=new ArrayList<HashMap<String, String>>();
    private JSONArray ordersJson = null;
    private JSONArray tablesJson = null;
    private JSONObject currentOrder = null;
    private ArrayList<Table> tableList = new ArrayList<Table>();
    private ArrayList<String> orderTitle = new ArrayList<String>();
    private ArrayList<Integer[]> orderQuantityList = new ArrayList<Integer[]>();
    private ArrayList<Product[]> orderProductList = new ArrayList<Product[]>();
    private ArrayList<Integer> orderStateList = new ArrayList<Integer>();
    private ArrayList<Order> idOrderList = new ArrayList<Order>();
    private int flag = -1, maxIDProduct = 0, numOfCus = 0;
    private ListView lvProduct;
    private Spinner spnTables;
    private Button btnSave, btnPrint, btnDelete;
    private MyApp curOrder;
    private boolean isPrint = false;

    // For found product layout
    private EditText editNewProduct;
    private ImageButton btnAdd;
    private ArrayList<Product> foundProductList = new ArrayList<>();
    private ListView lvFoundProduct;
    private RelativeLayout rltFound;
    private Product foundItem = null;
    private Button btnClose;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public OrderListFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // khoi tao duong dan
        SharedPreferences prefs= getActivity().getSharedPreferences("server_conf", Context.MODE_PRIVATE);
        String servername = prefs.getString("servername", "");
        if (servername!="")
        {
            orderURL = servername + "order/order/";
            orderStateURL = servername + "order/orderState/2911984";
            tableURL = servername + "order/tables/";
            orderHasProductURL = servername + "order/orderHasProduct/";
            orderHasTableURL = servername + "order/tableHasOrder/";
            tableorderupdateURL = servername + "tables/tableorder/";
        }
        else
        {
            Log.e("server name error: ", " server not found");
        }
    }

    /**
     * Initialize Views
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_order_list, container, false);
        curOrder = (MyApp) getActivity().getApplicationContext();
        // Popup 
        final popupHolder mPopupStuff = new popupHolder();
        final PopupWindow popUp;
        View popView = inflater.inflate(R.layout.note, null, true);
        popUp = new PopupWindow(rootView.getContext());
        popUp.setContentView(popView);
        popUp.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        popUp.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);

        mPopupStuff.btnSave = (Button) popView.findViewById(R.id.btnSave);
        mPopupStuff.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                curOrder.setOrderComment(mPopupStuff.edtNote.getText().toString());
                popUp.dismiss();
            }
        });
        mPopupStuff.btnCancel = (Button) popView.findViewById(R.id.btnCancel);
        mPopupStuff.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popUp.dismiss();
            }
        });

        mPopupStuff.btnClose = (Button) popView.findViewById(R.id.btnClose);
        mPopupStuff.btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popUp.dismiss();
            }
        });
        mPopupStuff.edtNote = (EditText) popView.findViewById(R.id.edtNote);

        // Get found layout
        rltFound = (RelativeLayout) rootView.findViewById(R.id.relFound);
        btnClose = (Button) rootView.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rltFound.setVisibility(View.GONE);
            }
        });
        lvFoundProduct = (ListView) rootView.findViewById(R.id.lvFoundProduct);
        editNumOfCustomer = (EditText) rootView.findViewById(R.id.editNumOfCustomer);
        editNumOfCustomer.setText("0"); // default value

        // Show content of order list view
        btnAdd = (ImageButton) rootView.findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (foundItem != null)
                    refreshProductItems(foundItem);
            }
        });
        editNewProduct = (EditText) rootView.findViewById(R.id.editNewProduct);
        editNewProduct.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                rltFound.setVisibility(View.VISIBLE);
                // Reset idOfFoundItem
                foundItem = null;
                updateFoundProductList(editNewProduct.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        lvProduct = (ListView) rootView.findViewById(R.id.lvProduct);
        lvProduct.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                int i = -1;
            }
        });
        btnDelete = (Button) rootView.findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (idOrder != Integer.MAX_VALUE)
                    new postCancelOrder().execute(new String[] {String.valueOf(idOrder), String.valueOf(CANCEL_ORDER)});
            }
        });
        btnSave = (Button) rootView.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveOrders(rootView);
            }
        });
        btnPrint = (Button) rootView.findViewById(R.id.btnPrint);
        btnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPrint = true;
                saveOrders(rootView);
            }
        });
        spnTables = (Spinner) rootView.findViewById(R.id.spnTables);

        return rootView;
    }

    /**
     * Print bill and set it to paid
     */
    private void printOrders()
    {
        // Set order to paid
        payOrder();

        // Then print bill
        Intent intent = new Intent(getActivity().getBaseContext(),BloothPrinterActivity.class);
        intent.putExtra("order",tableorderInf);
        startActivity(intent);
    }

    /**
     * Set order to paid state
     */
    private void payOrder()
    {
        if(idOrder != -1 && idOrder != Integer.MAX_VALUE) {
            if( Utilities.isNetworkAvailable(getActivity())) {
                String userID = Lib.getInfos(getActivity(), "usr_id");
                new postTableOrder().execute(new String[]{String.valueOf(idOrder), "3", userID});// 3 mean paid state
                Toast.makeText(getActivity(), "Payment complete", Toast.LENGTH_LONG).show();
            }
            else
            {
                // update local database
                String userID = Lib.getInfos(getActivity(), "usr_id");
                OrderHandler orderHandler = new OrderHandler(getActivity());
                Order currorder=  orderHandler.getOrder(idOrder);
                currorder.setOrderState(3);
                //currorder.setIdStaff(Integer.valueOf(userID));
                orderHandler.updateOrder(currorder);
                Toast.makeText(getActivity(), "Payment complete ", Toast.LENGTH_LONG).show();
            }
        }
        else
        {
            Toast.makeText(getActivity(), "No bill is chosen!", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Save order's change
     * @param rootView
     */
    private void saveOrders(View rootView)
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();

        flag = 1; // Save order
        idTables = null;
        if (spnTables.getCount() > 0 && spnTables.getSelectedItemPosition() != 0)
            idTables = new int[]{tableList.get(spnTables.getSelectedItemPosition() - 1).getIdTable()};
        else
            idTables = new int[]{0};

        // Insert new record
        if (idOrder == Integer.MAX_VALUE)
            new postOrder().execute(new String[] {"-1", dateFormat.format(date), "1",
                    Lib.getInfos(getActivity(),"idrestaurant"), String.valueOf(idTables[0]),
                    editNumOfCustomer.getText().toString()});
        else
            // Update current record
            new postOrder().execute(new String[] {Integer.toString(idOrder),
                    dateFormat.format(idOrderList.get(0).getOrderTime()),
                    Integer.toString(idOrderList.get(0).getOrderState()),
                    Lib.getInfos(getActivity(),"idrestaurant"), String.valueOf(idTables[0]),
                    editNumOfCustomer.getText().toString()});
    }

    /**
     * Update results
     * @param productName
     */
    private void updateFoundProductList(String productName)
    {
        List<Product> temps = new ProductHandler(getActivity()).getAllProducts();
        final ArrayList<Product> result = new ArrayList<>();
        for (Product p: temps) {
            if (p.getProductName().toLowerCase().contains(productName.toLowerCase()))
                result.add(p);
        }

        // Refresh lvFoundProduct
        lvFoundProduct.setAdapter(new FoundItemAdapter(getActivity(), result));
        lvFoundProduct.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editNewProduct.setText(result.get(position).getProductName());
                foundItem = result.get(position);
                rltFound.setVisibility(View.GONE);
                editNewProduct.requestFocus();
            }
        });
    }

    /**
     * Show popup to update note for order
     */
    public class ItemOnClickListener implements View.OnClickListener
    {
        private PopupWindow popup = new PopupWindow();
        private popupHolder mHolder;
        private View mView;
        public ItemOnClickListener(PopupWindow mPop, View mView, popupHolder mHolder, String comment)
        {
            this.popup = mPop;
            this.mView = mView;
            this.mHolder = mHolder;
            this.mHolder.edtNote.setText(curOrder.getOrderComment());
        }
        @Override
        public void onClick(View v) {
            popup.showAtLocation(mView, Gravity.CENTER, 10, 10);
            popup.setFocusable(true); // To show keyboard
            popup.update();
        }
    };

    /**
     * popup elements
     */
    private class popupHolder {
        // Popup's stuff
        Button btnSave, btnCancel, btnClose;
        EditText edtNote;
    }

    /**
     * Get view of listview item
     * @param pos
     * @param listView
     * @return element's view
     */
    private View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    /**
     * Check if this product has been existed on order
     * @param pro
     * @return position of this product or -1 if not found
     */
    private int isExistedProduct(Product pro)
    {
        if (curOrder.getOrderProductList().size() > 0)
            for (int i=0; i<curOrder.getOrderProductList().size(); i++)
                if (curOrder.getOrderProductList().get(i).getIdProduct() == pro.getIdProduct())
                    return i;

        return -1;
    }

    /**
     * Update order
     * @param pro
     */
    public void refreshProductItems(Product pro)
    {
        //curOrder.setOrderQuantityList(new ArrayList<Integer>());
        //curOrder.setOrderProductList(new ArrayList<Product>());
        // Check if this product is existed in order list or not
        /*if (orderProductList.size() > 0)
        {
            curOrder.setOrderProductList(new ArrayList<Product>(Arrays.asList(orderProductList.get(0))));
            curOrder.setOrderQuantityList(new ArrayList<Integer> (Arrays.asList(orderQuantityList.get(0))));
        }*/

        // Add new product into order
        if (orderProductList.size() > 0) {
            orderProductList.remove(0);
            orderQuantityList.remove(0);
            //orderProductDiscountList.remove(0);
        }
        // Insert a new product into order list
        // Check if this pro is a combo product
        OtherProduct newPro = new OtherProduct(pro, null);
        ProductComboHandler combo = new ProductComboHandler(getActivity());
        List<OtherProduct> list = combo.getAllComboProduct(pro.getIdProduct());
        // if it is a combo, then behave as a combo.
        if (list != null && list.size()>0)
            newPro.setSubProducts((ArrayList<OtherProduct>)list);
        curOrder.getOrderProductList().add(newPro);
        // update Product, Quantity, ID, IDComment list
        // orderProductIDCommentList
        ProductHasCommentHandler commentHandler = new ProductHasCommentHandler(getActivity());
        ProductHasComment idx = commentHandler.getProductHasComment(newPro.getIdProduct(),
                Integer.valueOf(Lib.getInfos(getActivity(), "idrestaurant")));
        if (idx != null)
            curOrder.getOrderProductIDCommentList().add(idx.getIdComment());
        else
            // Default comment
            curOrder.getOrderProductIDCommentList().add(1);
        // update Product list
        Product[] mp = new Product[curOrder.getOrderProductList().size()];
        mp = curOrder.getOrderProductList().toArray(mp);
        orderProductList.add(mp);
        // update Quantity list
        curOrder.getOrderQuantityList().add(1);
        Integer[] mq = new Integer[curOrder.getOrderQuantityList().size()];
        mq = curOrder.getOrderQuantityList().toArray(mq);
        orderQuantityList.add(mq);
        // re-indexing orderProductIDList
        maxIDProduct += 1;
        curOrder.getOrderProductIDList().add(maxIDProduct);

//        int currentIndex = isExistedProduct(pro);
//        if (currentIndex != -1)
//        {
//            // Increase quantity 1
//            curOrder.getOrderQuantityList().set(currentIndex, curOrder.getOrderQuantityList().get(currentIndex) + 1);
//            orderQuantityList.remove(0);
//            orderProductDiscountList.remove(0);
//            Integer[] mq = new Integer[curOrder.getOrderQuantityList().size()];
//            Integer[] md = new Integer[curOrder.getOrderDiscountList().size()];
//            mq = curOrder.getOrderQuantityList().toArray(mq);
//            md = curOrder.getOrderDiscountList().toArray(md);
//            orderQuantityList.add(mq);
//            orderProductDiscountList.add(md);
//        }
//        else
//        {
//            if (orderProductList.size() > 0)
//            {
//                orderProductList.remove(0);
//                orderQuantityList.remove(0);
//                orderProductDiscountList.remove(0);
//            }
//            // Insert a new product into order list
//            curOrder.getOrderProductList().add(new OtherProduct(pro, null));
//            Product[] mp = new Product[curOrder.getOrderProductList().size()];
//            mp = curOrder.getOrderProductList().toArray(mp);
//            orderProductList.add(mp);
//            curOrder.getOrderQuantityList().add(1);
//            curOrder.getOrderDiscountList().add(0);
//            Integer[] mq = new Integer[curOrder.getOrderQuantityList().size()];
//            Integer[] md = new Integer[curOrder.getOrderQuantityList().size()];
//            mq = curOrder.getOrderQuantityList().toArray(mq);
//            md = curOrder.getOrderDiscountList().toArray(md);
//            orderQuantityList.add(mq);
//            orderProductDiscountList.add(md);
//        }

        // refresh order list
        // set data to list adapter and list.
        if (orderQuantityList.size() > 0)
            lvProduct.setAdapter(new OtherOrderItemAdapter(getActivity()));
        //lvProduct.getAdapter().notify();
    }

    /**
     * Process order state in offline scenior
     */
    private void updateCancelRecords()
    {
        if (curOrder == null)
        {
            curOrder.setIsOfflineMode(true);
            flag = 1; // Update order
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            try {
                Order updatedOrder;
                OrderHandler mOrHandler = new OrderHandler(getActivity());
                TableHandler mTable = new TableHandler(getActivity());
                // Insert new record
                if (idOrder != Integer.MAX_VALUE) {
                    // Get idTable first
                    int idTable = -1;
                    if (spnTables.getCount() > 0 && spnTables.getSelectedItemPosition() != 0)
                        idTable = tableList.get(spnTables.getSelectedItemPosition() - 1).getIdTable();
                    // Update table's State to AVAILABLE
//                    if (idTable != -1)
//                        mTable.updateStateTable(idTable, AVAILABLE_TABLE);
                    // Update current record
                    String s = dateFormat.format((idOrderList.get(0).getOrderTime()));
                    updatedOrder = new Order(idOrder, dateFormat.parse(s), CANCEL_ORDER,
                            idTable, Integer.parseInt(Lib.getInfos(getActivity(), "idrestaurant")),
                            Integer.valueOf(editNumOfCustomer.getText().toString()),0);
                    mOrHandler.updateOrder(updatedOrder);
                }
            }
            catch (ParseException e){}
        }

        // Back to order state
        Intent intent = new Intent(getActivity(), OrderState.class);
        startActivity(intent);
    }

    /**
     * update orderHasProduct and tableHasOrder tables, which is related to currentOrder
     */
    private void updateRelatedRecords()
    {
        // If update order succesfully, then update 2 related tables (Online scenior)
        if (currentOrder != null)
        {
            try
            {
                //currentOrder.setIsOfflineMode(false);
                // ===============================
                flag = 2; // Delete records related to current order
                new postOrder().execute(new String[] {currentOrder.get("idOrder").toString()});
                // ===============================

                // Update table_has_order first
                // Reset idTables
                idTables = null;
                // Update order_has_product
                for (int i=0; i<lvProduct.getCount(); i++)
                {
                    // Get info of related item
                    View v = getViewByPosition(i, lvProduct);
                    TextView ed = (TextView) v.findViewById(R.id.txtQuantity);
                    //TextView dc = (TextView) v.findViewById(R.id.txtDiscount);
                    TextView tvID = (TextView) v.findViewById(R.id.txtIDProduct);
                    TextView tvPrice = (TextView) v.findViewById(R.id.txtPrice);

                    // Update its change
                    new postProduct().execute(new String[] {String.valueOf(curOrder.getOrderProductIDList().get(i)),
                            currentOrder.get("idOrder").toString(),
                            tvID.getText().toString(),
                            ed.getText().toString(),
                            tvPrice.getText().toString(),
                            String.valueOf(curOrder.getOrderProductList().get(i).getProductState()),
                            "null", curOrder.getOrderProductIDCommentList().get(i).toString()});
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
        // Offline scenior
        else
        {
            curOrder.setIsOfflineMode(true);
            flag = 1; // Save order
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            try
            {
                Order updatedOrder;
                OrderHandler mOrHandler = new OrderHandler(getActivity());
                TableHandler mTables = new TableHandler(getActivity());
                OrderHasProductHandler mProducts = new OrderHasProductHandler(getActivity());
                TableHasOrderHandler mTableHasOrders = new TableHasOrderHandler(getActivity());
                // Insert new record
                if (idOrder == Integer.MAX_VALUE) {
                    // Get idTable first
                    int idTable = -1;
                    if (spnTables.getCount() > 0 && spnTables.getSelectedItemPosition() != 0)
                        idTable = tableList.get(spnTables.getSelectedItemPosition() - 1).getIdTable();
                    // Update current record
                    String s = dateFormat.format(new Date());
                    updatedOrder = new Order(mOrHandler.getMaxIDOrders(1) + 100, dateFormat.parse(dateFormat.format(date)), 1,
                            idTable, Integer.parseInt(Lib.getInfos(getActivity(), "idrestaurant")),
                            Integer.valueOf(editNumOfCustomer.getText().toString()),1);
                    mOrHandler.addOrder(updatedOrder);
                    // Update table's State to order's state
//                    if (idTable != -1)
//                        mTables.updateStateTable(updatedOrder.getOrderState(), 1);
                }
                else {
                    // Update current record
                    String s = dateFormat.format((idOrderList.get(0).getOrderTime()));
                    // Get idTable first
                    int idTable = -1;
                    if (spnTables.getCount() > 0 && spnTables.getSelectedItemPosition() != 0)
                        idTable = tableList.get(spnTables.getSelectedItemPosition() - 1).getIdTable();
                    // Update current record
                    if (idOrderList.get(0).getIsLocalData() == 1)
                        updatedOrder = new Order(idOrder, dateFormat.parse(s), 1,
                                idTable, Integer.parseInt(Lib.getInfos(getActivity(), "idrestaurant")),
                                Integer.valueOf(editNumOfCustomer.getText().toString()),1);
                    else
                        updatedOrder = new Order(idOrder, dateFormat.parse(s), 1,
                                idTable, Integer.parseInt(Lib.getInfos(getActivity(), "idrestaurant")),
                                Integer.valueOf(editNumOfCustomer.getText().toString()),0);
                    mOrHandler.updateOrder(updatedOrder);
                    // Update table's State to order's state
                    if (idTable != -1)
                        mTables.updateStateTable(updatedOrder.getOrderState(), 1);
                }

                flag = 2; // Delete records related to current order
                mProducts.deleteOrderExceptPaidOne(updatedOrder.getIdOrder());

                // Update order_has_product
                for (int i=0; i<lvProduct.getCount(); i++)
                {
                    // Get info of related item
                    View v = getViewByPosition(i, lvProduct);
                    TextView ed = (TextView) v.findViewById(R.id.txtQuantity);
                    TextView cm = (TextView) v.findViewById(R.id.txtDiscount); // Have to change this to txtComment
                    TextView tvID = (TextView) v.findViewById(R.id.txtIDProduct);
                    TextView tvPrice = (TextView) v.findViewById(R.id.txtPrice);

                    // Update its change
                    mProducts.addOrderProduct(new OrderHasProduct(curOrder.getOrderProductIDList().get(i),
                            updatedOrder.getIdOrder(),
                            Integer.valueOf(tvID.getText().toString()),
                            Integer.valueOf(ed.getText().toString()),
                            Float.valueOf(tvPrice.getText().toString()), -1,
                            curOrder.getOrderProductList().get(i).getProductState(),
                            curOrder.getOrderProductIDCommentList().get(i)));
                }
            }
            catch (ParseException e){ }
        }

        // Update printer info
        splitBill();
    }

    /**
     * Get data related to order at the beginning
     */
    public void getData() {
        orderURL = orderURL + Integer.toString(idOrder);
        tableURL = tableURL + Lib.getInfos(getActivity(), "idrestaurant"); // available tables
        //get data
        new getOrders(getActivity()).execute(orderURL);
        new getAvailableTables(getActivity()).execute(Lib.getInfos(getActivity(),"idrestaurant"));

        // ===================================
    }

    /**
     * Update order when save button is clicked
     */
    public void setListData() {
        // Has connection
        if (ordersJson != null)
            try
            {
                for (int i=0; i<ordersJson.length(); i++)
                {
                    JSONObject order_item = ordersJson.getJSONObject(i);
                    JSONArray orderTable = order_item.getJSONArray("table");
                    numOfCus = order_item.getInt("numOfCustomer");

                    if (orderTable != null && orderTable.length() > 0)
                    {
                        String title;
                        title = "Order: " + order_item.getString("idOrder") + " - Table: ";
                        for (int k=0; k<orderTable.length(); k++) {
                            JSONObject table = orderTable.getJSONObject(k);
                            title = title + table.get("tableName");
                        }

                        orderTitle.add(title);
                    }
                    else
                        orderTitle.add("Order: " + order_item.get("idOrder").toString());

                    if (order_item.get("orderState").toString() != "null")
                        orderStateList.add(Integer.valueOf(order_item.get("orderState").toString()));
                    else
                        orderStateList.add(-1);

                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    if (idOrder != Integer.MAX_VALUE)
                        try
                        {
                            if (!order_item.isNull("idTable"))
                                idOrderList.add(new Order(Integer.valueOf(order_item.getString("idOrder")),
                                        dateFormat.parse(order_item.getString("orderTime")),
                                        Integer.valueOf(order_item.getString("orderState")),
                                        Integer.valueOf(order_item.getString("idTable")),
                                        Integer.valueOf(order_item.getString("idRestaurant")),
                                        Integer.valueOf(order_item.getString("numOfCustomer")), 0));
                            else
                                idOrderList.add(new Order(Integer.valueOf(order_item.getString("idOrder")),
                                        dateFormat.parse(order_item.getString("orderTime")),
                                        Integer.valueOf(order_item.getString("orderState")), 0,
                                        Integer.valueOf(order_item.getString("idRestaurant")),
                                        Integer.valueOf(order_item.getString("numOfCustomer")), 0));
                        }
                        catch (ParseException e)
                        {
                            e.printStackTrace();
                        }

                    JSONArray orderProduct = order_item.getJSONArray("products");
                    maxIDProduct = order_item.getInt("MaxIDProduct");
                    if (orderProduct != null)
                    {
                        Integer[] orderQuantityL = new Integer[orderProduct.length()];
                        Integer[] orderIDL = new Integer[orderProduct.length()];
                        Integer[] orderIDC = new Integer[orderProduct.length()];
                        OtherProduct[] orderProductL = new OtherProduct[orderProduct.length()];
                        for (int j=0; j<orderProduct.length(); j++)
                        {
                            JSONObject item = orderProduct.getJSONObject(j);
                            //maxIDProduct = item.getInt("Max");
                            orderQuantityL[j] = item.getInt("productQuantity");
                            orderIDL[j] = item.getInt("idProduct");
                            orderIDC[j] = item.getInt("comments");
                            //orderDiscountL[j] = Integer.valueOf(item.get("productDiscount").toString());
                            orderProductL[j] = new OtherProduct(
                                    item.getInt("product_idProduct"),
                                    item.getString("productName"),
                                    "", Float.valueOf(item.get("price").toString()), -1, "",
                                    Integer.parseInt(Lib.getInfos(getActivity(), "idrestaurant")),
                                    item.getInt("productType"),
                                    1, null);

                            JSONArray subProducts = item.getJSONArray("combo");
                            if (subProducts != null && subProducts.length() > 0){
                                ArrayList<OtherProduct> subProductss = new ArrayList<>();
                                for (int k=0; k<subProducts.length(); k++) {
                                    JSONObject subItem = subProducts.getJSONObject(k);
                                    subProductss.add(new OtherProduct(1, subItem.getString("productName"), "", 0, -1, "", -1, -1, -1, null));
                                }

                                orderProductL[j].setSubProducts(subProductss);
                            }
                        }

                        // Reset gobal list
                        curOrder.setOrderProductList(new ArrayList<OtherProduct>(Arrays.asList(orderProductL)));
                        curOrder.setOrderQuantityList(new ArrayList<Integer>(Arrays.asList(orderQuantityL)));
                        curOrder.setOrderProductIDList(new ArrayList<Integer>(Arrays.asList(orderIDL)));
                        curOrder.setOrderProductIDCommentList(new ArrayList<Integer>(Arrays.asList(orderIDC)));
                        curOrder.setOrderComment("");

                        orderQuantityList.add(orderQuantityL);
                        orderProductList.add(orderProductL);
                    }
                }
            }
            catch (JSONException e)
            {

            }
        // Lost connection
        else
        {
            Order mOrder = new OrderHandler(getActivity()).getOrder(idOrder);
            numOfCus = Integer.valueOf(editNumOfCustomer.getText().toString());

            if (mOrder != null)
            {
                List<TableHasOrder> mTables = new TableHasOrderHandler(getActivity()).getAllTableHasOrders(idOrder);
                List<OrderHasProduct> mProducts = new OrderHasProductHandler(getActivity()).getAllOrdersInfo(idOrder);
                maxIDProduct = new OrderHasProductHandler((getActivity())).getMaxIDProduct(idOrder);
                ProductComboHandler comboHandler = new ProductComboHandler(getActivity());
                if (mTables != null && mTables.size()>0)
                {
                    String title = "Table: ";
                    for (int k=0; k<mTables.size(); k++)
                        title = title + mTables.get(k).getIdTable();

                    orderTitle.add(title);
                }
                else
                    orderTitle.add("Order: " + mOrder.getIdOrder());

                orderStateList.add(mOrder.getOrderState());
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                //mOrder.setIsLocalData(1);
                idOrderList.add(mOrder);

                if (mProducts != null)
                {
                    Integer[] orderQuantityL = new Integer[mProducts.size()];
                    Integer[] orderIDL = new Integer[mProducts.size()];
                    Integer[] orderIDC = new Integer[mProducts.size()];
                    OtherProduct[] orderProductL = new OtherProduct[mProducts.size()];
                    for (int j=0; j<mProducts.size(); j++)
                    {
                        orderQuantityL[j] = mProducts.get(j).getQuantity();
                        orderIDL[j] = mProducts.get(j).getId();
                        orderIDC[j] = mProducts.get(j).getIdComment();
                        orderProductL[j] = new OtherProduct(new ProductHandler(getActivity()).getOtherProduct(mProducts.get(j).getIdProduct()));
                        orderProductL[j].setProductPrice(mProducts.get(j).getPrice());
                        orderProductL[j].setProductState(mProducts.get(j).getState());

                        List<OtherProduct> subProducts = comboHandler.getAllComboProduct(mProducts.get(j).getIdProduct());
                        if (subProducts != null && subProducts.size() > 0){
                            orderProductL[j].setSubProducts((ArrayList<OtherProduct>)subProducts);
                        }
                    }

                    // Reset global list
                    curOrder.setOrderProductList(new ArrayList<OtherProduct>(Arrays.asList(orderProductL)));
                    curOrder.setOrderQuantityList(new ArrayList<Integer>(Arrays.asList(orderQuantityL)));
                    curOrder.setOrderProductIDList(new ArrayList<Integer>(Arrays.asList(orderIDL)));
                    curOrder.setOrderProductIDCommentList(new ArrayList<Integer>(Arrays.asList(orderIDC)));
                    curOrder.setOrderComment("");

                    orderQuantityList.add(orderQuantityL);
                    orderProductList.add(orderProductL);
                }
            }
        }
    }

    private void splitBill() {
        // Check if user require printer
        if (isPrint) {
            isPrint = false;
            // Then print bill
            Intent intent = new Intent(getActivity().getBaseContext(), SplitBillActivity.class);
            intent.putExtra(SplitBillActivity.CURRENT_ORDER, idOrder);
            // Set of OrderHasProduct
            ArrayList<OrderHasProduct> originalList = new ArrayList<>();
            for (int i=0; i<curOrder.getOrderProductList().size(); i++) {
                originalList.add(new OrderHasProduct(
                        curOrder.getOrderProductIDList().get(i), idOrder,
                        curOrder.getOrderProductList().get(i).getIdProduct(),
                        curOrder.getOrderQuantityList().get(i),
                        curOrder.getOrderProductList().get(i).getProductPrice(),
                        -1, 1, curOrder.getOrderProductIDCommentList().get(i),
                        curOrder.getOrderProductList().get(i).getProductName()
                ));
            }
            curOrder.setOriginalList(new ArrayList<OrderHasProduct>(originalList));
            curOrder.setNewList(new ArrayList<OrderHasProduct>());
            //intent.putExtra(SplitBillActivity.CURRENT_LIST, originalList);
            startActivity(intent);
        }
    }

    /**
     * Set format of bill
     */
    public void setListDataOfTable() {
        // usersInf = new ArrayList<HashMap<String, String>>();
        tableorderInf.clear();
        if (curOrder.getOrderProductList() != null && curOrder.getOrderProductList().size() > 0)
            try
            {
                // them nhan ban
                HashMap<String, String> map = new HashMap<String, String>();
                // adding each child node to HashMap key =&gt; value
                map.put("productName", "Order: " + String.valueOf(idOrder));
                map.put("productQuantity", "");
                map.put("productPrice", "");
                tableorderInf.add(map);
                // Adding child data
                float ordertotal=0;
                for (int i=0; i<curOrder.getOrderProductList().size(); i++)
                {
                    try {
                        ordertotal = ordertotal + curOrder.getOrderQuantityList().get(i) * curOrder.getOrderProductList().get(i).getProductPrice();
                    }catch (Exception ex){};
                    // creating new HashMap
                    map = new HashMap<String, String>();
                    // adding each child node to HashMap key =&gt; value
                    map.put("productName", curOrder.getOrderProductList().get(i).getProductName());
                    map.put("productQuantity", String.valueOf(curOrder.getOrderQuantityList().get(i)));
                    map.put("productPrice", String.valueOf(curOrder.getOrderProductList().get(i).getProductPrice()));
                    tableorderInf.add(map);
                }
                // them nhan ban
                map = new HashMap<String, String>();
                map.put("productName", "Total: " );
                map.put("productQuantity", "");
                map.put("productPrice", String.valueOf(ordertotal));
                tableorderInf.add(map);
//                if (idOrderList.get(0).getOrderDiscount()>=0)
//                {
//                    map = new HashMap<String, String>();
//                    map.put("productName", "Discount: " );
//                    map.put("productQuantity", "");
//                    map.put("productPrice", String.valueOf(idOrderList.get(0).getOrderDiscount()));
//                    tableorderInf.add(map);
//                    float payment = ordertotal*(100-idOrderList.get(0).getOrderDiscount())/100;
//                    map = new HashMap<String, String>();
//                    map.put("productName", "Payment: " );
//                    map.put("productQuantity", "");
//                    map.put("productPrice",String.valueOf(payment));
//                    tableorderInf.add(map);
//                }

                // Check if user require printer
                if (isPrint)
                {
                    isPrint = false;
                    printOrders();
                }
            }
            catch (Exception e) { }
    }

    /**
     * Set tableHasOrder
     */
    public void setTableListData() {
        tableList.clear();
        // Online scenior
        if (tablesJson != null)
            try
            {
                TableHandler mTables = new TableHandler(getActivity());
                OrderHandler order = new OrderHandler(getActivity());
                Order o = order.getOrder(idOrder);
                if (o != null){
                    Table currentTable = mTables.getTable(o.getTable_idTable());
                    if (currentTable == null)
                        currentTable = new Table(-1, "Take away",1, 5, 4, -1, -1, -1, -1, 1);
                    tableList.add(currentTable);
                }

                for (int i=0; i<tablesJson.length(); i++)
                {
                    JSONObject item = tablesJson.getJSONObject(i);
                    tableList.add(new Table(
                            item.getInt("idTable"),
                            item.getString("tableName"),
                            item.getInt("tableState"),
                            item.getInt("tableCapacity"),
                            item.getInt("tableShape"),
                            item.getInt("tableSize"),
                            item.getInt("tableLat"),
                            item.getInt("tableLog"),
                            item.getInt("section_idSection"),
                            item.getInt("idrestaurantinfo")));
                }
            }
            catch (JSONException e)
            {

            }
        // Offline scenior
        else
        {
            TableHandler mTables = new TableHandler(getActivity());
            OrderHandler order = new OrderHandler(getActivity());
            List<Table> tables = mTables.getAllTables(Integer.valueOf(Lib.getInfos(getActivity(),"idrestaurant")));

            Order o = order.getOrder(idOrder);
            if (o != null){
                Table currentTable = mTables.getTable(o.getTable_idTable());
                if (currentTable == null)
                    currentTable = new Table(-1, "Take away",1, 5, 4, -1, -1, -1, -1, 1);
                tables.add(0, currentTable);
            }

            if (tables != null && tables.size()>0)
                tableList.addAll(tables);
        }
    }

    /**
     * Webservice which get order information
     */
    class getOrders extends AsyncTask<String, Void, Void> {

        private Activity context;
        private ProgressDialog pDialog;
        getOrders(Activity context)
        {
            this.context= context;
            pDialog= new ProgressDialog(context);
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog.setMessage("Please wait...");
            //  pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(String... args) {
            JSONParser jParser = new JSONParser();

            // Getting JSON from URL
            JSONArray json = jParser.getJSONFromUrl(orderURL, 1, null);
            // Getting JSON Array
            try {
                // Getting JSON Object
                JSONObject mObject = null;
                if (json != null)
                    mObject = json.getJSONObject(0);
                if (mObject != null && mObject.get("status").toString() == "FALSE") {
                    ordersJson = null;
                    return null;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ordersJson = json;
            setListData();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            editNumOfCustomer.setText(String.valueOf(numOfCus));
            setListDataOfTable();

            // set data to list adapter and list.
            if (orderQuantityList.size() > 0)
                lvProduct.setAdapter(new OtherOrderItemAdapter(getActivity()));

//            if (idOrderList.size() > 0)
//            {
//                editTextNumOfCustomer.setText(Integer.toString(idOrderList.get(0).getOrderNumOfCustomer()));
//                editTextDiscount.setText(Integer.toString(idOrderList.get(0).getOrderDiscount()));
//            }
        }
    }

    /**
     * Webservice which cancel order
     */
    private class postCancelOrder extends AsyncTask<String[], String, JSONArray> {
        private ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONArray doInBackground(String[]... args) {
            JSONParser jParser = new JSONParser();
            // Collect params to pass to POST method on http
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("idOrder", args[0][0]));
            nameValuePairs.add(new BasicNameValuePair("orderState", args[0][1]));

            // Posting JSON from URL
            JSONArray orderJson = jParser.getJSONFromUrl(orderStateURL, JSONParser.POST, nameValuePairs);
            // Reset curOrder
            currentOrder = null;
            try {
                // Getting JSON Object
                if (orderJson != null)
                    currentOrder = orderJson.getJSONObject(0);
                if (currentOrder != null && currentOrder.get("status").toString() == "FALSE") {
                    currentOrder = null;
                    return null;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return orderJson;
        }
        @Override
        protected void onPostExecute(JSONArray json) {
            updateCancelRecords();
            //pDialog.dismiss();
        }
    }

    /**
     * webservice which will update order into paid state and others related info
     */
    private class postTableOrder extends AsyncTask<String[], String, JSONArray> {
        private ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Set diaglog to waiting for fetching data
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Updating Data ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONArray doInBackground(String[]... args) {
            JSONParser jParser = new JSONParser();
            // Collect params to pass to POST method on http
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("idOrder", args[0][0]));
            nameValuePairs.add(new BasicNameValuePair("orderState", args[0][1]));
            nameValuePairs.add(new BasicNameValuePair("idStaff", args[0][2]));

            // Postin JSON from URL
            JSONArray userJson = jParser.getJSONFromUrl(tableorderupdateURL, JSONParser.POST, nameValuePairs);
            if( userJson!= null) {
                try {
                    // Getting JSON Array
                    JSONObject UpdateUser = userJson.getJSONObject(0);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return userJson;
        }
        @Override
        protected void onPostExecute(JSONArray json) {
            pDialog.dismiss();
        }
    }


    /**
     * Webservice which update order detail
     */
    private class postOrder extends AsyncTask<String[], String, JSONArray> {
        private ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Set diaglog to waiting for fetching data
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Updating Data ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONArray doInBackground(String[]... args) {
            JSONParser jParser = new JSONParser();
            // Collect params to pass to POST method on http
            JSONArray orderJson;
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            if (flag == 1)
            {
                if (idOrder != Integer.MAX_VALUE)
                    nameValuePairs.add(new BasicNameValuePair("idOrder", args[0][ID_ORDER]));
                nameValuePairs.add(new BasicNameValuePair("orderTime", args[0][TIME]));
                nameValuePairs.add(new BasicNameValuePair("orderState", args[0][ORDER_STATE]));
                nameValuePairs.add(new BasicNameValuePair("restaurant_idrestaurant", args[0][3]));
                nameValuePairs.add(new BasicNameValuePair("table_idTable", args[0][4]));
                nameValuePairs.add(new BasicNameValuePair("numOfCustomer", args[0][5]));
                // Posting JSON from URL
                orderJson = jParser.getJSONFromUrl(orderURL, JSONParser.POST, nameValuePairs);
            }
            else
            {
                String url = orderURL.substring(0, orderURL.lastIndexOf("/") + 1) + args[0][ID_ORDER];
                // Posting JSON from URL
                orderJson = jParser.getJSONFromUrl(url, JSONParser.DELETE, nameValuePairs);
            }

            // Reset currentOrder
            currentOrder = null;
            try {
                // Getting JSON Object
                if (orderJson != null)
                    currentOrder = orderJson.getJSONObject(0);
                if (currentOrder != null && currentOrder.get("status").toString() == "FALSE") {
                    currentOrder = null;
                    return null;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return orderJson;
        }
        @Override
        protected void onPostExecute(JSONArray json) {
            // Only update if insert/change new records
            if (flag == 1)
                updateRelatedRecords();
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
        }
    }

    /**
     * Webservice which update product related to currentOrder
     */
    private class postProduct extends AsyncTask<String[], String, JSONArray> {
        private ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Set diaglog to waiting for fetching data
            /*pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Updating Data ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();*/
        }

        @Override
        protected JSONArray doInBackground(String[]... args) {
            JSONParser jParser = new JSONParser();
            // Collect params to pass to POST method on http
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("idProduct", args[0][ID_ORDER]));
            nameValuePairs.add(new BasicNameValuePair("order_idOrder", args[0][ID_ORDER + 1]));
            nameValuePairs.add(new BasicNameValuePair("product_idProduct", args[0][PRODUCT_ID_PRODUCT + 1]));
            nameValuePairs.add(new BasicNameValuePair("productQuantity", args[0][PRODUCT_QUANTITY + 1]));
            nameValuePairs.add(new BasicNameValuePair("productPrice", args[0][PRODUCT_PRICE + 1]));
            nameValuePairs.add(new BasicNameValuePair("orderProductState", args[0][PRODUCT_STATE + 1]));
            nameValuePairs.add(new BasicNameValuePair("bill_order_idBillOrder", args[0][PRODUCT_BILL + 1]));
            nameValuePairs.add(new BasicNameValuePair("comments_idComment", args[0][PRODUCT_COMMENT + 1]));
            nameValuePairs.add(new BasicNameValuePair("flag", "2"));

            // Posting JSON from URL
            JSONArray orderJson = jParser.getJSONFromUrl(orderHasProductURL, JSONParser.POST, nameValuePairs);
            try {
                // Getting JSON Array
                JSONObject updatedOrder = orderJson.getJSONObject(0);
                // If update order succesfully, then update 2 related tables
                if (updatedOrder != null)
                {

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return orderJson;
        }
        @Override
        protected void onPostExecute(JSONArray json) {
            //pDialog.dismiss();
        }
    }

    /**
     * Webservice which update table related to currentOrder
     */
    private class postTable extends AsyncTask<String[], String, JSONArray> {
        private ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected JSONArray doInBackground(String[]... args) {
            JSONParser jParser = new JSONParser();
            // Collect params to pass to POST method on http
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("order_idOrder", args[0][ORDER_ID_ORDER]));
            nameValuePairs.add(new BasicNameValuePair("table_idTable", args[0][TABLE_ID_TABLE]));
            nameValuePairs.add(new BasicNameValuePair("orderState", args[0][ORDER_STATE]));

            // Posting JSON from URL
            JSONArray orderJson = jParser.getJSONFromUrl(orderHasTableURL, JSONParser.POST, nameValuePairs);
            return orderJson;
        }
        @Override
        protected void onPostExecute(JSONArray json) {
            //pDialog.dismiss();
        }
    }

    /**
     * Get available tables
     */
    class getAvailableTables extends AsyncTask<String, Void, Void> {

        private Activity context;
        private ProgressDialog pDialog;
        getAvailableTables(Activity context)
        {
            this.context= context;
            pDialog= new ProgressDialog(context);
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            //pDialog.setMessage("Please wait...");
            //  pDialog.setCancelable(false);
            //pDialog.show();
        }

        @Override
        protected Void doInBackground(String... args) {
            JSONParser jParser = new JSONParser();
            //String url = tableURL + "/" +Integer.valueOf(args[0]);
            // Getting JSON from URL
            JSONArray json = jParser.getJSONFromUrl(tableURL, JSONParser.GET, null);
            // Getting JSON Array
            tablesJson = json;
            setTableListData();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            // set data to list adapter and list.
            if (tableList.size() > 0)
            {
                ArrayList<String> tableName = new ArrayList<String>();
                // Add take away into spinner
                tableName.add("Take away");
                SectionHandler sectionHandler = new SectionHandler(context);
                for (int i=0; i<tableList.size(); i++) {
                    Sections s = sectionHandler.getSection(tableList.get(i).getTablesection());
                    if (s != null)
                        tableName.add(s.getSectionName() + " : " + tableList.get(i).getTableName());
                    else
                        tableName.add(tableList.get(i).getTableName());
                }
                // Create an ArrayAdapter using the string array and a default spinner layout
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_spinner_item, tableName){
                    public View getView(int position, View convertView,ViewGroup parent) {
                        View v = super.getView(position, convertView, parent);
                        //((TextView) v).setTextSize(30);
                        return v;
                    }

                    public View getDropDownView(int position, View convertView,ViewGroup parent) {
                        View v = super.getDropDownView(position, convertView,parent);
                        ((TextView) v).setGravity(Gravity.LEFT);
                        return v;
                    }
                };

                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // Apply the adapter to the spinner
                spnTables.setAdapter(adapter);
                if (tableList.size() > 0)
                    spnTables.setSelection(1);
            }
        }
    }
}
