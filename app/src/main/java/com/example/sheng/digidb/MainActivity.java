package com.example.sheng.digidb;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends AppCompatActivity  implements SeekBar.OnSeekBarChangeListener,
        OnChartValueSelectedListener {

    private final String TAG = "DB TEST";
    private   String _date = null ;  //時間
    private   String _mode ="自主模式" ;  //模式
    private   String _exrpjct  ; //運動項目
    private    int _group =0;     //組數
    private    int _times =0;     //次數
    private   String _lmup ="null";  //上斜方肌之最大EMG訊號
    private   String _lmdw ="null";  //下斜方肌之最大EMG訊號
    private   String _rmup ="null";  //上斜方肌之最大EMG訊號
    private   String _rmdw ="null";  //下斜方肌之最大EMG訊號
    private   String StrGroup = "0";
    private   String StrTime ="0";

    private ArrayList LTarrayList;
    private ArrayList LCarrayList;
    private ArrayList RTarrayList;
    private ArrayList RCarrayList;


    private  Queue<Double> LTEMGqueue ;
    private  Queue<Double> LTEMGqueue96 ;
    private  Queue<Double> LCEMGqueue ;
    private  Queue<Double> LCEMGqueue96 ;
    private  Queue<Double> RTEMGqueue ;
    private  Queue<Double> RTEMGqueue96 ;
    private  Queue<Double> RCEMGqueue ;
    private  Queue<Double> RCEMGqueue96 ;

    private LineChart chart1,chart2,chart3,chart4;

    private    double[] LTWindow,LCWindow,RTWindow,RCWindow ;

    private  int SampleRate = 0 ;
    private  String[] Sports = {"請選擇運動", "運動一", "運動二"};
    private BluetoothGatt mBtGatt = null;
    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private List<String> bluetoothDevices = new ArrayList<String>();
    private Set<BluetoothDevice> device;

    private ArrayAdapter<String> DeviceAdapter;
    private ArrayAdapter<String> SportsAdapter;
    private Spinner SportSpinner;
    private Spinner DeviceSpinner;
    private Switch mode;
    private EditText group;
    private EditText times;
    private TextView textView;
    private TextView textView5;
    private TextView textView6;
    private TextView textView9;
    private TextView textView12;
    private Button comfirmNum;
    private Button ListDB;
    private Button setMaxButton;
    private Button DeletDB;
    private Button printMVC;
    private Button ButtonScan;
    private Handler handlerCH1;
    private Handler handlerCH2;
    private Handler countdown;
    private DBhelper DH = null;

    private boolean QueueFirstLocker;
    private int mState = 0;
    private final int CONNECTED = 0x01;
    private final int DISCONNECTED = 0x02;
    private final int CONNECTTING = 0x03;
    public Set set = new HashSet();
    private Timer  mTimer,RateConterTimer,mOffTime;;
    private Boolean TimerLocker;
    private Boolean setMaxBoolen;

    private int LTQ96Conter, LCQ96Conter, RTQ96Conter, RCQ96Conter;

    private BluetoothGattCharacteristic mWriteCharacteristic = null;
    private BluetoothGattCharacteristic mReadCharacteristric = null;

    private BluetoothGattCharacteristic mReadCharacteristric1 = null; //TTRI1

    // 讀寫相關的萬九DEVICE的Service、Characteristic的UUID
    public static final UUID TRANSFER_SERVICE_READ_ZENTAN = UUID.fromString("00000000-8535-B5A0-7140-A304D2495CB7");
    public static final UUID TRANSFER_SERVICE_WRITE_ZENTAN = UUID.fromString("00000000-8535-B5A0-7140-A304D2495CB7");
    public static final UUID TRANSFER_CHARACTERISTIC_READ_ZENTAN = UUID.fromString("00000000-8535-B5A0-7140-A304D2495CB8");
    public static final UUID TRANSFER_CHARACTERISTIC_WRITE_ZENTAN = UUID.fromString("00000000-8535-B5A0-7140-A304D2495CB8");

    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    //讀相關的TTRI_4CH_DEVICE的Service、Characteristic的UUID
    public static final UUID TRANSFER_SERVICE_READ_TTRI1 = UUID.fromString("0000fe84-0000-1000-8000-00805f9b34fb");
    public static final UUID TRANSFER_CHARACTERISTIC_READ_TTRI1 = UUID.fromString("2D30C082-F39F-4CE6-923F-3484EA480596");


    public static final UUID TRANSFER_DESCRIPTOR_READ = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private static final UUID Battery_Service_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
    private static final UUID Battery_Level_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");



    String string2 = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setMaxBoolen = true;

        textView = findViewById(R.id.textView4);
        group = findViewById(R.id.editText);
        times = findViewById(R.id.editText2);
        mode = findViewById(R.id.switch1);
        textView5 = findViewById(R.id.textView5);
        textView6 = findViewById(R.id.textView6);
        textView9 = findViewById(R.id.textView9);
        textView12= findViewById(R.id.textView12);
        //printMVC = findViewById(R.id.button6);

        LTWindow = new double[96];
        LCWindow = new double[96];
        RTWindow = new double[96];
        RCWindow = new double[96];


       //作圖
        chart1 = findViewById(R.id.chart1);
        chart1.setOnChartValueSelectedListener(MainActivity.this);
        // no description text
        chart1.getDescription().setEnabled(false);
        // enable touch gestures
        chart1.setTouchEnabled(true);

        chart2 = findViewById(R.id.chart2);
        chart2.setOnChartValueSelectedListener(this);
        // no description text
        chart2.getDescription().setEnabled(false);
        // enable touch gestures
        chart2.setTouchEnabled(true);

        chart3 = findViewById(R.id.chart3);
        chart3.setOnChartValueSelectedListener(this);
        // no description text
        chart3.getDescription().setEnabled(false);
        // enable touch gestures
        chart3.setTouchEnabled(true);

        chart4 = findViewById(R.id.chart4);
        chart4.setOnChartValueSelectedListener(this);
        // no description text
        chart4.getDescription().setEnabled(false);
        // enable touch gestures
        chart4.setTouchEnabled(true);


        chart1.setDragDecelerationFrictionCoef(0.9f);
        chart2.setDragDecelerationFrictionCoef(0.9f);
        chart3.setDragDecelerationFrictionCoef(0.9f);
        chart4.setDragDecelerationFrictionCoef(0.9f);

        // enable scaling and dragging
        chart1.setDragEnabled(true);
        chart1.setScaleEnabled(true);
        chart1.setDrawGridBackground(false);
        chart1.setHighlightPerDragEnabled(true);
        // if disabled, scaling can be done on x- and y-axis separately
        chart1.setPinchZoom(true);
        // set an alternative background color
        chart1.setBackgroundColor(Color.LTGRAY);


        // enable scaling and dragging
        chart2.setDragEnabled(true);
        chart2.setScaleEnabled(true);
        chart2.setDrawGridBackground(false);
        chart2.setHighlightPerDragEnabled(true);
        // if disabled, scaling can be done on x- and y-axis separately
        chart2.setPinchZoom(true);
        // set an alternative background color
        chart2.setBackgroundColor(Color.LTGRAY);

        // enable scaling and dragging
        chart3.setDragEnabled(true);
        chart3.setScaleEnabled(true);
        chart3.setDrawGridBackground(false);
        chart3.setHighlightPerDragEnabled(true);
        // if disabled, scaling can be done on x- and y-axis separately
        chart3.setPinchZoom(true);
        // set an alternative background color
        chart3.setBackgroundColor(Color.LTGRAY);

        // enable scaling and dragging
        chart4.setDragEnabled(true);
        chart4.setScaleEnabled(true);
        chart4.setDrawGridBackground(false);
        chart4.setHighlightPerDragEnabled(true);
        // if disabled, scaling can be done on x- and y-axis separately
        chart4.setPinchZoom(true);
        // set an alternative background color
        chart4.setBackgroundColor(Color.LTGRAY);


        TimerLocker = false;
        comfirmNum = findViewById(R.id.button);
        ListDB = findViewById(R.id.button2);
        DeletDB = findViewById(R.id.button3);
        ButtonScan = findViewById(R.id.button4);
        setMaxButton = findViewById(R.id.button5);

        SportSpinner = (Spinner) findViewById(R.id.spinner);
        DeviceSpinner = findViewById(R.id.spinner2);

        SportsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, Sports);
        DeviceAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,android.R.id.text1, bluetoothDevices);
        SportSpinner.setAdapter(SportsAdapter);
        DeviceSpinner.setAdapter(DeviceAdapter);

        // handlerCH1 = new EMGCH1Handler();
        // handlerCH2 = new EMGCH2Handler();
        countdown = new CountDownHandler();

        QueueFirstLocker = true;
        LTEMGqueue = new LinkedList<Double>();
        LTEMGqueue96 = new LinkedList<Double>();
        LCEMGqueue = new LinkedList<Double>();
        LCEMGqueue96 = new LinkedList<Double>();
        RTEMGqueue = new LinkedList<Double>();
        RTEMGqueue96 = new LinkedList<Double>();
        RCEMGqueue = new LinkedList<Double>();
        RCEMGqueue96 = new LinkedList<Double>();

        LTarrayList = new ArrayList();
        LCarrayList = new ArrayList();
        RTarrayList = new ArrayList();
        RCarrayList = new ArrayList();


        LTQ96Conter = 0;
        LCQ96Conter = 0;
        RTQ96Conter = 0;
        RCQ96Conter = 0;

        device = mBluetoothAdapter.getBondedDevices();

        bluetoothDevices.add("請點選欲連結的裝置" + "\n");
        openDB();




        chart1.animateX(100);
        chart2.animateX(100);
        chart3.animateX(100);
        chart4.animateX(100);

        // get the legend (only possible after setting data)
        Legend l1 = chart1.getLegend();

        // modify the legend ...
        l1.setForm(Legend.LegendForm.LINE);
        //l.setTypeface(tfLight);
        l1.setTextSize(11f);
        l1.setTextColor(Color.WHITE);
        l1.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l1.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l1.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l1.setDrawInside(false);
//        l.setYOffset(11f);


        Legend l2 = chart2.getLegend();

        // modify the legend ...
        l2.setForm(Legend.LegendForm.LINE);
        //l.setTypeface(tfLight);
        l2.setTextSize(11f);
        l2.setTextColor(Color.WHITE);
        l2.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l2.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l2.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l2.setDrawInside(false);




        //設定藍芽下拉式選單
        DeviceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


                if (position > 0) {
                    String s = DeviceAdapter.getItem(position);
                    // 对其进行分割，获取到这个设备的地址
                    String address = s.substring(s.indexOf(":") + 1).trim();
                    Toast.makeText(getApplicationContext(), "當前點擊的裝置地址:" + address, Toast.LENGTH_SHORT).show();
                    Devicedial(address);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

/*
        printMVC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
*/

        //掃描藍芽按鈕
        ButtonScan.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View v) {
                BTOnScan();
                startScan();
            }
        });


        setMaxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {




                if(setMaxBoolen==true) {
                    setMaxBoolen = false;
                    Toast.makeText(getApplicationContext(), "最大肌力施力測試開始", Toast.LENGTH_LONG).show();
                    setMaxCountDown();

                }
            }
        });


        //將"自主模式"或是"教練模式"抓進 _mode變數中
        mode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked)  {
                    _mode ="教練模式";
                    Log.d(TAG, "教練模式");
                } else {
                    _mode ="自主模式";
                    Log.d(TAG, "自主模式");
                }
            }
        });






        //運動項目設定
        SportSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    _exrpjct  = Sports[position];
                    if(position!=0){
                    setText5ListLast();
                        //setMVCData();
                        }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        //確認儲存按鈕
        View.OnClickListener comfirmNumBut = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDate();
                getEdit();
                dial();
                Log.d(TAG, "buttom案");
            }
        };
        comfirmNum.setOnClickListener( comfirmNumBut);


        //列出DB按鈕
       ListDB.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
           listAll();
           }
       });


       //刪除DB按鈕
        DeletDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //closeDB();
                deleteAll();
                string2 ="";
                textView.setText(string2);
               // SQLiteDatabase db = DH.getWritableDatabase();
               // DH.onUpgrade(db,db.getVersion(),db.getVersion());
            }
        });

        // init timer
        mTimer = new Timer();
        // start timer task
        setTimerTask();

    } //oncreat底




    private void getDate(){         //把日期時間抓到_date
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis()) ; // 獲取當前時間
        Log.d(TAG, "獲取時間");
         _date = formatter.format(curDate);

    }

    private void getEdit(){    //把組數跟次數從EDITX抓到 _group & _times
        if((((group.getText().toString()).length()) != 0)&&(((times.getText().toString()).length()) != 0)) {

            StrGroup = group.getText().toString();
            StrTime = times.getText().toString();

            _group = Integer.valueOf(StrGroup);
            _times = Integer.valueOf(StrTime);
        }
    }


    //對話框設定
    public void dial() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("確認輸入資訊");
        builder.setMessage("日期:"+_date+'\n'+ "模式:"+_mode+ '\n'+"運動項目:"+_exrpjct+ '\n'+"目標組數:" + _group + '\n'+"目標次數:"+_times+'\n');

        builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "寫進資料庫");
                add();

            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //沒做事就只是取消
                Log.d(TAG, "重新填寫資訊");
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }



    /**
     * 對話框設定
     */
    public void Devicedial(final String add) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("連接確認");
        builder.setMessage("你要連接的裝置為:" + "\n" + add);
        builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                BluetoothDevice btDev = mBluetoothAdapter.getRemoteDevice(add);
                connect(btDev);
                stopScan();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //沒做事就只是取消
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }




    //若藍芽沒開你要開藍芽，並把連結過的裝置設在選單中
    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void BTOnScan() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                builder.setTitle("權限請求");
                builder.setMessage("請准許此應用程式使用定位權限");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @TargetApi(Build.VERSION_CODES.M)
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {

            Toast.makeText(getApplicationContext(), "請開啟藍芽", Toast.LENGTH_LONG).show();
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            int REQUEST_ENABLE_BT = 1;
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        if (mBluetoothAdapter.isEnabled()) {
            if (device.size() > 0) {

                for (Iterator iterator = device.iterator(); iterator.hasNext(); ) {
                    BluetoothDevice bluetoothDevice = (BluetoothDevice) iterator.next();
                    bluetoothDevices.add(bluetoothDevice.getName() + "曾經連結過:" + bluetoothDevice.getAddress() + "\n");

                }
                //  listView.setAdapter(arrayAdapter);
                DeviceSpinner.setAdapter(DeviceAdapter);

            } else {
                Toast.makeText(getApplicationContext(), "尚未有連結過的藍芽裝置", Toast.LENGTH_LONG).show();
            }
        }
       // startScan();
        Toast.makeText(getApplicationContext(), "開始掃描藍芽裝置", Toast.LENGTH_SHORT).show();
    }





//**************************藍芽函式********************************



    //BLE的LeScan CallBack Function
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice bluetoothDevice, int rssi, byte[] ScanRecord) {
            // device 被掃到的BLE對象
            //aram rssi BLE的訓強度。
            //param scanRecord 被掃到BLE對象的紀錄
            runOnUiThread(new Runnable() {
                @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                @Override
                public void run() {

                    if (bluetoothDevice.getBondState() != BluetoothDevice.BOND_BONDED) {

                        String S = bluetoothDevice.getAddress();
                        //boolean isContains = Arrays.asList(set).contains(S);
                        boolean isContains = set.contains(S);

                        if (!isContains) {
                            set.add(S);
                            bluetoothDevices.add(bluetoothDevice.getName() + "新搜尋到的裝置:" + bluetoothDevice.getAddress() + "\n");

                            DeviceAdapter.notifyDataSetChanged();
                            DeviceSpinner.setAdapter(DeviceAdapter);

                        }


                    }
                }
            });
        }
    };



    //Gatt Call Back Function
    private BluetoothGattCallback mBtGattCallback = new BluetoothGattCallback() {

        // 連接狀態發生改變時的回調
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                mState = CONNECTED;
                Log.d(TAG, "connected OK");
                mBtGatt.discoverServices();
            } else if (newState == BluetoothGatt.GATT_FAILURE) {
                mState = DISCONNECTED;
                Log.d(TAG, "connect failed");
            }
        }


        // 遠端設備中的服務可用時的回調
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onServicesDiscovered(BluetoothGatt mBtGatt, int status) {
            Log.d(TAG, "public void onServicesDiscovered");

/*
            //萬九裝置
            if (status == BluetoothGatt.GATT_SUCCESS) {
               // BluetoothGattService btGattWriteService = mBtGatt.getService(TRANSFER_SERVICE_WRITE_ZENTAN);
                BluetoothGattService btGattReadService = mBtGatt.getService(TRANSFER_SERVICE_READ_ZENTAN);
                Log.d(TAG, "GATT_SUCCESS");

                if (btGattReadService != null) {
                    mReadCharacteristric = btGattReadService.getCharacteristic(TRANSFER_CHARACTERISTIC_READ_ZENTAN);
                    Log.d(TAG, " btGattReadService = ture ;");

                    if (mReadCharacteristric != null) {

                        for(BluetoothGattDescriptor dp:mReadCharacteristric.getDescriptors()){
                            dp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            Log.d(TAG, " for迴圈跑完 dp : "+ dp.toString());}



                        BluetoothGattDescriptor dp = mReadCharacteristric.getDescriptor(TRANSFER_DESCRIPTOR_READ);
                        dp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        mBtGatt.setCharacteristicNotification(mReadCharacteristric, true);
                        mBtGatt.readCharacteristic(mReadCharacteristric);


                        Log.d(TAG, "mReadCharacteristric = ture");

                    } else {
                        Log.d(TAG, "mReadCharacteristric = null");
                    }
                }
                if (btGattReadService == null) {
                    Log.d(TAG, "btGattService = null");
                }


                BluetoothGattService batteryService = mBtGatt.getService(Battery_Service_UUID);
                BluetoothGattCharacteristic batteryLevel = batteryService.getCharacteristic(Battery_Level_UUID);

                if ((batteryService != null) && (batteryLevel != null)) {
                     // Battery.setEnabled(true);
                }
            }
*/


            //TTRI裝置
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //BluetoothGattService btGattWriteService = mBtGatt.getService(TRANSFER_SERVICE_WRITE_TTRI);
                BluetoothGattService btGattReadService = mBtGatt.getService(TRANSFER_SERVICE_READ_TTRI1);
                Log.d(TAG, "TTRI GATT_SUCCESS");

                if (btGattReadService != null) {
                    mReadCharacteristric = btGattReadService.getCharacteristic(TRANSFER_CHARACTERISTIC_READ_TTRI1);
                    Log.d(TAG, "TTRI btGattReadService = ture ;");

                    if (mReadCharacteristric != null) {


                        BluetoothGattDescriptor dp = mReadCharacteristric.getDescriptor(TRANSFER_DESCRIPTOR_READ);
                        dp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        mBtGatt.setCharacteristicNotification(mReadCharacteristric, true);
                        mBtGatt.readCharacteristic(mReadCharacteristric);



                       // dp = mReadCharacteristric2.getDescriptor(TRANSFER_DESCRIPTOR_READ);
                       // dp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);




                        Log.d(TAG, "TTRI mReadCharacteristric = ture");

                    } else {
                        Log.d(TAG, "TTRI mReadCharacteristric = null");
                    }
                }else{
                    Log.d(TAG, "TTRI btGattService = null");
                }

            }

        }







        // 某Characteristic的狀態為可讀時的回調
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onCharacteristicRead(BluetoothGatt mBtGatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, " void onCharacteristicRead ");

            if (status == BluetoothGatt.GATT_SUCCESS) {
                readCharacterisricValue(characteristic);
                Log.d(TAG, "onCharacteristicRead --> status == BluetoothGatt.GATT_SUCCESS");
                // 訂閱遠端設備的characteristic，
                // 當此characteristic發生改變時當回調mBtGattCallback中的onCharacteristicChanged方法

                mBtGatt.setCharacteristicNotification(mReadCharacteristric, true);
                BluetoothGattDescriptor descriptor = mReadCharacteristric.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);


                if (descriptor != null) {
                    byte[] val = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                    descriptor.setValue(val);
                    mBtGatt.writeDescriptor(descriptor);
                    Log.d(TAG, "descriptor != null");
                } else {
                    Log.d(TAG, "onCharacteristicRead --> status == BluetoothGatt.GATT_SUCCESS && descriptor = null");
                }


            }
        }


        // 寫入Characteristic成功與否的回調
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {

            switch (status) {
                case BluetoothGatt.GATT_SUCCESS:
                    Log.d(TAG, "write data success");
                    break;// 寫入成功
                case BluetoothGatt.GATT_FAILURE:
                    Log.d(TAG, "write data failed");
                    break;// 寫入失敗
                case BluetoothGatt.GATT_WRITE_NOT_PERMITTED:
                    Log.d(TAG, "write not permitted");
                    break;// 沒有寫入的權限
            }
        }

        // 訂閱了遠端設備的Characteristic信息後，
        // 當遠端設備的Characteristic信息發生改變後,回調此方法
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            // Log.d(TAG, "onCharacteristicChanged");
            if ((characteristic.getUuid()).equals(TRANSFER_CHARACTERISTIC_READ_TTRI1)) {
                readCharacterisricValue(characteristic);
            } else{
                readCharacterisricValue(characteristic);
            }
        }
    };





    /**
     * 讀取BluetoothGattCharacteristic中的數據
     *
     * @param characteristic
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void readCharacterisricValue(BluetoothGattCharacteristic characteristic) {

        SampleRate = SampleRate + 1;
        if(QueueFirstLocker ==true){
            RateConterTimer = new Timer();
           RateConter();
            QueueFirstLocker =false;
        }


        byte[] data = characteristic.getValue();
        StringBuffer buffer = new StringBuffer("0x");
        int i;
        int j = 0;
        String k = null;


        for (byte b : data) {

            i = b & 0xff;
            if (i == 0) {
                k = "00";
            } else if ((i < 16) && (i != 0)) {
                k = "0" + Integer.toHexString(i);
            } else {
                k = Integer.toHexString(i);
            }
            j = j + 1;
            buffer.append(k);
            // Log.d(TAG, "Integer.toHexString = " + k);

        }

/*
        //如果要讀的是電量
        if ((characteristic.getUuid()).equals(Battery_Level_UUID)) {
            Log.d(TAG, "電池電量:" + buffer.toString());
            Message message = Message.obtain();
            //message.arg1 = Integer.parseInt(buffer.toString());
            message.obj = buffer.toString();
            handler2.sendMessage(message);
        }
*/

        //如果要讀的是data
        if ((characteristic.getUuid()).equals(TRANSFER_CHARACTERISTIC_READ_ZENTAN)) {
            List<String> LS = new ArrayList<String>();
            StringBuffer EMGdataC1Buffer = new StringBuffer();
            StringBuffer EMGdataC2Buffer = new StringBuffer();
            String M;
            String Header;  //2
            String Length;  //2
            String SN;      //2
            String Flag;    //4
            String Time;    //6
            String GS;      //6
            String EMGdataC1; //32
            String EMGdataC2; //32


           // if (TimerLocker == false) {

                M = buffer.toString();
               // TimerLocker = true;

                String[] ary = M.split("");

                for (int a = 3; a < 7; a++) {
                    LS.add(ary[a]);
                }
                Header = LS.toString();
                LS.clear();


                for (int a = 7; a < 11; a++) {
                    LS.add(ary[a]);
                }
/*                if(SRN.length() < 200)
                    SRN.append(ary[7]+"|");
                Log.d(TAG, "完整SN:" + SRN);*/

                SN = LS.toString();
                LS.clear();


                for (int a = 11; a < 15; a++) {
                    LS.add(ary[a]);
                }
                Length = LS.toString();
                LS.clear();

                for (int a = 15; a < 23; a++) {
                    LS.add(ary[a]);
                }
                Flag = LS.toString();
                LS.clear();

                for (int a = 23; a < 35; a++) {
                    LS.add(ary[a]);
                }
                Time = LS.toString();
                LS.clear();

                for (int a = 35; a < 47; a++) {
                    LS.add(ary[a]);
                }
                GS = LS.toString();
                LS.clear();

                for (int a = 47; a < 111; a++) {
                    LS.add(ary[a]);
                    EMGdataC1Buffer.append(ary[a]);
                }
                EMGdataC1 = LS.toString();
                LS.clear();

                for (int a = 111; a < 175; a++) {
                    LS.add(ary[a]);
                    EMGdataC2Buffer.append(ary[a]);
                }
                EMGdataC2 = LS.toString();
                LS.clear();


                Log.d(TAG,
                        "" + '\n'
                                + "[Header]:" + Header + '\n'
                                + "[Length]:" + Length + '\n'
                                + "[Flag]:" + Flag + '\n'
                                + "[SN]:" + SN + '\n'
                                + "[Time]:" + Time + '\n'
                                + "[GS]:" + GS + '\n'
                                + "[EMG_Data_C1]:" + EMGdataC1 + '\n'
                                + "[EMG_Data_C2]:" + EMGdataC2 + '\n'
                );
                Log.d(TAG, "EMG read data:" + buffer.toString());
                //  Log.d(TAG, "長度:" + M.length());


                //SRN以message傳送至handler
                //Message messageSRN =Message.obtain();
                // messageSRN.obj = SN+"|";
                //SRNhandler.sendMessage(messageSRN);


                //EMG_CH1之DATA
                Message message1 = Message.obtain();
                //message.arg1 = Integer.parseInt(buffer.toString());
                message1.obj = EMGdataC1Buffer.toString();
                handlerCH1.sendMessage(message1);

                //EMG_CH2之DATA
                Message message2 = Message.obtain();
                //message.arg1 = Integer.parseInt(buffer.toString());
                message2.obj = EMGdataC2Buffer.toString();
                handlerCH2.sendMessage(message2);
//
           // }
        }

        //TTRI1
        if ((characteristic.getUuid()).equals(TRANSFER_CHARACTERISTIC_READ_TTRI1)) {



                //  Log.d(TAG, buffer.toString());

                List<String> LS = new ArrayList<String>();

                String LTEMGString;
                String LCEMGString;
                String RTEMGString;
                String RCEMGString;

                String StrBuffer;
                String Header0;
                int Header1;  //2

                StrBuffer = buffer.toString();
                String[] ary = StrBuffer.split("");
                Header0 = ary[3] + ary[4];
                Header1 = Integer.valueOf(ary[5] + ary[6], 16);


                Log.d(TAG, "Header0 = " + Header0.toString()
                        + "Header1 =" + Header1);


                if ((Header0.equals("a0")) && (Header1 == 20)) {

                    LTEMGString = Hex123intoDec(ary[7], ary[8], ary[9], ary[10], ary[11], ary[12]);
                    LCEMGString = Hex123intoDec(ary[13], ary[14], ary[15], ary[16], ary[17], ary[18]);
                    RTEMGString = Hex123intoDec(ary[19], ary[20], ary[21], ary[22], ary[23], ary[24]);
                    RCEMGString = Hex123intoDec(ary[25], ary[26], ary[27], ary[28], ary[29], ary[30]);

                    double LTdouble = Double.parseDouble(LTEMGString);
                    double LCdouble = Double.parseDouble(LCEMGString);
                    double RTdouble = Double.parseDouble(RTEMGString);
                    double RCdouble = Double.parseDouble(RCEMGString);

                    Log.d(TAG, "data =  " + buffer.toString());
                    Log.d(TAG, "Header0 = " + Header0.toString() + '\n' +
                            "Header1 =" + Header1 +
                            "LTdouble =" + LTdouble +
                            "LCdouble =" + LCdouble +
                            "RTdouble =" + RTdouble +
                            "RCdouble =" + RCdouble);


                    if (LTQ96Conter < 96) {
                        LTEMGqueue96.offer(LTdouble);
                        LTQ96Conter = LTQ96Conter + 1;

                    } else {
                        LTEMGqueue.offer(LTdouble);
                    }


                    if (LCQ96Conter < 96) {
                        LCEMGqueue96.offer(LCdouble);
                        LCQ96Conter = LCQ96Conter + 1;

                    } else {
                        LCEMGqueue.offer(LCdouble);
                    }


                    if (RTQ96Conter < 96) {
                        RTEMGqueue96.offer(RTdouble);
                        RTQ96Conter = RTQ96Conter + 1;

                    } else {
                        RTEMGqueue.offer(RTdouble);
                    }


                    if (RCQ96Conter < 96) {
                        RCEMGqueue96.offer(RCdouble);
                        RCQ96Conter = RCQ96Conter + 1;

                    } else {
                        RCEMGqueue.offer(RCdouble);
                    }

                    LTEMGString = Hex123intoDec(ary[31], ary[32], ary[33], ary[34], ary[35], ary[36]);
                    LCEMGString = Hex123intoDec(ary[37], ary[38], ary[39], ary[40], ary[41], ary[42]);
                    LTdouble = Double.parseDouble(LTEMGString);
                    LCdouble = Double.parseDouble(LCEMGString);


                    if (LTQ96Conter < 96) {
                        LTEMGqueue96.offer(LTdouble);
                        LTQ96Conter = LTQ96Conter + 1;

                    } else {
                        LTEMGqueue.offer(LTdouble);
                    }


                    if (LCQ96Conter < 96) {
                        LCEMGqueue96.offer(LCdouble);
                        LCQ96Conter = LCQ96Conter + 1;

                    } else {
                        LCEMGqueue.offer(LCdouble);
                    }

                }


                if (Header0.equals("a1") && (Header1 == 20)) {

                    RTEMGString = Hex123intoDec(ary[7], ary[8], ary[9], ary[10], ary[11], ary[12]);
                    RCEMGString = Hex123intoDec(ary[13], ary[14], ary[15], ary[16], ary[17], ary[18]);
                    LTEMGString = Hex123intoDec(ary[19], ary[20], ary[21], ary[22], ary[23], ary[24]);
                    LCEMGString = Hex123intoDec(ary[25], ary[26], ary[27], ary[28], ary[29], ary[30]);

                    double LTdouble = Double.parseDouble(LTEMGString);
                    double LCdouble = Double.parseDouble(LCEMGString);
                    double RTdouble = Double.parseDouble(RTEMGString);
                    double RCdouble = Double.parseDouble(RCEMGString);

                    Log.d(TAG, "data =  " + buffer.toString());
                    Log.d(TAG, "Header0 = " + Header0.toString() + '\n' +
                            "Header1 =" + Header1 +
                            "LTdouble =" + LTdouble +
                            "LCdouble =" + LCdouble +
                            "RTdouble =" + RTdouble +
                            "RCdouble =" + RCdouble);

                    if (LTQ96Conter < 96) {
                        LTEMGqueue96.offer(LTdouble);
                        LTQ96Conter = LTQ96Conter + 1;

                    } else {
                        LTEMGqueue.offer(LTdouble);
                    }


                    if (LCQ96Conter < 96) {
                        LCEMGqueue96.offer(LCdouble);
                        LCQ96Conter = LCQ96Conter + 1;

                    } else {
                        LCEMGqueue.offer(LCdouble);
                    }


                    if (RTQ96Conter < 96) {
                        RTEMGqueue96.offer(RTdouble);
                        RTQ96Conter = RTQ96Conter + 1;

                    } else {
                        RTEMGqueue.offer(RTdouble);
                    }


                    if (RCQ96Conter < 96) {
                        RCEMGqueue96.offer(RCdouble);
                        RCQ96Conter = RCQ96Conter + 1;

                    } else {
                        RCEMGqueue.offer(RCdouble);
                    }

                    RTEMGString = Hex123intoDec(ary[31], ary[32], ary[33], ary[34], ary[35], ary[36]);
                    RCEMGString = Hex123intoDec(ary[37], ary[38], ary[39], ary[40], ary[41], ary[42]);

                    RTdouble = Double.parseDouble(RTEMGString);
                    RCdouble = Double.parseDouble(RCEMGString);

                    if (RTQ96Conter < 96) {
                        RTEMGqueue96.offer(RTdouble);
                        RTQ96Conter = RTQ96Conter + 1;

                    } else {
                        RTEMGqueue.offer(RTdouble);
                    }


                    if (RCQ96Conter < 96) {
                        RCEMGqueue96.offer(RCdouble);
                        RCQ96Conter = RCQ96Conter + 1;

                    } else {
                        RCEMGqueue.offer(RCdouble);
                    }


                }

            }




/*

            Message message1 = Message.obtain();
            //message.arg1 = Integer.parseInt(buffer.toString());
            message1.obj = buffer.toString();
            handlerCH1.sendMessage(message1);

*/
         if(setMaxBoolen == false){



            String LTEMGString;
            String LCEMGString;
            String RTEMGString;
            String RCEMGString;

            String StrBuffer;
            String Header0;
            int Header1;  //2

            StrBuffer = buffer.toString();
            String[] ary = StrBuffer.split("");
            Header0 = ary[3] + ary[4];
            Header1 = Integer.valueOf(ary[5] + ary[6], 16);





            if ((Header0.equals("a0")) && (Header1 == 20)) {

                LTEMGString = Hex123intoDec(ary[7], ary[8], ary[9], ary[10], ary[11], ary[12]);
                LCEMGString = Hex123intoDec(ary[13], ary[14], ary[15], ary[16], ary[17], ary[18]);
                RTEMGString = Hex123intoDec(ary[19], ary[20], ary[21], ary[22], ary[23], ary[24]);
                RCEMGString = Hex123intoDec(ary[25], ary[26], ary[27], ary[28], ary[29], ary[30]);

                double LTdouble = Double.parseDouble(LTEMGString);
                double LCdouble = Double.parseDouble(LCEMGString);
                double RTdouble = Double.parseDouble(RTEMGString);
                double RCdouble = Double.parseDouble(RCEMGString);

                LTarrayList.add(LTdouble);
                LCarrayList.add(LCdouble);
                RTarrayList.add(RTdouble);
                RCarrayList.add(RCdouble);

                LTEMGString = Hex123intoDec(ary[31], ary[32], ary[33], ary[34], ary[35], ary[36]);
                LCEMGString = Hex123intoDec(ary[37], ary[38], ary[39], ary[40], ary[41], ary[42]);
                LTdouble = Double.parseDouble(LTEMGString);
                LCdouble = Double.parseDouble(LCEMGString);
                LTarrayList.add(LTdouble);
                LCarrayList.add(LCdouble);

            }



            if (Header0.equals("a1") && (Header1 == 20)) {

                RTEMGString = Hex123intoDec(ary[7], ary[8], ary[9], ary[10], ary[11], ary[12]);
                RCEMGString = Hex123intoDec(ary[13], ary[14], ary[15], ary[16], ary[17], ary[18]);
                LTEMGString = Hex123intoDec(ary[19], ary[20], ary[21], ary[22], ary[23], ary[24]);
                LCEMGString = Hex123intoDec(ary[25], ary[26], ary[27], ary[28], ary[29], ary[30]);

                double LTdouble = Double.parseDouble(LTEMGString);
                double LCdouble = Double.parseDouble(LCEMGString);
                double RTdouble = Double.parseDouble(RTEMGString);
                double RCdouble = Double.parseDouble(RCEMGString);

                LTarrayList.add(LTdouble);
                LCarrayList.add(LCdouble);
                RTarrayList.add(RTdouble);
                RCarrayList.add(RCdouble);

                RTEMGString = Hex123intoDec(ary[31], ary[32], ary[33], ary[34], ary[35], ary[36]);
                RCEMGString = Hex123intoDec(ary[37], ary[38], ary[39], ary[40], ary[41], ary[42]);

                RTdouble = Double.parseDouble(RTEMGString);
                RCdouble = Double.parseDouble(RCEMGString);
                RTarrayList.add(RTdouble);
                RCarrayList.add(RCdouble);

            }


            }



    }





    /**
     * 與指定的設備建立連接
     *
     * @param device
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void connect(BluetoothDevice device) {

        mBtGatt = device.connectGatt(mContext, false, mBtGattCallback);
        mState = CONNECTTING;
        Log.d(TAG, "connectDevice");

    }



    /**
     * 開始BLE設備掃瞄
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void startScan() {
        mBluetoothAdapter.startLeScan(mLeScanCallback);
        Log.d(TAG, "Start Scan");
        // TX.setText("******目前狀態: 藍芽掃描中******");
    }

    /**
     * 停止BLE設備掃瞄
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void stopScan() {
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        //   TX.setText("******目前狀態:藍芽掃描關閉******");
        Log.d(TAG, "stop Scan");
    }






    //***********************資料庫函式**************
    private void openDB(){
        Log.d(TAG, "openDB");
        DH = new DBhelper(this);

    }


    private void closeDB(){
        Log.d(TAG, "closeDB");
        DH.close();

    }


    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        closeDB();

    }


    public void deleteAll()
    {
        SQLiteDatabase db = DH.getWritableDatabase();
        db.execSQL("DELETE FROM " + "Data_Base" );
    }


    private void add(){
        Log.d(TAG, "add");
        SQLiteDatabase db = DH.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("_DATE", _date.toString());
        values.put("_MODE", _mode.toString());
        values.put("_EXRPJECT", _exrpjct.toString());
        values.put("_GROUP", _group);
        values.put("_TIMES",_times);
        values.put("_LMUP", _lmup.toString());
        values.put("_LMDW", _lmdw.toString());
        values.put("_RMUP", _rmup.toString());
        values.put("_RMDW", _rmdw.toString());
        db.insert("Data_Base", null, values);
    }

//列出資料庫
    private void listAll(){
        SQLiteDatabase db = DH.getWritableDatabase();
        //取得資料庫的指標
        Cursor mCursor = db.rawQuery("SELECT * FROM "+"Data_Base", null);
        //將指標滑動到第一筆，取第一筆資料

        for(int i = 0 ; i < mCursor.getCount() ; i++ )
        {
            //利用for迴圈切換指標位置
            mCursor.moveToPosition(i);

            String _id = mCursor.getString(mCursor.getColumnIndex("_id"));
            String Cur_date = mCursor.getString(mCursor.getColumnIndex("_DATE")) ;  //時間
            String Cur_mode = mCursor.getString(mCursor.getColumnIndex("_MODE")) ;  //模式
            String Cur_exrpjct = mCursor.getString(mCursor.getColumnIndex("_EXRPJECT")) ; //運動項目
            int Cur_group = mCursor.getInt(mCursor.getColumnIndex("_GROUP"));     //組數
            int Cur_times = mCursor.getInt(mCursor.getColumnIndex("_TIMES"));;     //次數
            String Cur_lmup = mCursor.getString(mCursor.getColumnIndex("_LMUP")) ;  ;  //上斜方肌之最大EMG訊號
            String Cur_lmdw = mCursor.getString(mCursor.getColumnIndex("_LMDW")) ; ;  //下斜方肌之最大EMG訊號
            String Cur_rmup = mCursor.getString(mCursor.getColumnIndex("_RMUP")) ;  ;  //上斜方肌之最大EMG訊號
            String Cur_rmdw = mCursor.getString(mCursor.getColumnIndex("_RMDW")) ; ;  //下斜方肌之最大EMG訊號


            String string = "ID:"+_id+
                    "| 日期時間:"+Cur_date+
                    "| 模式:"+Cur_mode+
                    "| 項目:"+Cur_exrpjct+
                    "| 組數:"+Cur_group+
                    "| 次數:"+Cur_times+
                    "| 左上斜方肌:"+Cur_lmup+
                    "| 左下斜方肌:"+Cur_lmdw+
                    "| 右上斜方肌:"+Cur_rmup+
                    "| 右下斜方肌:"+Cur_rmdw
                    ;

            if(string2 =="")
            {
                textView.setText(string);
                string2 = string;
            }else {
                string2 = string2 + '\n' + string;
                textView.setText(string2);
            }
        }


    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        chart1.invalidate();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

//*************handler
    /**
     * EMG CH1
     */
    /*
    private class EMGCH1Handler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //int arg1 = msg.arg1;
            String name = (String) msg.obj;
            // TX2.append(name);
            EMGDataText1.setText("CH1: "+name);
        }
    }


    /**
     * EMG CH2
     */
    /*
    private class EMGCH2Handler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String name = (String) msg.obj;
            EMGDataText2.setText("CH2: " +name);
        }
    }

*/
    /**
     * 最大肌力倒數
     */
    private class CountDownHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //int arg1 = msg.arg1;
            String name = (String) msg.obj;
            // TX2.append(name);
            Toast.makeText(getApplicationContext(), "距離最大肌力色是結束尚餘" +  name + "秒", Toast.LENGTH_SHORT).show();
          //  EMGDataText1.setText("CH1: "+name);
        }
    }



    //16進位三數字組成16進位數字
    private String Hex123intoDec(String A,String B,String C,String D,String E,String F) {

        String Str ;
        Str = A+B+C+D+E+F;

        int DecInt = (Integer.parseInt( Str, 16 ));

        if(DecInt>8388608){
            DecInt=DecInt-16777216;}

        String DecStr;
        DecStr = String.valueOf(DecInt);

        return DecStr;
    }



    //建出window以及每0.25秒運算一次Window,之後就可以做小波了
    private void RateConter() {
        RateConterTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                //累積四秒計算一次window,之後每0.25秒計算一次四秒的window


                Log.d(TAG,    "進ReateCounter");

                 Log.d(TAG,    "LTWindow[ ]=" +  LTWindow[0] );



                for(int b =0;b<96;b++){


                    LTWindow[b] = LTEMGqueue96.poll();
                    LTEMGqueue96.offer( LTWindow[b]);

                    LCWindow[b] = LCEMGqueue96.poll();
                    LCEMGqueue96.offer( LCWindow[b]);

                    RTWindow[b] = RTEMGqueue96.poll();
                   // Log.d(TAG,    " RTWindow[b]" +  RTWindow[b] );
                    RTEMGqueue96.offer( RTWindow[b]);

                    RCWindow[b] = RCEMGqueue96.poll();
                    RCEMGqueue96.offer( RCWindow[b]);

                    //queueTxt.offer(Window[b]);
                    // Log.d(TAG,  "|" +  Window[b] + "|");
                    //  outputTxtLocker = false;
                    //Window[b] = butterworth.filter(Window[b]);
                    //  Log.d(TAG, "Filter後Window["+ b + "]: "+ Window[b]);

                }


                //Log.d(TAG, "+++++++++++++++繪圖前++++++++++++");

               /* for(int c=84; c<95;c++) {
                    draw2point[0] = Window[c];
                    draw2point[1] = Window[c+1];

                    Message draw =Message.obtain();
                    draw.obj = "1";
                    HandlerGraph.sendMessage(draw);
                }*/
                //拿windows運算或是拿Queue96運算
                //小波變換演算法


                //皆從後往前
               // ThresholdDouble = Threshold(Window);
                //找頭
                /*
                for(int d=0;d<96;d++){
                    if(Window[d]>ThresholdDouble){
                        WindowIndexHead = d;
                        break;
                    }
                }*/
                //找尾
                /*
                for(int e=95;e>-1;e--)
                {
                    if(Window[e]>ThresholdDouble){
                        WindowIndexRear = e;
                        break;
                    }
                }*/






                setData();
                setMVCData();
                for(int a = 0;a < 12;a++ ){


                    LTEMGqueue96.poll();
                    LTEMGqueue96.offer(LTEMGqueue.poll());

                    LCEMGqueue96.poll();
                    LCEMGqueue96.offer(LCEMGqueue.poll());

                    RTEMGqueue96.poll();
                    RTEMGqueue96.offer(RTEMGqueue.poll());

                    RCEMGqueue96.poll();
                    RCEMGqueue96.offer(RCEMGqueue.poll());


                }
            }


        }, 2500, 250/* 表示4000毫秒之後，每隔250毫秒執行一次 */);

    }

    //标准差σ=sqrt(s^2) //閥值為平均值+標準差*3
    public static double Threshold(double[] x) {
        int m = x.length;
        double[] y = new double[m];
        double sum = 0;
        for (int i = 0; i < m; i++) {//求和
            y[i] = x[i];
            if(y[i]<0){
                y[i] = y[i]*(-1);
            }
            sum += y[i];
        }
        double dAve = sum / m;//求平均值
        double dVar = 0;
        double StandaDivision =0;
        for (int i = 0; i < m; i++) {//求方差
            dVar += (y[i] - dAve) * (y[i] - dAve);
        }
        StandaDivision =  Math.sqrt(dVar / m);

        return (dAve + (StandaDivision*3));
    }


    //SETTimer
    private void setTimerTask() {
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerLocker = false;
                 Log.d(TAG, "Sample Rate = " + SampleRate );
                //  Log.d(TAG, "Timerlocker set false");
                SampleRate = 0;
            }
        }, 1000, 1000/* 表示1000毫秒之後，每隔1000毫秒執行一次 */);

    }



    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("coarse location permission granted");
                } else {
                    final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    private void setMaxCountDown(){

            mOffTime = new Timer(true);
            TimerTask tt = new TimerTask() {
                int countTime = 7;

                int theSizeLT ;
                int theSizeLC ;
                int theSizeRT ;
                int theSizeRC ;

                double LTsum ;
                double LCsum ;
                double RTsum ;
                double RCsum ;

                public void run() {
                    if (countTime > 0) {
                        countTime--;
                      //  Toast.makeText(getApplicationContext(), "距離最大肌力色是結束尚餘" + countTime + "秒", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "距離最大肌力測試結束尚餘" + countTime + "秒" );
                        Message message = Message.obtain();
                        //message.arg1 = Integer.parseInt(buffer.toString());
                        message.obj =Integer.toString(countTime);
                        countdown.sendMessage(message);
                    }


                    if (countTime == 2) {


                         theSizeLT = LTarrayList.size();
                         theSizeLC = LCarrayList.size();
                         theSizeRT = RTarrayList.size();
                         theSizeRC = RCarrayList.size();


                        for(int i=0;i<theSizeLT;i++){
                            double j=( double) LTarrayList.get(i);
                            LTsum+=j;
                        }

                        for(int i=0;i<theSizeLC;i++){
                            double j=( double) LCarrayList.get(i);
                            LCsum+=j;
                        }

                        for(int i=0;i<theSizeRT;i++){
                            double j=( double) RTarrayList.get(i);
                            RTsum+=j;
                        }

                        for(int i=0;i<theSizeRC;i++){
                            double j=( double) RCarrayList.get(i);
                            RCsum+=j;
                        }





                    }

                    if (countTime == 0) {
                        setMaxBoolen = true;
                        mOffTime.cancel();
                        Log.d(TAG, "最大肌力測試結束");

                        Log.d(TAG, "最大肌力測試結束"+'\n'+
                        "LTsum:"+LTsum/theSizeLT+'\n'+
                                        "LCsum:"+LCsum/theSizeLC+'\n'+
                                        "RTsum:"+RTsum/theSizeRT+'\n'+
                                        "RCsum:"+RCsum/theSizeRC+'\n'
                        );
                        LTarrayList = new ArrayList();
                        LCarrayList = new ArrayList();
                        RTarrayList = new ArrayList();
                        RCarrayList = new ArrayList();

                         _lmup = String.valueOf(LTsum/theSizeLT);  //上斜方肌之最大EMG訊號
                         _lmdw = String.valueOf(LCsum/theSizeLC);  //下斜方肌之最大EMG訊號
                         _rmup = String.valueOf(RTsum/theSizeRT);  //上斜方肌之最大EMG訊號
                         _rmdw = String.valueOf(RCsum/theSizeRC);  //下斜方肌之最大EMG訊號

                    }
                }
            };

            mOffTime.schedule(tt, 1000, 1000);

    }

    private void setData() {

        int count =96;


        ArrayList<Entry> values1 = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            float val = (float) LTWindow[i];
            values1.add(new Entry(i, val));
            // Log.d(TAG, "val="+val);
        }
        ArrayList<Entry> values2 = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            float val = (float) LCWindow[i];
            values2.add(new Entry(i, val));
        }


        ArrayList<Entry> values3 = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            float val = (float) RTWindow[i];
            values3.add(new Entry(i, val));
            // Log.d(TAG, "val="+val);
        }

        ArrayList<Entry> values4 = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            float val = (float) RCWindow[i];
            values4.add(new Entry(i, val));
        }

        LineDataSet set1, set2,set3,set4;

        if (chart1.getData() != null && chart1.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) chart1.getData().getDataSetByIndex(0);
            set2 = (LineDataSet) chart1.getData().getDataSetByIndex(1);

            set1.setValues(values1);
            set2.setValues(values2);

            chart1.getData().notifyDataChanged();
            chart1.notifyDataSetChanged();
            chart1.invalidate();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(values1, "LT EMG");

            set1.setAxisDependency(YAxis.AxisDependency.RIGHT);
            set1.setColor(ColorTemplate.getHoloBlue());
            set1.setCircleColor(Color.WHITE);
            set1.setLineWidth(2f);
            set1.setCircleRadius(3f);
            set1.setFillAlpha(65);
            set1.setFillColor(ColorTemplate.getHoloBlue());
            set1.setHighLightColor(Color.rgb(244, 117, 117));
            set1.setDrawCircleHole(false);
            set1.setDrawCircles(false);
            //set1.setFillFormatter(new MyFillFormatter(0f));
            //set1.setDrawHorizontalHighlightIndicator(false);
            //set1.setVisible(false);
            //set1.setCircleHoleColor(Color.WHITE);

            // create a dataset and give it a type
            set2 = new LineDataSet(values2, "LC EMG");
            set2.setAxisDependency(YAxis.AxisDependency.RIGHT);
            set2.setColor(Color.RED);
            set2.setCircleColor(Color.WHITE);
            set2.setLineWidth(2f);
            set2.setCircleRadius(3f);
            set2.setFillAlpha(65);
            set2.setFillColor(Color.RED);
            set2.setDrawCircleHole(false);
            set2.setHighLightColor(Color.rgb(244, 117, 117));
            //set2.setFillFormatter(new MyFillFormatter(900f));
            set2.setDrawCircles(false);


            // create a data object with the data sets
            LineData data = new LineData(set1, set2);
            data.setValueTextColor(Color.WHITE);
            data.setValueTextSize(9f);
            chart1.invalidate();
            chart2.invalidate();
            // set data
            chart1.setData(data);
        }



        if (chart2.getData() != null && chart2.getData().getDataSetCount() > 0) {
            set3 = (LineDataSet) chart2.getData().getDataSetByIndex(0);
            set4 = (LineDataSet) chart2.getData().getDataSetByIndex(1);

            set3.setValues(values3);
            set4.setValues(values4);

            chart2.getData().notifyDataChanged();
            chart2.notifyDataSetChanged();
            chart2.invalidate();
        } else {
            // create a dataset and give it a type
            set3 = new LineDataSet(values3, "RT EMG");

            set3.setAxisDependency(YAxis.AxisDependency.RIGHT);
            set3.setColor(ColorTemplate.getHoloBlue());
            set3.setCircleColor(Color.WHITE);
            set3.setLineWidth(2f);
            set3.setCircleRadius(3f);
            set3.setFillAlpha(65);
            set3.setFillColor(ColorTemplate.getHoloBlue());
            set3.setHighLightColor(Color.rgb(244, 117, 117));
            set3.setDrawCircleHole(false);
            set3.setDrawCircles(false);
            //set1.setFillFormatter(new MyFillFormatter(0f));
            //set1.setDrawHorizontalHighlightIndicator(false);
            //set1.setVisible(false);
            //set1.setCircleHoleColor(Color.WHITE);

            // create a dataset and give it a type
            set4 = new LineDataSet(values2, "RC EMG");
            set4.setAxisDependency(YAxis.AxisDependency.RIGHT);
            set4.setColor(Color.RED);
            set4.setCircleColor(Color.WHITE);
            set4.setLineWidth(2f);
            set4.setCircleRadius(3f);
            set4.setFillAlpha(65);
            set4.setFillColor(Color.RED);
            set4.setDrawCircleHole(false);
            set4.setHighLightColor(Color.rgb(244, 117, 117));
            //set2.setFillFormatter(new MyFillFormatter(900f));
            set4.setDrawCircles(false);


            // create a data object with the data sets
            LineData data = new LineData(set1, set2);
            data.setValueTextColor(Color.WHITE);
            data.setValueTextSize(9f);

            // create a data object with the data sets
            LineData data2 = new LineData(set3, set4);
            data2.setValueTextColor(Color.WHITE);
            data2.setValueTextSize(9f);

            chart1.invalidate();
            chart2.invalidate();
            // set data
            chart1.setData(data);
            chart2.setData(data2);
        }



    }




    private void setMVCData() {

        if ((!(_lmup.equals("null"))) && (!(_lmdw.equals("null"))) && (!(_rmup.equals("null"))) && (!(_rmdw.equals("null")))) {

            int count = 96;
            float flo_lmup = Float.parseFloat(_lmup);
            float flo_lmdw = Float.parseFloat(_lmdw);
            float flo_rmup = Float.parseFloat(_rmup);
            float flo_rmdw = Float.parseFloat(_rmdw);

            ArrayList<Entry> values1 = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                float val = (float) LTWindow[i] / flo_lmup;
                values1.add(new Entry(i, val));
                // Log.d(TAG, "val="+val);
            }
            ArrayList<Entry> values2 = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                float val = (float) LCWindow[i] / flo_lmdw;
                values2.add(new Entry(i, val));
            }


            ArrayList<Entry> values3 = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                float val = ((float) LTWindow[i] / flo_lmup) / ((float) LCWindow[i] / flo_lmdw);
                values3.add(new Entry(i, val));
            }


            ArrayList<Entry> values4 = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                float val = (float) RTWindow[i] / flo_rmup;
                values4.add(new Entry(i, val));
                // Log.d(TAG, "val="+val);
            }


            ArrayList<Entry> values5 = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                float val = (float) RCWindow[i] / flo_rmdw;
                values5.add(new Entry(i, val));
            }


            ArrayList<Entry> values6 = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                float val = ((float) RTWindow[i] / flo_rmup) / ((float) RCWindow[i] / flo_rmdw);
                values6.add(new Entry(i, val));
            }

            LineDataSet set1, set2, set3;

            if (chart3.getData() != null &&
                    chart3.getData().getDataSetCount() > 0) {
                set1 = (LineDataSet) chart3.getData().getDataSetByIndex(0);
                set2 = (LineDataSet) chart3.getData().getDataSetByIndex(1);
                set3 = (LineDataSet) chart3.getData().getDataSetByIndex(2);
                set1.setValues(values1);
                set2.setValues(values2);
                set3.setValues(values3);
                chart3.getData().notifyDataChanged();
                chart3.notifyDataSetChanged();
                chart3.invalidate();

            } else {
                // create a dataset and give it a type
                set1 = new LineDataSet(values1, "MCV LTEMG");

                set1.setAxisDependency(YAxis.AxisDependency.RIGHT);
                set1.setColor(ColorTemplate.getHoloBlue());
                set1.setCircleColor(Color.WHITE);
                set1.setLineWidth(2f);
                set1.setCircleRadius(3f);
                set1.setFillAlpha(65);
                set1.setFillColor(ColorTemplate.getHoloBlue());
                set1.setHighLightColor(Color.rgb(244, 117, 117));
                set1.setDrawCircleHole(false);
                //set1.setFillFormatter(new MyFillFormatter(0f));
                //set1.setDrawHorizontalHighlightIndicator(false);
                //set1.setVisible(false);
                //set1.setCircleHoleColor(Color.WHITE);
                set1.setDrawCircles(false);

                // create a dataset and give it a type
                set2 = new LineDataSet(values2, "MCV LCEMG");
                set2.setAxisDependency(YAxis.AxisDependency.RIGHT);
                set2.setColor(Color.RED);
                set2.setCircleColor(Color.WHITE);
                set2.setLineWidth(2f);
                set2.setCircleRadius(3f);
                set2.setFillAlpha(65);
                set2.setFillColor(Color.RED);
                set2.setDrawCircleHole(false);
                set2.setHighLightColor(Color.rgb(244, 117, 117));
                //set2.setFillFormatter(new MyFillFormatter(900f));
                set2.setDrawCircles(false);

                set3 = new LineDataSet(values3, "LT/LC");
                set3.setAxisDependency(YAxis.AxisDependency.LEFT);
                set3.setColor(Color.YELLOW);
                set3.setCircleColor(Color.WHITE);
                set3.setLineWidth(2f);
                set3.setCircleRadius(3f);
                set3.setFillAlpha(65);
                set3.setFillColor(ColorTemplate.colorWithAlpha(Color.YELLOW, 200));
                set3.setDrawCircleHole(false);
                set3.setHighLightColor(Color.rgb(244, 117, 117));
                set3.setDrawCircles(false);

                // create a data object with the data sets
                LineData data = new LineData(set1, set2, set3);
                data.setValueTextColor(Color.WHITE);
                data.setValueTextSize(9f);
                chart3.invalidate();
                chart3.setData(data);

            }


            LineDataSet set4, set5, set6;

            if (chart4.getData() != null &&
                    chart4.getData().getDataSetCount() > 0) {
                set4 = (LineDataSet) chart4.getData().getDataSetByIndex(0);
                set5 = (LineDataSet) chart4.getData().getDataSetByIndex(1);
                set6 = (LineDataSet) chart4.getData().getDataSetByIndex(2);
                set4.setValues(values4);
                set5.setValues(values5);
                set6.setValues(values6);
                chart4.getData().notifyDataChanged();
                chart4.notifyDataSetChanged();
                chart4.invalidate();

            } else {
                // create a dataset and give it a type
                set4 = new LineDataSet(values1, "MCV RTEMG");

                set4.setAxisDependency(YAxis.AxisDependency.RIGHT);
                set4.setColor(ColorTemplate.getHoloBlue());
                set4.setCircleColor(Color.WHITE);
                set4.setLineWidth(2f);
                set4.setCircleRadius(3f);
                set4.setFillAlpha(65);
                set4.setFillColor(ColorTemplate.getHoloBlue());
                set4.setHighLightColor(Color.rgb(244, 117, 117));
                set4.setDrawCircleHole(false);
                //set1.setFillFormatter(new MyFillFormatter(0f));
                //set1.setDrawHorizontalHighlightIndicator(false);
                //set1.setVisible(false);
                //set1.setCircleHoleColor(Color.WHITE);
                set4.setDrawCircles(false);

                // create a dataset and give it a type
                set5 = new LineDataSet(values2, "MCV RCEMG");
                set5.setAxisDependency(YAxis.AxisDependency.RIGHT);
                set5.setColor(Color.RED);
                set5.setCircleColor(Color.WHITE);
                set5.setLineWidth(2f);
                set5.setCircleRadius(3f);
                set5.setFillAlpha(65);
                set5.setFillColor(Color.RED);
                set5.setDrawCircleHole(false);
                set5.setHighLightColor(Color.rgb(244, 117, 117));
                //set2.setFillFormatter(new MyFillFormatter(900f));
                set5.setDrawCircles(false);

                set6 = new LineDataSet(values3, "RT/RC");
                set6.setAxisDependency(YAxis.AxisDependency.LEFT);
                set6.setColor(Color.YELLOW);
                set6.setCircleColor(Color.WHITE);
                set6.setLineWidth(2f);
                set6.setCircleRadius(3f);
                set6.setFillAlpha(65);
                set6.setFillColor(ColorTemplate.colorWithAlpha(Color.YELLOW, 200));
                set6.setDrawCircleHole(false);
                set6.setHighLightColor(Color.rgb(244, 117, 117));
                set6.setDrawCircles(false);

                // create a data object with the data sets
                LineData data2 = new LineData(set4, set5, set6);
                data2.setValueTextColor(Color.WHITE);
                data2.setValueTextSize(9f);
                chart4.invalidate();
                chart4.setData(data2);

            }


        }
    }



    private void setText5ListLast()
    {

        SQLiteDatabase db = DH.getWritableDatabase();
        //取得資料庫的指標
        Cursor mCursor = db.rawQuery("SELECT * FROM "+"Data_Base", null);
        //將指標滑動到第一筆，取第一筆資料
        String string1 ="null";
        boolean MVC = false;
        String Cur_lmup = "null";
        String Cur_lmdw = "null";
        String Cur_rmup = "null";
        String Cur_rmdw = "null";
        int Cur_group = 0;
        int Cur_times = 0;

        for(int i = 0 ; i < mCursor.getCount() ; i++ )
        {
            //利用for迴圈切換指標位置
            mCursor.moveToPosition(i);

            String _id = mCursor.getString(mCursor.getColumnIndex("_id"));
            String Cur_date = mCursor.getString(mCursor.getColumnIndex("_DATE")) ;  //時間
            String Cur_mode = mCursor.getString(mCursor.getColumnIndex("_MODE")) ;  //模式
            String Cur_exrpjct = mCursor.getString(mCursor.getColumnIndex("_EXRPJECT")) ; //運動項目
            Cur_group = mCursor.getInt(mCursor.getColumnIndex("_GROUP"));     //組數
            Cur_times = mCursor.getInt(mCursor.getColumnIndex("_TIMES"));;     //次數
            Cur_lmup = mCursor.getString(mCursor.getColumnIndex("_LMUP")) ;  ;  //上斜方肌之最大EMG訊號
            Cur_lmdw = mCursor.getString(mCursor.getColumnIndex("_LMDW")) ; ;  //下斜方肌之最大EMG訊號
            Cur_rmup = mCursor.getString(mCursor.getColumnIndex("_RMUP")) ;  ;  //上斜方肌之最大EMG訊號
            Cur_rmdw = mCursor.getString(mCursor.getColumnIndex("_RMDW")) ; ;  //下斜方肌之最大EMG訊號


            if(Cur_exrpjct.equals(_exrpjct)){
                 string1 = "上次運動一設定為:"
                        +"| 日期時間:"+Cur_date+'\n'+
                        "| 模式:"+Cur_mode+'\n'+
                        "| 項目:"+Cur_exrpjct+'\n'+
                        "| 組數:"+Cur_group+'\n'+
                        "| 次數:"+Cur_times;
                textView9.setText(Cur_group+"組");
                textView12.setText(Cur_times +"次");

            if(Cur_lmdw.equals("null")){
                MVC = false;
            }else {
                MVC = true;

            }

            }
            //"運動一", "運動二"

        }
        if(string1.equals("null")){
            Toast.makeText(getApplicationContext(), "運動:"+_exrpjct+"尚未設定"+'\n'+"請設定完再試", Toast.LENGTH_SHORT).show();

        }else if((!string1.equals("null")) && (MVC ==true)){
            Log.d(TAG, string1);
            textView5.setText(string1);

            _lmup = Cur_lmup ;  //上斜方肌之最大EMG訊號
            _lmdw = Cur_lmdw ;  //下斜方肌之最大EMG訊號
            _rmup = Cur_rmup ;  //上斜方肌之最大EMG訊號
            _rmdw = Cur_rmdw;  //下斜方肌之最大EMG訊號





        }else {   Toast.makeText(getApplicationContext(), "運動:"+_exrpjct+"之最大肌力尚未設定"+'\n'+"請設定完再試", Toast.LENGTH_SHORT).show();}

    }

}
