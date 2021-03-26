package ca.cmpt276.walkinggroup.app;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import ca.cmpt276.walkinggroup.dataobjects.User;

public class LeaderboardAdapter extends ArrayAdapter<User> {

    private Context mContext;
    private int mResource;

    /**
     * Holds variables in a View
     */
    private static class ViewHolder {
        TextView rankTextView;
        TextView nameTextView;
        TextView pointsTextView;
    }

    public LeaderboardAdapter(@NonNull Context context, int resource, List<User> users) {
        super(context, resource, users);
        mContext = context;
        mResource = resource;
        Log.i("SSS", users.size() + " ");
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //get the persons information
        String name = getSeparateNames(getItem(position).getName());
        int points = getItem(position).getTotalPointsEarned();
        int rank = position + 1;

//        LayoutInflater inflater = LayoutInflater.from(mContext);
//        convertView = inflater.inflate(mResource, parent, false);
//
//        TextView rankTextView = (TextView) convertView.findViewById(R.id.leaderboardRankTextView);
//        TextView nameTextView = (TextView) convertView.findViewById(R.id.leaderboardNameTextView);
//        TextView pointsTextView = (TextView) convertView.findViewById(R.id.leaderboardPointsTextView);


        //ViewHolder object
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);
            holder = new ViewHolder();

            holder.rankTextView = convertView.findViewById(R.id.leaderboardRankTextView);
            holder.nameTextView = convertView.findViewById(R.id.leaderboardNameTextView);
            holder.pointsTextView = convertView.findViewById(R.id.leaderboardPointsTextView);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.rankTextView.setText(rank + "");
        holder.nameTextView.setText(name);
        holder.pointsTextView.setText(points + "");

        return convertView;
}

    private String getSeparateNames(String fullName) {
        String[] splitName = fullName.split(" ");
        String firstName = " ";
        String lastName = " ";
        int length = splitName.length;

        if (length > 0) {
            if (length == 1) {
                firstName = splitName[0];
            } else {
                firstName = splitName[0];
                lastName = splitName[length - 1];
            }
        }

        return firstName + " " + lastName.substring(0, 1).toUpperCase();
    }
}
