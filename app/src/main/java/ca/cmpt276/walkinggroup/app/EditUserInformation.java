package ca.cmpt276.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import ca.cmpt276.walkinggroup.dataobjects.SharedData;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

public class EditUserInformation extends AppCompatActivity {

    private SharedData sharedData;
    private WGServerProxy proxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_information);

        sharedData = SharedData.getSharedData();
        proxy = sharedData.getProxy();

        setupComponents();

    }


    private void setupEditButton() {
        final Button btnOk = findViewById(R.id.btn_edit);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                EditText name = findViewById(R.id.etxt_name);
                String editedName = name.getText().toString();

                if(editedName.matches("")){
                    //Do nothing
                }else{
                    String firstLetter = editedName.substring(0,1).toUpperCase();
                    editedName = firstLetter + editedName.substring(1);
                    sharedData.getUser().setName(editedName);
                }

                EditText email = findViewById(R.id.etxt_email);
                String editedEmail = email.getText().toString();
                if(editedEmail.matches("")){
                    //Do nothing
                }else{
                    sharedData.getUser().setEmail(editedEmail);
                }


                EditText birthYear = findViewById(R.id.etxt_birthYear);
                String editedBirthYear = birthYear.getText().toString();
                if(editedBirthYear.matches("")){
                    //Do nothing
                }else{
                    sharedData.getUser().setBirthYear(Integer.parseInt(birthYear.getText().toString()));
                }



                EditText birthMonth = findViewById(R.id.etxt_birthMonth);
                String editedBirthMonth = birthMonth.getText().toString();
                if(editedBirthMonth.matches("")){
                    //Do nothing
                }else{
                    sharedData.getUser().setBirthMonth(Integer.parseInt(birthMonth.getText().toString()));

                }

                EditText address = findViewById(R.id.etxt_address);
                String editedAddress = address.getText().toString();
                if(editedAddress.matches("")){
                    //Do nothing
                }else{
                    sharedData.getUser().setAddress(address.getText().toString());

                }

                EditText cellPhone = findViewById(R.id.etxt_cellPhone);
                String editedCellPhone = cellPhone.getText().toString();
                if(editedCellPhone.matches("")){
                    //Do nothing
                }else{
                    sharedData.getUser().setCellPhone(cellPhone.getText().toString());

                }

                EditText homePhone = findViewById(R.id.etxt_homePhone);
                String editedHomePhone = homePhone.getText().toString();
                if(editedHomePhone.matches("")){
                    //Do nothing
                }else{
                    sharedData.getUser().setHomePhone(homePhone.getText().toString());

                }

                EditText grade = findViewById(R.id.etxt_grade);
                String editedGrade = grade.getText().toString();
                if(editedGrade.matches("")){
                    //Do nothing
                }else{
                    sharedData.getUser().setGrade(grade.getText().toString());

                }

                EditText teacherName = findViewById(R.id.etxt_teacher);
                String editedTeacherName = teacherName.getText().toString();
                if(editedTeacherName.matches("")){
                    //Do nothing
                }else{
                    sharedData.getUser().setTeacherName(teacherName.getText().toString());

                }

                EditText emergencyContactInfo = findViewById(R.id.etxt_emergencyContact);
                String editedEmergencyContactInfo = emergencyContactInfo.getText().toString();
                if(editedEmergencyContactInfo.matches("")){
                    //Do nothing
                }else{
                    sharedData.getUser().setEmergencyContactInfo(emergencyContactInfo.getText().toString());
                }


                //Call Server
                Call<User> caller = proxy.editUser(sharedData.getUser().getId(), sharedData.getUser());
                ProxyBuilder.callProxy(EditUserInformation.this, caller, editedUser -> setUserResponse(editedUser));

                finish();

            }
        });
    }

    private void setupHints(){
        EditText name = findViewById(R.id.etxt_name);
        name.setHint(sharedData.getUser().getName());

        EditText email = findViewById(R.id.etxt_email);
        email.setHint(sharedData.getUser().getEmail());

        EditText birthYear = findViewById(R.id.etxt_birthYear);
        birthYear.setHint(String.valueOf(sharedData.getUser().getBirthYear()));

        EditText birthMonth = findViewById(R.id.etxt_birthMonth);
        birthMonth.setHint(String.valueOf(sharedData.getUser().getBirthMonth()));

        EditText address = findViewById(R.id.etxt_address);
        address.setHint(sharedData.getUser().getAddress());

        EditText cellPhone = findViewById(R.id.etxt_cellPhone);
        cellPhone.setHint(sharedData.getUser().getCellPhone());

        EditText homePhone = findViewById(R.id.etxt_homePhone);
        homePhone.setHint(sharedData.getUser().getHomePhone());

        EditText grade = findViewById(R.id.etxt_grade);
        grade.setHint(sharedData.getUser().getGrade());

        EditText teacherName = findViewById(R.id.etxt_teacher);
        teacherName.setHint(sharedData.getUser().getTeacherName());

        EditText emergencyContactInfo = findViewById(R.id.etxt_emergencyContact);
        emergencyContactInfo.setHint(sharedData.getUser().getEmergencyContactInfo());
    }

    private void setUserResponse(User editedUser) {
        notifyUserViaLogAndToast("Server replied with user: " + editedUser.toString());
//        name = editedUser.getName();
//        email = editedUser.getEmail();
//        address = editedUser.getAddress();
    }

    private void notifyUserViaLogAndToast(String message) {
        Log.w("TEST", message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void setupComponents(){
        setupHints();
        setupEditButton();
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, EditUserInformation.class);
    }
}

