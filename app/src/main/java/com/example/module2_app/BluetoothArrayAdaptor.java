package com.example.module2_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


public class BluetoothArrayAdaptor extends ArrayAdapter<String> {
    // my new class variables, copies of constructor params, but add more if required
    private Context context ;
    private ArrayList<String> theStringArray;
    // constructor
    public BluetoothArrayAdaptor ( Context _context,
                                  int textViewResourceId,
                                  ArrayList<String> _theStringArray
    )
    {
        // call base class constructor
        super(_context, textViewResourceId, _theStringArray);
        // save the context and the array of strings we were given
        context = _context;
        theStringArray = _theStringArray;
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE );
        View row = inflater.inflate ( R.layout.row, parent, false );
        ImageView icon = (ImageView) row.findViewById (R.id.BTicon);
        icon.setImageResource (R.drawable.bluetooth);
        icon.setVisibility (View.VISIBLE);
        TextView label = (TextView) row.findViewById( R.id.BTdeviceText);
        label.setText (theStringArray.get(position));
        icon = (ImageView) row.findViewById (R.id.Selected);
        icon.setImageResource (R.drawable.lightbulb_on);
        icon.setVisibility (View.VISIBLE);

        return row;
    }
}