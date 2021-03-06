package com.evolvan.timegrid;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

public class MyTask extends Fragment implements View.OnClickListener,ViewPager.OnPageChangeListener {

    AlertDialog.Builder builder;
    AlertDialog alert;

    Button ButtonAddUpdate,ButtonCancel;
    static TextView Dayofweektxt,StartTime,EndTime;
    int[] intEStime,intEEtime;

    Boolean timeExist;

    static EditText AlermBefore;
    static AutoCompleteTextView Subject,Venue;
    static CheckBox AlermRepeat,Allday;

    ViewPager pager;
    int pageposition;

    LinearLayout layout;
    Animation slideUp,slideDown;
    int TimeFlag=0,intstart,intend;
    String getTime,Strday,StrStartTime,StrEndTime,StrSubject,StrVenue,StrAlembefor,Error="Field Cannot be empty!",getdataposition;
    Snackbar snackbar1;
    SQLiteDatabase SQLITEDATABASE;
    SQLiteHelper SQLITEHELPER;
    Cursor cursor;
    ArrayList<String> autosub=new ArrayList<String>();
    ArrayList<String> autoven=new ArrayList<String>();
    ArrayAdapter<String> sub;
    ArrayAdapter<String> ven;

    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "MyPREFERENCES" ;
    public static final String StoreId = "StoreId";
    public static final String AddUpdateFlag = "AddUpdateFlag";
    private final String DefaultUnameValue = "";
    private final String DefaultInsertUpdateValue = "";
    static  String getStoreId;
    private String InsertUpdateStoreId;
    String insertdata="INSERTDATA", updatedata="UPDATE";


    private String tabtitles[] = new String[] { "Monday", "Tuesday", "Wednesday", "Thursday","Friday","Saturday","Sunday" };
    int a,updatestart=0,updateend=0,storeupdateend=0,storeupdatestart=0, hour,minute ;
    String Stimeid = null,Etimeid=null;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(com.evolvan.timegrid.R.string.Day_View);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View views= inflater.inflate(com.evolvan.timegrid.R.layout.fragment_mytask, container, false);
        sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        SQLITEHELPER = new SQLiteHelper(getActivity());
        pager=(ViewPager) views.findViewById(com.evolvan.timegrid.R.id.pager);
        pager.setAdapter(new ViewPagerAdapter(getChildFragmentManager()));
        pager.getAdapter().notifyDataSetChanged();
        pager.addOnPageChangeListener(this);

        ButtonAddUpdate=(Button) views.findViewById(com.evolvan.timegrid.R.id.ButtonAddUpdate);
        ButtonCancel=(Button) views.findViewById(com.evolvan.timegrid.R.id.ButtonCancel);

        Dayofweektxt =(TextView)views.findViewById(com.evolvan.timegrid.R.id.Dayofweek);
        StartTime=(TextView)views.findViewById(com.evolvan.timegrid.R.id.StartTime);
        EndTime=(TextView)views.findViewById(com.evolvan.timegrid.R.id.EndTime);
        StartTime.setOnClickListener(this);
        EndTime.setOnClickListener(this);

        Subject=(AutoCompleteTextView) views.findViewById(com.evolvan.timegrid.R.id.Subject);
        Venue=(AutoCompleteTextView) views.findViewById(com.evolvan.timegrid.R.id.Venue);
        AlermBefore=(EditText)views.findViewById(com.evolvan.timegrid.R.id.AlermBefore);
        AlermBefore.setFilters(new InputFilter[]{new InputFilterMinMax("1", "120")});

        AlermRepeat=(CheckBox)views.findViewById(com.evolvan.timegrid.R.id.AlermRepeat);
        Allday=(CheckBox)views.findViewById(com.evolvan.timegrid.R.id.Allday);

        sub = new ArrayAdapter<String>(getContext(),android.R.layout.select_dialog_item,autosub);
        ven = new ArrayAdapter<String>(getContext(),android.R.layout.select_dialog_item,autoven);

        Subject.setThreshold(1);
        Subject.setAdapter(sub);
        Venue.setThreshold(1);
        Venue.setAdapter(ven);

        ButtonAddUpdate.setOnClickListener(this);
        ButtonCancel.setOnClickListener(this);

        Subject.setOnClickListener(this);
        Venue.setOnClickListener(this);
        Allday.setOnClickListener(this);

        return views;
    }

    public class InputFilterMinMax implements InputFilter {
        private int min, max;

        public InputFilterMinMax(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public InputFilterMinMax(String min, String max) {
            this.min = Integer.parseInt(min);
            this.max = Integer.parseInt(max);
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            try {
                int input = Integer.parseInt(dest.toString() + source.toString());
                if (isInRange(min, max, input))
                    return null;
            } catch (NumberFormatException nfe) { }
            return "";
        }
        private boolean isInRange(int a, int b, int c) {
            return b > a ? c >= a && c <= b : c >= b && c <= a;
        }
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        DBCreate();
        cursor = SQLITEDATABASE.rawQuery("SELECT * FROM " + SQLITEHELPER.TABLE_NAME, null);
        if(cursor.getCount()!=0) {
            if (pager.getChildCount() > 0) {
                Calendar calendar = Calendar.getInstance();
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                Dayofweektxt.setText(tabtitles[dayOfWeek - 2]);
                getdataposition = tabtitles[dayOfWeek - 2];

                if (Calendar.MONDAY == dayOfWeek) {
                    pager.setCurrentItem(0, true);
                } else if (Calendar.TUESDAY == dayOfWeek) {
                    pager.setCurrentItem(1, true);
                } else if (Calendar.WEDNESDAY == dayOfWeek) {
                    pager.setCurrentItem(2, true);
                } else if (Calendar.THURSDAY == dayOfWeek) {
                    pager.setCurrentItem(3, true);
                } else if (Calendar.FRIDAY == dayOfWeek) {
                    pager.setCurrentItem(4, true);
                } else if (Calendar.SATURDAY == dayOfWeek) {
                    pager.setCurrentItem(5, true);
                } else if (Calendar.SUNDAY == dayOfWeek) {
                    pager.setCurrentItem(6, true);
                }
            }
            autocomplete();
            AddData();
       }
        else {
            ((MainActivity) getActivity()).WhenNullRecord();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(com.evolvan.timegrid.R.menu.add_landscape, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        layout = (LinearLayout) getActivity().findViewById(com.evolvan.timegrid.R.id.updatelayout);
        slideUp = AnimationUtils.loadAnimation(getContext(), com.evolvan.timegrid.R.anim.slide_up);
        switch(item.getItemId()) {
            case com.evolvan.timegrid.R.id.action_add: {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(AddUpdateFlag, insertdata);
                editor.commit();
                Button b = (Button) layout.findViewById(com.evolvan.timegrid.R.id.ButtonAddUpdate);
                b.setText(com.evolvan.timegrid.R.string.Add);
                layout.setVisibility(View.VISIBLE);
                layout.startAnimation(slideUp);
                Allday.setVisibility(View.VISIBLE);
                AddData();
                break;
            }
            case com.evolvan.timegrid.R.id.action_LANDSCAPE: {
                ((MainActivity)getActivity()).WhenLandScape();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        layout = (LinearLayout) getActivity().findViewById(com.evolvan.timegrid.R.id.updatelayout);
        slideDown = AnimationUtils.loadAnimation(getContext(), com.evolvan.timegrid.R.anim.slide_down);
        switch (view.getId())
        {
            case com.evolvan.timegrid.R.id.ButtonAddUpdate:{
                AddorUpdateData();
                break;
            }
            case com.evolvan.timegrid.R.id.ButtonCancel: {
                layout.startAnimation(slideDown);
                layout.setVisibility(View.GONE);
                break;
            }
            case com.evolvan.timegrid.R.id.StartTime:{
                TimeFlag=789;
                SetTime();
                break;
            }
            case com.evolvan.timegrid.R.id.EndTime:{
                TimeFlag=456;
                SetTime();
                break;
            }
            case com.evolvan.timegrid.R.id.Subject:{
                autocomplete();
                break;
            }
            case com.evolvan.timegrid.R.id.Venue:{
                autocomplete();
                break;
            }
            case com.evolvan.timegrid.R.id.Allday:{
                if(Allday.isChecked()){
                    Dayofweektxt.setVisibility(View.GONE);
                }
                else {
                    Dayofweektxt.setVisibility(View.VISIBLE);
                }
                break;
            }
        }
    }

    public void SetTime(){
        final Calendar mcurrentTime = Calendar.getInstance();
        final int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker= new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                int inthour = selectedHour;
                int intminute=selectedMinute;
                getTime=String.format("%02d:%02d %s", inthour == 0 ? 12 : inthour, intminute, inthour < 12 ? "AM" : "PM");
                if(TimeFlag==789) {
                    StartTime.setText(getTime);
                    intstart = inthour * 60 + intminute;
                }
                else if(TimeFlag==456) {
                    EndTime.setText(getTime);
                    intend=inthour * 60 + intminute;
                }
            }
        }, hour, minute, true);
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    public void AddorUpdateData() {
        getStoreId="";
        getStoreId= sharedpreferences.getString(StoreId, DefaultUnameValue);
        InsertUpdateStoreId= sharedpreferences.getString(AddUpdateFlag, DefaultInsertUpdateValue);

        Strday= Dayofweektxt.getText().toString();
        StrStartTime=StartTime.getText().toString();
        StrEndTime=EndTime.getText().toString();
        StrSubject=Subject.getText().toString();
        StrVenue=Venue.getText().toString();
        StrAlembefor=AlermBefore.getText().toString();

        if(TextUtils.isEmpty(Strday) || Strday.length()==0){
            snackbar1 = Snackbar.make(getView(), "Day of Week Cannot be empty!", Snackbar.LENGTH_SHORT);snackbar1.show();
        }
        else if(TextUtils.isEmpty(StrStartTime)  || StrStartTime.length()==0){
            snackbar1 = Snackbar.make(getView(), "Start Time Cannot be empty!", Snackbar.LENGTH_SHORT);snackbar1.show();
        }
        else if(TextUtils.isEmpty(StrEndTime) || StrEndTime.length()==0){
            snackbar1 = Snackbar.make(getView(), "End Time Cannot be empty!", Snackbar.LENGTH_SHORT);snackbar1.show();
        }
        else if(intstart>=intend || intstart==0 || StrStartTime.length()==0 || StrEndTime.length()==0){
            snackbar1 = Snackbar.make(getView(), "Start Time Cannot be higher than or Equals to End Time.", Snackbar.LENGTH_SHORT);snackbar1.show();
        }
        else if(TextUtils.isEmpty(StrSubject) || StrSubject.length()==0) {
            Subject.requestFocus();
            Subject.setError(Error);
        }
        else if(TextUtils.isEmpty(StrVenue) || StrVenue.length()==0) {
            Venue.requestFocus();
            Venue.setError(Error);
        }
        else if((StrAlembefor.length()==0 || TextUtils.isEmpty(StrAlembefor)) &&(AlermRepeat.isChecked())) {
            AlermBefore.requestFocus();
            AlermBefore.setError(Error);
        }
        else if((!StrAlembefor.equals("") || StrAlembefor.length()!=0) && !AlermRepeat.isChecked()){
            snackbar1 = Snackbar.make(getView(), "Tick Checkbox!", Snackbar.LENGTH_SHORT);
            snackbar1.show();
        }
        else {
            if(StrAlembefor.length()==0 || TextUtils.isEmpty(StrAlembefor)){
                StrAlembefor="00";
            }
            String[] SplitStrStartTime = StrStartTime.split(" ");
            String[] SplitStrEndTime = StrEndTime.split(" ");
            String SStrStartTime = SplitStrStartTime[0];
            String SStrEndTime= SplitStrEndTime[0];
            if(StrStartTime.length()!=0 && StrEndTime.length()!=0) {
                Date dateStrStartTime = new Date();
                dateStrStartTime.setTime((((Integer.parseInt(SStrStartTime.split(":")[0])) * 60 + (Integer.parseInt(SStrStartTime.split(":")[1]))) + dateStrStartTime.getTimezoneOffset()) * 60000);
                Date dateStrEndTime = new Date();
                dateStrEndTime.setTime((((Integer.parseInt(SStrEndTime.split(":")[0])) * 60 + (Integer.parseInt(SStrEndTime.split(":")[1]))) + dateStrEndTime.getTimezoneOffset()) * 60000);
                intstart = dateStrStartTime.getHours() * 60 + dateStrStartTime.getMinutes();
                intend = dateStrEndTime.getHours() * 60 + dateStrEndTime.getMinutes();
            }

            TimeExistOrNot(intstart,intend);

            if (insertdata.equals(InsertUpdateStoreId)) {
                if(timeExist==true){
                    Toast.makeText(getActivity(),"Timing  Already Exists in Table.", Toast.LENGTH_LONG).show();
                }
                else {
                    InsertDataInTable(Strday, StrStartTime, StrEndTime, StrSubject, StrVenue, StrAlembefor);
                    changeoccur();
                }
            }
            else if (updatedata.equals(InsertUpdateStoreId)) {
                if (getStoreId.isEmpty()) {
                    snackbar1 = Snackbar.make(getView(), "Error Add data first!", Snackbar.LENGTH_SHORT);
                    snackbar1.show();
                }
                else {
                    if(timeExist==true){

                        UpdateDataInTableValidate(intstart, intend, getStoreId, StrStartTime, StrEndTime, StrSubject, StrVenue, StrAlembefor);
                    }
                    else {

                        UpdateDataInTable(getStoreId, StrStartTime, StrEndTime, StrSubject, StrVenue, StrAlembefor);
                        changeoccur();

                    }
                }
            }
            else {
                Toast.makeText(getActivity(),"Checkout error  :Something went wrong!", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void changeoccur(){
        layout.startAnimation(slideDown);
        layout.setVisibility(View.GONE);
        pager.setAdapter(new ViewPagerAdapter(getChildFragmentManager()));
        pager.setCurrentItem(pageposition);
    }

    public void DBCreate(){
        SQLITEDATABASE = getActivity().openOrCreateDatabase(SQLITEHELPER.DATABASE_NAME, MODE_PRIVATE, null);
        String CREATE_WEEKTABLE = "CREATE TABLE IF NOT EXISTS " + SQLITEHELPER.TABLE_NAME + " (" + SQLITEHELPER.KEY_ID + " INTEGER PRIMARY KEY NOT NULL, "+ SQLITEHELPER.KEY_DOWeek + " VARCHAR NOT NULL, " + SQLITEHELPER.KEY_STime + " VARCHAR NOT NULL, " + SQLITEHELPER.KEY_ETime + " VARCHAR NOT NULL, " + SQLITEHELPER.KEY_Subject + " VARCHAR NOT NULL, " + SQLITEHELPER.KEY_Venue + " VARCHAR NOT NULL , " + SQLITEHELPER.KEY_AlermBefor + " VARCHAR)";
        SQLITEDATABASE.execSQL(CREATE_WEEKTABLE);
        if(SQLITEDATABASE.isOpen()) {}
        else {DBCreate();}
    }

    public void InsertDataInTable(String Strday,String StrStartTime,String StrEndTime,String StrSubject,String StrVenue,String StrAlembefor) {
        if(Allday.isChecked()){
                for(int i=0;i<7;i++) {
                    String SQLiteQueryWEEKTABLE = "INSERT or replace INTO " + SQLITEHELPER.TABLE_NAME + " " + "(" + SQLITEHELPER.KEY_DOWeek + "," + SQLITEHELPER.KEY_STime + "," + SQLITEHELPER.KEY_ETime + "," + SQLITEHELPER.KEY_Subject + "," + SQLITEHELPER.KEY_Venue + "," + SQLITEHELPER.KEY_AlermBefor + ")" + " VALUES('" + tabtitles[i] + "', '" + StrStartTime + "', '" + StrEndTime + "', '" + StrSubject + "', '" + StrVenue + "' , '" + StrAlembefor + "');";
                    SQLITEDATABASE.execSQL(SQLiteQueryWEEKTABLE);
                }
            }
            else {
                String SQLiteQueryWEEKTABLE = "INSERT or replace INTO " + SQLITEHELPER.TABLE_NAME + " " + "(" + SQLITEHELPER.KEY_DOWeek + "," + SQLITEHELPER.KEY_STime + "," + SQLITEHELPER.KEY_ETime + "," + SQLITEHELPER.KEY_Subject + "," + SQLITEHELPER.KEY_Venue + " ," + SQLITEHELPER.KEY_AlermBefor + ")" + " VALUES('" + Strday + "', '" + StrStartTime + "', '" + StrEndTime + "', '" + StrSubject + "', '" + StrVenue + "' , '" + StrAlembefor + "');";
                SQLITEDATABASE.execSQL(SQLiteQueryWEEKTABLE);
                snackbar1 = Snackbar.make(getView(), "Inserted Successfully", Snackbar.LENGTH_SHORT);
                snackbar1.show();
            }
        ((MainActivity)getActivity()).startService();
    }

    public void UpdateDataInTable(String getStoreId,String StrStartTime,String StrEndTime,String StrSubject,String StrVenue,String StrAlembefor) {
        updateScheduleTimeId(StrStartTime, StrEndTime,StrSubject,StrVenue,StrAlembefor, getStoreId);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

    @Override
    public void onPageSelected(int position) {

        pageposition=position;
        getdataposition=tabtitles[position];
        Dayofweektxt.setText(tabtitles[position]);
        autocomplete();
        AddData();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        //pageposition = state;
    }

    public void autocomplete() {
        SQLITEDATABASE = getActivity().openOrCreateDatabase(SQLITEHELPER.DATABASE_NAME, MODE_PRIVATE, null);
        cursor = SQLITEDATABASE.rawQuery("SELECT " + SQLITEHELPER.KEY_Subject +"," + SQLITEHELPER.KEY_Venue + " FROM " + SQLITEHELPER.TABLE_NAME +" GROUP BY " + SQLITEHELPER.KEY_Subject + " ORDER BY " + SQLITEHELPER.KEY_Subject + " DESC", null);
        autosub.clear();
        autoven.clear();
        while (cursor != null && cursor.moveToNext()) {
            autosub.add(cursor.getString(cursor.getColumnIndex(SQLiteHelper.KEY_Subject)));
            autoven.add(cursor.getString(cursor.getColumnIndex(SQLiteHelper.KEY_Venue)));
        }
    }

    public void AddData() {

        SQLITEDATABASE = getActivity().openOrCreateDatabase(SQLITEHELPER.DATABASE_NAME, MODE_PRIVATE, null);
        cursor = SQLITEDATABASE.rawQuery("SELECT * FROM " + SQLITEHELPER.TABLE_NAME + " WHERE  " + SQLITEHELPER.KEY_DOWeek + " = '" + getdataposition + "' ORDER BY " + SQLITEHELPER.KEY_STime + " ASC ", null);
        if(cursor.getCount()==0) {
            cursor = SQLITEDATABASE.rawQuery("SELECT * FROM " + SQLITEHELPER.TABLE_NAME + " ORDER BY " + SQLITEHELPER.KEY_STime + " ASC ", null);
        }
            while (cursor != null && cursor.moveToNext()) {
                String Stime = cursor.getString(cursor.getColumnIndex(SQLiteHelper.KEY_STime));
                String Etime = cursor.getString(cursor.getColumnIndex(SQLiteHelper.KEY_ETime));
                String[] SplitStime = Stime.split(" ");
                String[] SplitEtime = Etime.split(" ");
                String StineSplitStime = SplitStime[0];
                String EtineSplitEtime = SplitEtime[0];
                Date date1 = new Date();
                date1.setTime((((Integer.parseInt(StineSplitStime.split(":")[0])) * 60 + (Integer.parseInt(StineSplitStime.split(":")[1]))) + date1.getTimezoneOffset()) * 60000);
                Date date2 = new Date();
                date2.setTime((((Integer.parseInt(EtineSplitEtime.split(":")[0])) * 60 + (Integer.parseInt(EtineSplitEtime.split(":")[1]))) + date2.getTimezoneOffset()) * 60000);
                int shour = date1.getHours();
                int smint = date1.getMinutes();
                int ehour = date2.getHours();
                int emint = date2.getMinutes();
                int sdiff = shour * 60 + smint;
                int Ediff = ehour * 60 + emint;
                int diff = Ediff - sdiff;
                Date date3 = new Date();
                date3.setTime(date2.getTime() + (diff * 60000));
                int hour = date3.getHours();
                int mint = date3.getMinutes();
                intstart = shour * 60 + smint;
                intend = hour * 60 + mint;
                StartTime.setText(cursor.getString(cursor.getColumnIndex(SQLiteHelper.KEY_ETime)));
                EndTime.setText(String.format("%02d:%02d %s", hour == 0 ? 12 : hour, mint, hour < 12 ? "AM" : "PM"));
                Subject.setText(cursor.getString(cursor.getColumnIndex(SQLiteHelper.KEY_Subject)));
                Venue.setText(cursor.getString(cursor.getColumnIndex(SQLiteHelper.KEY_Venue)));

            }
    }

    public void TimeExistOrNot(int intstart, int intend) {
        SQLITEDATABASE = getActivity().openOrCreateDatabase(SQLITEHELPER.DATABASE_NAME, MODE_PRIVATE, null);
        cursor = SQLITEDATABASE.rawQuery("SELECT * FROM " + SQLITEHELPER.TABLE_NAME + " WHERE  " + SQLITEHELPER.KEY_DOWeek + " = '" + getdataposition + "' ORDER BY " + SQLITEHELPER.KEY_STime + " ASC ", null);
        intEStime=new int[cursor.getCount()];
        intEEtime=new int[cursor.getCount()];
        a = 0;
        while (cursor != null && cursor.moveToNext()) {
            String Stime = cursor.getString(cursor.getColumnIndex(SQLiteHelper.KEY_STime));
            String Etime = cursor.getString(cursor.getColumnIndex(SQLiteHelper.KEY_ETime));
            String[] SplitStime = Stime.split(" ");
            String[] SplitEtime = Etime.split(" ");
            String StineSplitStime = SplitStime[0];
            String EtineSplitEtime = SplitEtime[0];
            Date dateStime = new Date();
            dateStime.setTime((((Integer.parseInt(StineSplitStime.split(":")[0])) * 60 + (Integer.parseInt(StineSplitStime.split(":")[1]))) + dateStime.getTimezoneOffset()) * 60000);
            Date dateEtime = new Date();
            dateEtime.setTime((((Integer.parseInt(EtineSplitEtime.split(":")[0])) * 60 + (Integer.parseInt(EtineSplitEtime.split(":")[1]))) + dateEtime.getTimezoneOffset()) * 60000);
            intEStime[a]=dateStime.getHours()*60+dateStime.getMinutes();
            intEEtime[a]=dateEtime.getHours()*60+dateEtime.getMinutes();
            a++;
        }
        if(cursor.getCount()==0){
            timeExist = false;
        }
        else {
            for (int j = 0; j < intEStime.length; j++) {
                if (intstart >= intEStime[j] && intstart < intEEtime[j]) {
                    timeExist = true;
                    break;
                } else if (intend > intEStime[j] && intend < intEEtime[j]) {
                    timeExist = true;
                    break;
                } else if (intstart < intEStime[j] && intend > intEEtime[j]) {
                    timeExist = true;
                    break;
                } else{
                    timeExist = false;
                }
            }
        }
    }

    public void UpdateDataInTableValidate(final int intstart, final int intend, final String getStoreId, final String StrStartTime, final String StrEndTime, final String StrSubject, final String StrVenue, final String StrAlembefor){
        SQLITEDATABASE = getActivity().openOrCreateDatabase(SQLITEHELPER.DATABASE_NAME, MODE_PRIVATE, null);
        cursor= SQLITEDATABASE.rawQuery("SELECT * FROM " + SQLITEHELPER.TABLE_NAME + " WHERE  " + SQLITEHELPER.KEY_DOWeek + " = '" + getdataposition + "' AND " + SQLITEHELPER.KEY_ID + " = '" + getStoreId + "' ORDER BY " + SQLITEHELPER.KEY_STime + " ASC ", null);
        while (cursor != null && cursor.moveToNext()) {
            Stimeid= cursor.getString(cursor.getColumnIndex(SQLiteHelper.KEY_STime));
            Etimeid= cursor.getString(cursor.getColumnIndex(SQLiteHelper.KEY_ETime));
            String[] SplitStime = Stimeid.split(" ");
            String[] SplitEtime=Etimeid.split(" ");
            String StineSplitStime = SplitStime[0];
            String EtineSplitEtime = SplitEtime[0];
            Date dateStime = new Date();
            dateStime.setTime((((Integer.parseInt(StineSplitStime.split(":")[0])) * 60 + (Integer.parseInt(StineSplitStime.split(":")[1]))) + dateStime.getTimezoneOffset()) * 60000);
            updatestart = dateStime.getHours()*60+dateStime.getMinutes();

            Date dateEtime = new Date();
            dateEtime.setTime((((Integer.parseInt(EtineSplitEtime.split(":")[0])) * 60 + (Integer.parseInt(EtineSplitEtime.split(":")[1]))) + dateEtime.getTimezoneOffset()) * 60000);
            updateend = dateEtime.getHours()*60+dateEtime.getMinutes();
        }

            if (((intstart >= updatestart && intstart < updateend) && (intend > updatestart && intend <=updateend)) ) {
                updateScheduleTimeId(StrStartTime, StrEndTime,StrSubject,StrVenue,StrAlembefor, getStoreId);
            }
            else if(intstart < updatestart && intend<=updateend){
                builder = new AlertDialog.Builder(getContext());
                builder.setMessage("WARNING :Upper Timing Overlapping !" + "\n" + "clicking on 'Push' to Push timing 'Upper'  Which are Overlapping Start Time.")
                        .setCancelable(false)
                        .setPositiveButton("Push", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                cursor = SQLITEDATABASE.rawQuery("SELECT * FROM " + SQLITEHELPER.TABLE_NAME + " WHERE  " + SQLITEHELPER.KEY_DOWeek + " = '" + getdataposition + "' AND " + SQLITEHELPER.KEY_STime + " < '" + Stimeid + "' ORDER BY " + SQLITEHELPER.KEY_STime + " ASC ", null);
                                while (cursor != null && cursor.moveToNext()) {
                                    storeupdatestart = updatestart - intstart;
                                    String Stime = cursor.getString(cursor.getColumnIndex(SQLiteHelper.KEY_STime));
                                    String Etime = cursor.getString(cursor.getColumnIndex(SQLiteHelper.KEY_ETime));
                                    String strId = cursor.getString(cursor.getColumnIndex(SQLiteHelper.KEY_ID));

                                    String[] SplitStime = Stime.split(" ");
                                    String[] SplitEtime = Etime.split(" ");
                                    String StineSplitStime = SplitStime[0];
                                    String EtineSplitEtime = SplitEtime[0];

                                    Date dateStime = new Date();
                                    dateStime.setTime((((Integer.parseInt(StineSplitStime.split(":")[0])) * 60 + (Integer.parseInt(StineSplitStime.split(":")[1]))) + dateStime.getTimezoneOffset()) * 60000);
                                    dateStime.setTime(dateStime.getTime() - (storeupdatestart * 60000));
                                    String ust = String.format("%02d:%02d %s", dateStime.getHours() == 0 ? 12 : dateStime.getHours(), dateStime.getMinutes(), dateStime.getHours() < 12 ? "AM" : "PM");

                                    Date dateEtime = new Date();
                                    dateEtime.setTime((((Integer.parseInt(EtineSplitEtime.split(":")[0])) * 60 + (Integer.parseInt(EtineSplitEtime.split(":")[1]))) + dateEtime.getTimezoneOffset()) * 60000);
                                    dateEtime.setTime(dateEtime.getTime() - (storeupdatestart * 60000));
                                    String uet = String.format("%02d:%02d %s", dateEtime.getHours() == 0 ? 12 : dateEtime.getHours(), dateEtime.getMinutes(), dateEtime.getHours() < 12 ? "AM" : "PM");

                                    updateScheduleTime(ust, uet, strId);
                                }
                                if (cursor.getCount() != 0) {
                                    updateScheduleTimeId(StrStartTime, StrEndTime,StrSubject,StrVenue,StrAlembefor, getStoreId);
                                }

                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                alert = builder.create();
                alert.show();
            }
            else if(intstart >= updatestart && intend>updateend){
                builder = new AlertDialog.Builder(getContext());
                builder.setMessage("WARNING :Below Timing Overlapping !" + "\n" + "clicking on 'Push' to Push timing 'Below'  Which are Overlapping End Time.")
                        .setCancelable(false)
                        .setPositiveButton("Push", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                cursor= SQLITEDATABASE.rawQuery("SELECT * FROM " + SQLITEHELPER.TABLE_NAME + " WHERE  " + SQLITEHELPER.KEY_DOWeek + " = '" + getdataposition + "' AND "+ SQLITEHELPER.KEY_STime + " >= '" + Etimeid + "' ORDER BY " + SQLITEHELPER.KEY_STime + " ASC ", null);
                                while (cursor != null && cursor.moveToNext()) {
                                    storeupdateend=intend-updateend;
                                    String Stime= cursor.getString(cursor.getColumnIndex(SQLiteHelper.KEY_STime));
                                    String Etime = cursor.getString(cursor.getColumnIndex(SQLiteHelper.KEY_ETime));
                                    String strId = cursor.getString(cursor.getColumnIndex(SQLiteHelper.KEY_ID));
                                    String[] SplitStime = Stime.split(" ");
                                    String[] SplitEtime = Etime.split(" ");
                                    String StineSplitStime = SplitStime[0];
                                    String EtineSplitEtime = SplitEtime[0];
                                    Date dateStime = new Date();
                                    dateStime.setTime((((Integer.parseInt(StineSplitStime.split(":")[0])) * 60 + (Integer.parseInt(StineSplitStime.split(":")[1]))) + dateStime.getTimezoneOffset()) * 60000);
                                    dateStime.setTime(dateStime.getTime()+(storeupdateend*60000));
                                    String ust=String.format("%02d:%02d %s", dateStime.getHours() == 0 ? 12 : dateStime.getHours(), dateStime.getMinutes(), dateStime.getHours() < 12 ? "AM" : "PM");

                                    Date dateEtime = new Date();
                                    dateEtime.setTime((((Integer.parseInt(EtineSplitEtime.split(":")[0])) * 60 + (Integer.parseInt(EtineSplitEtime.split(":")[1]))) + dateEtime.getTimezoneOffset()) * 60000);
                                    dateEtime.setTime(dateEtime.getTime()+(storeupdateend*60000));
                                    String uet=String.format("%02d:%02d %s", dateEtime.getHours() == 0 ? 12 : dateEtime.getHours(), dateEtime.getMinutes(), dateEtime.getHours() < 12 ? "AM" : "PM");

                                    updateScheduleTime(ust, uet, strId);
                                }
                                if(cursor.getCount()!=0) {
                                    updateScheduleTimeId(StrStartTime, StrEndTime,StrSubject,StrVenue,StrAlembefor, getStoreId);
                                }

                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                alert = builder.create();
                alert.show();
            }


            else if(intstart < updatestart && intend>updateend){
                builder = new AlertDialog.Builder(getContext());
                builder.setMessage("WARNING :Upper and Below Timing Overlapping !" + "\n" + "clicking on 'Push' to Push timing 'Upper' and 'Below'  Which are Overlapping Start Time and End Time.")
                        .setCancelable(false)
                        .setPositiveButton("Push", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                cursor= SQLITEDATABASE.rawQuery("SELECT * FROM " + SQLITEHELPER.TABLE_NAME + " WHERE  " + SQLITEHELPER.KEY_DOWeek + " = '" + getdataposition + "' AND "+ SQLITEHELPER.KEY_STime + " < '" + Stimeid + "' ORDER BY " + SQLITEHELPER.KEY_STime + " ASC ", null);
                                while (cursor != null && cursor.moveToNext()) {
                                    storeupdatestart=updatestart-intstart;
                                    String Stime= cursor.getString(cursor.getColumnIndex(SQLiteHelper.KEY_STime));
                                    String Etime = cursor.getString(cursor.getColumnIndex(SQLiteHelper.KEY_ETime));
                                    String strId = cursor.getString(cursor.getColumnIndex(SQLiteHelper.KEY_ID));
                                    String[] SplitStime = Stime.split(" ");
                                    String[] SplitEtime = Etime.split(" ");
                                    String StineSplitStime = SplitStime[0];
                                    String EtineSplitEtime = SplitEtime[0];
                                    Date dateStime = new Date();
                                    dateStime.setTime((((Integer.parseInt(StineSplitStime.split(":")[0])) * 60 + (Integer.parseInt(StineSplitStime.split(":")[1]))) + dateStime.getTimezoneOffset()) * 60000);
                                    dateStime.setTime(dateStime.getTime()-(storeupdatestart*60000));
                                    String ust=String.format("%02d:%02d %s", dateStime.getHours() == 0 ? 12 : dateStime.getHours(), dateStime.getMinutes(), dateStime.getHours() < 12 ? "AM" : "PM");

                                    Date dateEtime = new Date();
                                    dateEtime.setTime((((Integer.parseInt(EtineSplitEtime.split(":")[0])) * 60 + (Integer.parseInt(EtineSplitEtime.split(":")[1]))) + dateEtime.getTimezoneOffset()) * 60000);
                                    dateEtime.setTime(dateEtime.getTime()-(storeupdatestart*60000));
                                    String uet=String.format("%02d:%02d %s", dateEtime.getHours() == 0 ? 12 : dateEtime.getHours(), dateEtime.getMinutes(), dateEtime.getHours() < 12 ? "AM" : "PM");

                                    updateScheduleTime(ust, uet, strId);
                                }
                                cursor= SQLITEDATABASE.rawQuery("SELECT * FROM " + SQLITEHELPER.TABLE_NAME + " WHERE  " + SQLITEHELPER.KEY_DOWeek + " = '" + getdataposition + "' AND "+ SQLITEHELPER.KEY_STime + " >= '" + Etimeid + "' ORDER BY " + SQLITEHELPER.KEY_STime + " ASC ", null);
                                while (cursor != null && cursor.moveToNext()) {
                                    storeupdateend=intend-updateend;
                                    String Stime= cursor.getString(cursor.getColumnIndex(SQLiteHelper.KEY_STime));
                                    String Etime = cursor.getString(cursor.getColumnIndex(SQLiteHelper.KEY_ETime));
                                    String strId = cursor.getString(cursor.getColumnIndex(SQLiteHelper.KEY_ID));
                                    String[] SplitStime = Stime.split(" ");
                                    String[] SplitEtime = Etime.split(" ");
                                    String StineSplitStime = SplitStime[0];
                                    String EtineSplitEtime = SplitEtime[0];
                                    Date dateStime = new Date();
                                    dateStime.setTime((((Integer.parseInt(StineSplitStime.split(":")[0])) * 60 + (Integer.parseInt(StineSplitStime.split(":")[1]))) + dateStime.getTimezoneOffset()) * 60000);
                                    dateStime.setTime(dateStime.getTime()+(storeupdateend*60000));
                                    String ust=String.format("%02d:%02d %s", dateStime.getHours() == 0 ? 12 : dateStime.getHours(), dateStime.getMinutes(), dateStime.getHours() < 12 ? "AM" : "PM");

                                    Date dateEtime = new Date();
                                    dateEtime.setTime((((Integer.parseInt(EtineSplitEtime.split(":")[0])) * 60 + (Integer.parseInt(EtineSplitEtime.split(":")[1]))) + dateEtime.getTimezoneOffset()) * 60000);
                                    dateEtime.setTime(dateEtime.getTime()+(storeupdateend*60000));
                                    String uet=String.format("%02d:%02d %s", dateEtime.getHours() == 0 ? 12 : dateEtime.getHours(), dateEtime.getMinutes(), dateEtime.getHours() < 12 ? "AM" : "PM");

                                    updateScheduleTime(ust, uet, strId);
                                }
                                if(cursor.getCount()!=0) {
                                    updateScheduleTimeId(StrStartTime, StrEndTime,StrSubject,StrVenue,StrAlembefor, getStoreId);
                                }

                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                alert = builder.create();
                alert.show();
            }
            else {
                Toast.makeText(getContext(),"Oops! Something went wrong",Toast.LENGTH_LONG).show();
            }
        ((MainActivity)getActivity()).startService();
   }

    private void updateScheduleTimeId(String StrStartTime, String StrEndTime, String StrSubject, String  StrVenue, String StrAlembefor,String getStoreId) {
        SQLITEDATABASE = getActivity().openOrCreateDatabase(SQLITEHELPER.DATABASE_NAME, MODE_PRIVATE, null);
        SQLITEDATABASE.execSQL(" UPDATE " + SQLITEHELPER.TABLE_NAME + " SET " + SQLITEHELPER.KEY_STime + " = '" + StrStartTime + "' ," + SQLITEHELPER.KEY_ETime + "= '" + StrEndTime + "' ," + SQLITEHELPER.KEY_Subject + "= '" + StrSubject + "' ," + SQLITEHELPER.KEY_Venue + "= '" + StrVenue + "' ," + SQLITEHELPER.KEY_AlermBefor + "= '" + StrAlembefor + "' WHERE " + SQLITEHELPER.KEY_ID + " = '" + getStoreId + "'");
        changeoccur();
    }

    private void updateScheduleTime(String ust, String uet, String strId ) {
        SQLITEDATABASE = getActivity().openOrCreateDatabase(SQLITEHELPER.DATABASE_NAME, MODE_PRIVATE, null);
        SQLITEDATABASE.execSQL(" UPDATE " + SQLITEHELPER.TABLE_NAME + " SET " + SQLITEHELPER.KEY_STime + " = '" + ust + "' ," + SQLITEHELPER.KEY_ETime + "= '" + uet + "'  WHERE " + SQLITEHELPER.KEY_DOWeek + " = '" + getdataposition + "' AND " + SQLITEHELPER.KEY_ID + " = '" + strId + "'");
        changeoccur();
    }

}
