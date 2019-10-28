package com.example.fitnesssapp;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.Collection;

public class AxisValueFormatter extends ValueFormatter {

    public AxisValueFormatter() {
        super();
    }

//    @Override
//    public String getFormattedValue(float value, AxisBase base) {
//        base.setLabelCount(2,true);
//        return value + "$";
//    }
//
//    @Override
//    public String getBarLabel(BarEntry barEntry) {
//        return barEntry + "Day";
//    }

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        return " DAY";
    }
}