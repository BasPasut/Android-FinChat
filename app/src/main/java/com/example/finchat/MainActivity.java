package com.example.finchat;

import android.content.Intent;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    private Toolbar mToolbar;

    private ViewPager mViewPager;
    private SectionPagerAdapter mPagerAdapter;

    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        mToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("FinChat");

        if(mAuth.getCurrentUser() != null) {
            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        }

        //Tabs
        mViewPager = findViewById(R.id.main_Pager);
        mPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mPagerAdapter);

        mTabLayout = findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.getTabAt(0).setIcon(R.drawable.friends_icon);
        mTabLayout.getTabAt(1).setIcon(R.drawable.chat_icon);
        mTabLayout.getTabAt(2).setIcon(R.drawable.request_icon);

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null){
            sendToStart();
        }
        else{
            mUserDatabase.child("online").setValue("true");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null) {

            mUserDatabase.child("online").setValue("true");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null) {

            mUserDatabase.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    private void sendToStart() {
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch(item.getItemId()){
            case R.id.menu_logout_btn:
                FirebaseAuth.getInstance().signOut();
                sendToStart();
                break;
            case R.id.menu_setting_btn:
                Intent settings_intent = new Intent(MainActivity.this, ProfilesActivity.class);
                startActivity(settings_intent);
                break;
            case R.id.menu_all_btn:
                Intent friends_intent = new Intent(MainActivity.this, UsersActivity.class);
                startActivity(friends_intent);
                break;
        }

        return true;
    }
}
