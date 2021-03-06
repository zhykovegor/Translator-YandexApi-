package com.translator.translator;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

import entry.TranslatedData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import services.Url;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TextView text;
    private TextView translated;
    private Spinner spinner;

    private String selectedLanguage;
    private Gson gson = new GsonBuilder().create();
    private final String BASE_URL = "https://translate.yandex.net";
    private final String KEY = "trnsl.1.1.20161213T090001Z.b58c535961cc82b7.181d722d1506c2ac62e75282e49c101705e43a2f";

    //array contains allowed languages
    final String[] languages = {"en-ru", "ru-en"};

    //setting retrofit
    private Retrofit retrofit = new Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(BASE_URL)
            .build();

    //setting service
    private Url service = retrofit.create(Url.class);


    //Finding views
    private void findViews() {
        text = (TextView) findViewById(R.id.text);
        translated = (TextView) findViewById(R.id.translated);
    }

    //Toolbar
    private Toolbar getToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        return toolbar;
    }

    //shared preference save method
    private void saveSetting(int index){
        SharedPreferences sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.putInt("lang", index);
        editor.commit();
    }

    //shared preference load method
    private int loadSetting(){
        SharedPreferences sPref = getPreferences(MODE_PRIVATE);
        selectedLanguage = languages[sPref.getInt("lang", 0)];
        return sPref.getInt("lang", 0);
    }

    //FloatingButton
    private void initFloatingButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, String> params = new HashMap<>();
                params.put("key", KEY);
                params.put("text", text.getText().toString());
                params.put("lang", selectedLanguage);

                Call<TranslatedData> call = service.update(params);

                call.enqueue(new Callback<TranslatedData>() {
                    @Override
                    public void onResponse(Call<TranslatedData> call, Response<TranslatedData> response) {
                        //cutting not useful symbols
                        String currentText = response.body().getText().toString();
                        currentText = currentText.substring(1, currentText.length() - 1);

                        //setting test from respond to text field
                        translated.setText(currentText);
                    }

                    @Override
                    public void onFailure(Call<TranslatedData> call, Throwable t) {
                        //fail message
                        Toast.makeText(getBaseContext(), "Error! Check internet connection!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    //NavigationView
    private void initNavigationView() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    //Drawer
    private void initDrawer(Toolbar toolbar) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
    }

    //Spinner
    private void initSpinner() {
        //adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //spinner
        spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setAdapter(adapter);

        //event listener
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedLanguage = languages[i];

                //saving setting to shared preference
                saveSetting(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init toolbar
        Toolbar toolbar = getToolbar();
        setSupportActionBar(toolbar);

        //init floating button
        initFloatingButton();

        //init navigation view
        initNavigationView();

        //init drawer
        initDrawer(toolbar);

        //init spinner and load setting from shared preference
        initSpinner();
        spinner.setSelection(loadSetting());

        //finding views
        findViews();

    }

    @Override
    protected void onResume() {
        super.onResume();
        //load setting from shared preference
        spinner.setSelection(loadSetting());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    //create and show about dialog
    private void showAboutDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("About");
        dialog.setMessage("Build 1.0.0");
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            showAboutDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_about) {
            showAboutDialog();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
