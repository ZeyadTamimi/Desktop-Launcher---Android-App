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
    //----------------------------------------------------------------------------------------------
    public enum ConnectionState {
        CONNECTED,
        CONNECTING,
        DISCONNECTED;
    }

    // my new class variables, copies of constructor params, but add more if required
    private Context context ;
    private ArrayList<String> theStringArray;
    public final int numRows = 500 ;
    private ConnectionState[] RowConnection = new ConnectionState[numRows];

    //----------------------------------------------------------------------------------------------
    public BluetoothArrayAdaptor(Context _context,
                                 int textViewResourceId,
                                 ArrayList<String> _theStringArray)
    {
        // call base class constructor
        super(_context, textViewResourceId, _theStringArray);
        // save the context and the array of strings we were given
        context = _context;
        theStringArray = _theStringArray;
        clearConnection ();
    }

    //----------------------------------------------------------------------------------------------
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

        switch (getConnection(position)) {
            case DISCONNECTED:
                row.findViewById(R.id.Connecting).setVisibility(View.GONE);
                icon.setVisibility(View.VISIBLE);
                icon.setImageResource(R.drawable.checkbox_blank_outline);
                break;
            case CONNECTED:
                row.findViewById(R.id.Connecting).setVisibility(View.GONE);
                icon.setVisibility(View.VISIBLE);
                icon.setImageResource(R.drawable.checkbox_marked);
                break;
            case CONNECTING:
                icon.setVisibility(View.GONE);
                row.findViewById(R.id.Connecting).setVisibility(View.VISIBLE);
                break;
        }

        icon.setVisibility (View.VISIBLE);

        return row;
    }

    //----------------------------------------------------------------------------------------------
    public void setState(int position, ConnectionState state) {RowConnection [position] = state; }
    public ConnectionState getConnection(int position) {return RowConnection[position];}
    public void clearConnection () {
        for(int i = 0; i < numRows; i ++)
            setState(i, ConnectionState.DISCONNECTED);
    }
}