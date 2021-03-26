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

import java.util.List;

import ca.cmpt276.walkinggroup.dataobjects.SharedData;
import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

public class EditChosenChildInfo extends AppCompatActivity {

    private static final String MESSAGE_KEY_CHILD_INDEX = "CHILD CHOSEN TO EDIT";
    private SharedData sharedData = SharedData.getSharedData();
    private WGServerProxy proxy;
    private Long userId;
    private User chosenChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_chosen_child_info);

        userId = sharedData.getUser().getId();
        proxy = sharedData.getProxy();

        setupComponents();
    }



    private void setupList() {
        // Get initial list, call server
        Call<List<User>> caller = proxy.getMonitorsUsers(userId);
        ProxyBuilder.callProxy(EditChosenChildInfo.this, caller,
                returnedUsers -> updateList(returnedUsers));
    }

    private void updateList(List<User> users) {
        Intent intent = getIntent();
        int index = intent.getIntExtra(MESSAGE_KEY_CHILD_INDEX, 100);
        chosenChild = users.get(index);
        setupHints();
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
                    chosenChild.setName(editedName);
                }

                EditText email = findViewById(R.id.etxt_email);
                String editedEmail = email.getText().toString();
                if(editedEmail.matches("")){
                    //Do nothing
                }else{
                    chosenChild.setEmail(editedEmail);
                }

                EditText birthYear = findViewById(R.id.etxt_birthYear);
                String editedBirthYear = birthYear.getText().toString();
                if(editedBirthYear.matches("")){
                    //Do nothing
                }else{
                    chosenChild.setBirthYear(Integer.parseInt(birthYear.getText().toString()));
                }

                EditText birthMonth = findViewById(R.id.etxt_birthMonth);
                String editedBirthMonth = birthMonth.getText().toString();
                if(editedBirthMonth.matches("")){
                    //Do nothing
                }else{
                    chosenChild.setBirthMonth(Integer.parseInt(birthMonth.getText().toString()));

                }

                EditText address = findViewById(R.id.etxt_address);
                String editedAddress = address.getText().toString();
                if(editedAddress.matches("")){
                    //Do nothing
                }else{
                    chosenChild.setAddress(address.getText().toString());

                }

                EditText cellPhone = findViewById(R.id.etxt_cellPhone);
                String editedCellPhone = cellPhone.getText().toString();
                if(editedCellPhone.matches("")){
                    //Do nothing
                }else{
                    chosenChild.setCellPhone(cellPhone.getText().toString());

                }

                EditText homePhone = findViewById(R.id.etxt_homePhone);
                String editedHomePhone = homePhone.getText().toString();
                if(editedHomePhone.matches("")){
                    //Do nothing
                }else{
                    chosenChild.setHomePhone(homePhone.getText().toString());

                }

                EditText grade = findViewById(R.id.etxt_grade);
                String editedGrade = grade.getText().toString();
                if(editedGrade.matches("")){
                    //Do nothing
                }else{
                    chosenChild.setGrade(grade.getText().toString());

                }

                EditText teacherName = findViewById(R.id.etxt_teacher);
                String editedTeacherName = teacherName.getText().toString();
                if(editedTeacherName.matches("")){
                    //Do nothing
                }else{
                    chosenChild.setTeacherName(teacherName.getText().toString());

                }

                EditText emergencyContactInfo = findViewById(R.id.etxt_emergencyContact);
                String editedEmergencyContactInfo = emergencyContactInfo.getText().toString();
                if(editedEmergencyContactInfo.matches("")){
                    //Do nothing
                }else{
                    chosenChild.setEmergencyContactInfo(emergencyContactInfo.getText().toString());
                }


                //Call Server
                Call<User> caller = proxy.editUser(chosenChild.getId(), chosenChild);
                ProxyBuilder.callProxy(EditChosenChildInfo.this, caller, editedUser -> setUserResponse(editedUser));
                setupList();

                setResult(RESULT_OK);
                finish();

            }
        });
    }

    private void setupHints(){

        EditText name = findViewById(R.id.etxt_name);
        name.setHint(chosenChild.getName());

        EditText email = findViewById(R.id.etxt_email);
        email.setHint(chosenChild.getEmail());

        EditText birthYear = findViewById(R.id.etxt_birthYear);
        birthYear.setHint(String.valueOf(chosenChild.getBirthYear()));

        EditText birthMonth = findViewById(R.id.etxt_birthMonth);
        birthMonth.setHint(String.valueOf(chosenChild.getBirthMonth()));

        EditText address = findViewById(R.id.etxt_address);
        address.setHint(chosenChild.getAddress());

        EditText cellPhone = findViewById(R.id.etxt_cellPhone);
        cellPhone.setHint(chosenChild.getCellPhone());

        EditText homePhone = findViewById(R.id.etxt_homePhone);
        homePhone.setHint(chosenChild.getHomePhone());

        EditText teacherName = findViewById(R.id.etxt_teacher);
        teacherName.setHint(chosenChild.getTeacherName());

        EditText emergencyContactInfo = findViewById(R.id.etxt_emergencyContact);
        emergencyContactInfo.setHint(chosenChild.getEmergencyContactInfo());

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

    public static Intent makeIntent(Context context) {
        return new Intent(context, EditChosenChildInfo.class);
    }

    private void setupComponents(){
        setupList();
        setupEditButton();
    }

}

