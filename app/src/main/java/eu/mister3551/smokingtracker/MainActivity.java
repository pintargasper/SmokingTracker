package eu.mister3551.smokingtracker;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.lang.ref.WeakReference;
import java.sql.SQLDataException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import eu.mister3551.smokingtracker.database.Manager;
import eu.mister3551.smokingtracker.databinding.ActivityMainBinding;
import eu.mister3551.smokingtracker.record.Settings;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        requestPermissions();

        Utils.setMainActivity(new WeakReference<>(this));
        Utils.setManager(new WeakReference<>(new Manager(this)));
        try {
            Utils.getManager().get().open();
        } catch (SQLDataException sqlDataException) {
            System.out.println(sqlDataException.getMessage());
        }
        Utils.setSettings(setSettings(Utils.manager.get()));

        setLanguage(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_graph, R.id.navigation_settings)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    private Settings setSettings(Manager manager) {

        Cursor cursor = manager.fetchSettings();

        if (cursor != null && cursor.moveToFirst()) {
            String language = cursor.getString(cursor.getColumnIndexOrThrow("language"));
            int color = cursor.getInt(cursor.getColumnIndexOrThrow("color"));
            String pointColors = cursor.getString(cursor.getColumnIndexOrThrow("point_color"));
            String lineColors = cursor.getString(cursor.getColumnIndexOrThrow("line_color"));
            String paintStyle = cursor.getString(cursor.getColumnIndexOrThrow("paint_style"));

            Paint.Style style = switch (paintStyle) {
                case "STROKE" -> Paint.Style.STROKE;
                case "FILL_AND_STROKE" -> Paint.Style.FILL_AND_STROKE;
                default -> Paint.Style.FILL;
            };
            return new Settings(language, color, getColors(pointColors), getColors(lineColors), style);
        }
        return new Settings("EN", Color.GRAY, getColors(String.valueOf(Color.GRAY)), getColors(String.valueOf(Color.GRAY)), Paint.Style.FILL);
    }

    private List<Integer> getColors(String colors) {
        List<Integer> allColors = new ArrayList<>();

        if (colors != null) {
            for (String color : colors.split(";")) {
                allColors.add((int) Long.parseLong(color));
            }
        }
        return allColors;
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_MEDIA_IMAGES, android.Manifest.permission.READ_MEDIA_VIDEO}, 1);
            }
        } else {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    public static void setLanguage(Context context) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(new Locale(Utils.settings.getLanguage()));
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }

    public void recreateActivity() {
        this.recreate();
    }
}