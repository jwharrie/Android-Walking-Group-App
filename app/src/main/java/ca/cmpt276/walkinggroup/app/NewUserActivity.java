package ca.cmpt276.walkinggroup.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import ca.cmpt276.walkinggroup.dataobjects.EarnedRewards;
import ca.cmpt276.walkinggroup.dataobjects.User;

public class NewUserActivity extends AppCompatActivity {

    private final String TAG = "NewUserActivity";

    private static final String MESSAGE_KEY_NAME
            = "ca.cmpt276.walkinggroup.app.NewUserActivity - NAME";
    private static final String MESSAGE_KEY_EMAIL
            = "ca.cmpt276.walkinggroup.app.NewUserActivity - EMAIL";
    private static final String MESSAGE_KEY_PASSWORD
            = "ca.cmpt276.walkinggroup.app.NewUserActivity - PASSWORD";
    private static final String MESSAGE_KEY_BIRTHYEAR
            = "ca.cmpt276.walkinggroup.app.NewUserActivity - BIRTHYEAR";
    private static final String MESSAGE_KEY_BIRTHMONTH
            = "ca.cmpt276.walkinggroup.app.NewUserActivity - BIRTHMONTH";
    private static final String MESSAGE_KEY_ADDRESS
            = "ca.cmpt276.walkinggroup.app.NewUserActivity - ADDRESS";
    private static final String MESSAGE_KEY_CELLPHONE
            = "ca.cmpt276.walkinggroup.app.NewUserActivity - CELLPHONE";
    private static final String MESSAGE_KEY_HOMEPHONE
            = "ca.cmpt276.walkinggroup.app.NewUserActivity - HOMEPHONE";
    private static final String MESSAGE_KEY_GRADE
            = "ca.cmpt276.walkinggroup.app.NewUserActivity - GRADE";
    private static final String MESSAGE_KEY_TEACHERNAME
            = "ca.cmpt276.walkinggroup.app.NewUserActivity - TEACHERNAME";
    private static final String MESSAGE_KEY_EMERGENCYCONTACTINFO
            = "ca.cmpt276.walkinggroup.app.NewUserActivity - EMERGENCYCONTACTINFO";

    private String name = "Anthony";
    private String email = "abrach@sfu.ca";
    private String password = "123456";
    private Integer birthYear;
    private Integer birthMonth;
    private String address = " ";
    private String cellPhone = " ";
    private String homePhone = " ";
    private String grade = " ";
    private String teacherName = " ";
    private String emergencyContactInfo = " ";

    private static final int DEFAULT_VALUE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        setupOkButton();
    }

    private void setupOkButton() {
        final Button btnOk = findViewById(R.id.btn_ok);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText name = findViewById(R.id.etxt_name);
                String nameString = name.getText().toString();
                if(nameString.length() > 0) {
                    String firstLetter = nameString.substring(0, 1).toUpperCase();
                    nameString = firstLetter + nameString.substring(1);
                    setName(nameString);
                }

                EditText email = findViewById(R.id.etxt_email);
                setEmail(email.getText().toString());

                EditText password = findViewById(R.id.etxt_password);
                setPassword(password.getText().toString());

                EditText birthYear = findViewById(R.id.etxt_birthYear);
                try{
                    setBirthYear(Integer.parseInt(birthYear.getText().toString()));
                }catch (NumberFormatException e){
                    Log.i(TAG, "birth year not entered");
                }

                EditText birthMonth = findViewById(R.id.etxt_birthMonth);
                try{
                    setBirthMonth(Integer.parseInt(birthMonth.getText().toString()));
                }catch(NumberFormatException e){
                    Log.i(TAG, "birth month not entered");
                }

                EditText address = findViewById(R.id.etxt_address);
                setAddress(address.getText().toString());

                EditText cellPhone = findViewById(R.id.etxt_cellPhone);
                setCellPhone(cellPhone.getText().toString());

                EditText homePhone = findViewById(R.id.etxt_homePhone);
                setHomePhone(homePhone.getText().toString());

                EditText grade = findViewById(R.id.etxt_grade);
                setGrade(grade.getText().toString());

                EditText teacherName = findViewById(R.id.etxt_teacher);
                setTeacherName(teacherName.getText().toString());

                EditText emergencyContactInfo = findViewById(R.id.etxt_emergencyContact);
                setEmergencyContactInfo(emergencyContactInfo.getText().toString());

                if( (name.length() > 0) && (email.length() > 0) &&
                    (password.length() > 0)){

                    Intent intent = new Intent();
                    intent.putExtra(MESSAGE_KEY_NAME, getName());
                    intent.putExtra(MESSAGE_KEY_EMAIL, getEmail());
                    intent.putExtra(MESSAGE_KEY_PASSWORD, getPassword());
                    intent.putExtra(MESSAGE_KEY_BIRTHYEAR, getBirthYear());
                    intent.putExtra(MESSAGE_KEY_BIRTHMONTH, getBirthMonth());
                    intent.putExtra(MESSAGE_KEY_ADDRESS, getAddress());
                    intent.putExtra(MESSAGE_KEY_CELLPHONE, getCellPhone());
                    intent.putExtra(MESSAGE_KEY_HOMEPHONE, getHomePhone());
                    intent.putExtra(MESSAGE_KEY_GRADE, getGrade());
                    intent.putExtra(MESSAGE_KEY_TEACHERNAME, getTeacherName());
                    intent.putExtra(MESSAGE_KEY_EMERGENCYCONTACTINFO, getEmergencyContactInfo());

                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }else{
                    displayMsg();
                }
            }
        });
    }

    public static Intent makeIntent(Context context, User user) {
        Intent intent = new Intent(context, NewUserActivity.class);
        intent.putExtra(MESSAGE_KEY_NAME,user.getName());
        intent.putExtra(MESSAGE_KEY_EMAIL,user.getEmail());
        intent.putExtra(MESSAGE_KEY_PASSWORD,user.getPassword());
        intent.putExtra(MESSAGE_KEY_BIRTHYEAR, user.getBirthYear());
        intent.putExtra(MESSAGE_KEY_BIRTHMONTH, user.getBirthMonth());
        intent.putExtra(MESSAGE_KEY_ADDRESS, user.getAddress());
        intent.putExtra(MESSAGE_KEY_CELLPHONE, user.getCellPhone());
        intent.putExtra(MESSAGE_KEY_HOMEPHONE, user.getHomePhone());
        intent.putExtra(MESSAGE_KEY_GRADE, user.getGrade());
        intent.putExtra(MESSAGE_KEY_TEACHERNAME, user.getTeacherName());
        intent.putExtra(MESSAGE_KEY_EMERGENCYCONTACTINFO, user.getEmergencyContactInfo());
        return intent;
    }

    public static User getUserFromIntent(Intent data){
        String userName;
        String userEmail;
        String userPassword;
        Integer userBirthYear;
        Integer userBirthMonth;
        String userAddress;
        String userCellPhone;
        String userHomePhone;
        String userGrade;
        String userTeacherName;
        String userEmergencyContactInfo;

        userName = data.getStringExtra(MESSAGE_KEY_NAME);
        userEmail = data.getStringExtra(MESSAGE_KEY_EMAIL);
        userPassword = data.getStringExtra(MESSAGE_KEY_PASSWORD);
        userBirthYear = data.getIntExtra(MESSAGE_KEY_BIRTHYEAR, DEFAULT_VALUE);
        userBirthMonth = data.getIntExtra(MESSAGE_KEY_BIRTHMONTH,DEFAULT_VALUE);
        userAddress = data.getStringExtra(MESSAGE_KEY_ADDRESS);
        userCellPhone = data.getStringExtra(MESSAGE_KEY_CELLPHONE);
        userHomePhone = data.getStringExtra(MESSAGE_KEY_HOMEPHONE);
        userGrade = data.getStringExtra(MESSAGE_KEY_GRADE);
        userTeacherName = data.getStringExtra(MESSAGE_KEY_TEACHERNAME);
        userEmergencyContactInfo = data.getStringExtra(MESSAGE_KEY_EMERGENCYCONTACTINFO);

        User theUser = new User();

        theUser.setEmail(userEmail);
        theUser.setName(userName);
        theUser.setPassword(userPassword);
        theUser.setBirthYear(userBirthYear);
        theUser.setBirthMonth(userBirthMonth);
        theUser.setAddress(userAddress);
        theUser.setCellPhone(userCellPhone);
        theUser.setHomePhone(userHomePhone);
        theUser.setGrade(userGrade);
        theUser.setTeacherName(userTeacherName);
        theUser.setEmergencyContactInfo(userEmergencyContactInfo);

        theUser.setCurrentPoints(0);
        theUser.setTotalPointsEarned(0);
        theUser.setRewards(new EarnedRewards());

        return theUser;
    }

    private String getName() {
        return name;
    }

    private void setName(String name) {
        try{
            if (name != null && name.length() > 0) {
                this.name = name;
            }
        }catch (Exception e){
            displayMsg();
            throw new IllegalArgumentException("Empty Name");
        }
    }

    private String getEmail() {
        return email;
    }

    private void setEmail(String email){
        try{
            if (email != null && email.length() > 0) {
                this.email = email;
            }
        }catch (Exception e){
            displayMsg();
            throw new IllegalArgumentException("Empty Email");
        }
    }

    private String getPassword() {
        return password;
    }

    private void setPassword(String password) {
        try{
            if (password != null && password.length() > 0) {
                this.password = password;
            }
        }catch (Exception e){
            displayMsg();
            throw new IllegalArgumentException("Empty Password");
        }
    }

    private Integer getBirthYear() {
        return birthYear;
    }

    private void setBirthYear(Integer birthYear) {
        try{
            if (birthYear != null && !birthYear.toString().equals("")) {
                this.birthYear = birthYear;
            }
        }catch (Exception e){
//            displayMsg();
            throw new IllegalArgumentException("Empty BirthYear");
        }

        this.birthYear = birthYear;
    }

    private Integer getBirthMonth() {
        return birthMonth;
    }

    private void setBirthMonth(Integer birthMonth) {
        try{
            if (birthMonth != null && !birthMonth.toString().equals("")) {
                this.birthMonth = birthMonth;
            }
        }catch (Exception e){
//            displayMsg();
            throw new IllegalArgumentException("Empty Birth Month");
        }

        this.birthMonth = birthMonth;
    }

    private String getAddress() {
        return address;
    }

    private void setAddress(String address) {
        try{
            if (address != null && address.length() > 0) {
                this.address = address;
            }
        }catch (Exception e){
//            displayMsg();
            throw new IllegalArgumentException("Empty Address");
        }
    }

    private String getCellPhone() {
        return cellPhone;
    }

    private void setCellPhone(String cellPhone) {
        try{
            if (cellPhone != null && cellPhone.length() > 0) {
                this.cellPhone = cellPhone;
            }
        }catch (Exception e){
//            displayMsg();
            //throw new IllegalArgumentException("Empty Cellphone");
        }
    }

    private String getHomePhone() {
        return homePhone;
    }

    private void setHomePhone(String homePhone) {
        try{
            if (homePhone != null && homePhone.length() > 0) {
                this.homePhone = homePhone;
            }
        }catch (Exception e){
//            displayMsg();
            //throw new IllegalArgumentException("Empty Homephone");
        }
    }

    private String getGrade() {
        return grade;
    }

    private void setGrade(String grade) {
        try{
            if (grade != null && grade.length() > 0) {
                this.grade = grade;
            }
        }catch (Exception e){
//            displayMsg();
            //throw new IllegalArgumentException("Empty Grade");
        }
    }

    private String getTeacherName() {
        return teacherName;
    }

    private void setTeacherName(String teacherName) {
        try{
            if (teacherName != null && teacherName.length() > 0) {
                this.teacherName = teacherName;
            }
        }catch (Exception e){
//            displayMsg();
            //throw new IllegalArgumentException("Empty Teacher's Name");
        }
    }

    private String getEmergencyContactInfo() {
        return emergencyContactInfo;
    }

    private void setEmergencyContactInfo(String emergencyContactInfo) {
        try{
            if (emergencyContactInfo != null && emergencyContactInfo.length() > 0) {
                this.emergencyContactInfo = emergencyContactInfo;
            }
        }catch (Exception e){
//            displayMsg();
            //throw new IllegalArgumentException("Empty Emergency Contact Info");
        }
    }

    private void displayMsg(){
        Toast.makeText(this, "Please Fill In Required Fields", Toast.LENGTH_SHORT).show();
    }
}
