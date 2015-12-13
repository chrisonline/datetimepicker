package com.fourmob.datetimepicker.date;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

import java.util.HashMap;

public class SimpleMonthAdapter extends BaseAdapter implements SimpleMonthView.OnDayClickListener {

    private static final String TAG = "SimpleMonthAdapter";
    protected static int WEEK_7_OVERHANG_HEIGHT = 7;
    protected static final int MONTHS_IN_YEAR = 12;

    private final Context mContext;
    private final DatePickerController mController;

    private CalendarDay mSelectedDay;
    private CalendarDay mMinDate;
    private CalendarDay mMaxDate = null;

    public SimpleMonthAdapter(Context context, DatePickerController datePickerController) {
        mContext = context;
        mController = datePickerController;
        init();
        setSelectedDay(mController.getSelectedDay());
    }

    private boolean isSelectedDayInMonth(int year, int month) {
        return (mSelectedDay.year == year) && (mSelectedDay.month == month);
    }

    public int getCount() {
        return mController.getMonthCount();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        SimpleMonthView v;
        HashMap<String, Integer> drawingParams = null;
        if (convertView != null) {
            v = (SimpleMonthView) convertView;
            drawingParams = (HashMap<String, Integer>) v.getTag();
        } else {
            v = new SimpleMonthView(mContext);
            v.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            v.setClickable(true);
            v.setOnDayClickListener(this);
        }
        if (drawingParams == null) {
            drawingParams = new HashMap<String, Integer>();
        }
        drawingParams.clear();

        final int month = (position + mController.getFirstMonth()) % MONTHS_IN_YEAR;
        final int year = (position + mController.getFirstMonth()) / MONTHS_IN_YEAR + mController.getMinYear();

        int selectedDay = -1;
        if (isSelectedDayInMonth(year, month)) {
            selectedDay = mSelectedDay.day;
        }

        v.reuse();

        drawingParams.put(SimpleMonthView.VIEW_PARAMS_SELECTED_DAY, selectedDay);
        drawingParams.put(SimpleMonthView.VIEW_PARAMS_YEAR, year);
        drawingParams.put(SimpleMonthView.VIEW_PARAMS_MONTH, month);
        drawingParams.put(SimpleMonthView.VIEW_PARAMS_WEEK_START, mController.getFirstDayOfWeek());
        if (mMinDate != null) {
            drawingParams.put(SimpleMonthView.VIEW_PARAMS_MIN_DATE_DAY, mMinDate.day);
            drawingParams.put(SimpleMonthView.VIEW_PARAMS_MIN_DATE_MONTH, mMinDate.month);
            drawingParams.put(SimpleMonthView.VIEW_PARAMS_MIN_DATE_YEAR, mMinDate.year);
        }
        if (mMaxDate != null) {
            drawingParams.put(SimpleMonthView.VIEW_PARAMS_MAX_DATE_DAY, mMaxDate.day);
            drawingParams.put(SimpleMonthView.VIEW_PARAMS_MAX_DATE_MONTH, mMaxDate.month);
            drawingParams.put(SimpleMonthView.VIEW_PARAMS_MAX_DATE_YEAR, mMaxDate.year);
        }
        v.setMonthParams(drawingParams);
        v.invalidate();

        return v;
    }

    protected void init() {
        mSelectedDay = new CalendarDay(System.currentTimeMillis());
    }

    public void onDayClick(SimpleMonthView simpleMonthView, CalendarDay calendarDay) {
        if (calendarDay != null) {
            if ((this.mMinDate == null || calendarDay.isAfter(this.mMinDate) || calendarDay.equals(this.mMinDate)) &&
                    (this.mMaxDate == null || calendarDay.isBefore(this.mMaxDate) || calendarDay.equals(this.mMaxDate))) {
                onDayTapped(calendarDay);
            } else {
                Log.i(TAG, "ignoring push since day is after minDate or before maxDate");
            }
        }
    }

    protected void onDayTapped(CalendarDay calendarDay) {
        mController.tryVibrate();
        mController.onDayOfMonthSelected(calendarDay.year, calendarDay.month, calendarDay.day);
        setSelectedDay(calendarDay);
    }

    public void setSelectedDay(CalendarDay calendarDay) {
        mSelectedDay = calendarDay;
        notifyDataSetChanged();
    }

    /**
     * sets the min date. All previous days are disabled.
     *
     * @param mMinDate
     */
    public void setMinDate(CalendarDay mMinDate) {
        this.mMinDate = mMinDate;
        notifyDataSetChanged();
    }

    public void setMaxDate(CalendarDay mMaxDate) {
        this.mMaxDate = mMaxDate;
        notifyDataSetChanged();
    }

}