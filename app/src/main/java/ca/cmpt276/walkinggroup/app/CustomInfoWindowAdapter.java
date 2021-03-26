package ca.cmpt276.walkinggroup.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View mWindow;
//    private Context mContext;

    public CustomInfoWindowAdapter(Context context) {
//        this.mContext = context;
        mWindow = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null);
    }

    private void setInfoWindowText(Marker marker, View view){

        String title = marker.getTitle();
        TextView windowTitle = view.findViewById(R.id.infoWindowTitle);
        if(!title.equals("")){
            windowTitle.setText(title);
        }

        String snippet = marker.getSnippet();
        TextView windowSnippet = view.findViewById(R.id.infoWindowSnippet);
        if(!snippet.equals("")){
            windowSnippet.setText(snippet);
        }
        else{
            windowSnippet.setText(" ");
        }
    }


    @Override
    public View getInfoWindow(Marker marker) {
        setInfoWindowText(marker, mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        setInfoWindowText(marker, mWindow);
        return mWindow;
    }
}
