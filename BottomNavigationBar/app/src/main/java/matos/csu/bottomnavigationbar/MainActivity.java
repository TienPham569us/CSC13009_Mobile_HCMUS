package matos.csu.bottomnavigationbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;

    AlbumFragment albumFragment = new AlbumFragment();
    ImageFragment imageFragment = new ImageFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set default fragment when open app
        getSupportFragmentManager().beginTransaction().replace(R.id.container, albumFragment).commit();
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                if (item.getItemId() == R.id.album) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, albumFragment).commit();
                    return true;
                }
                else if (item.getItemId() == R.id.image) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, imageFragment).commit();
                    return true;
                }
                return false;
            }
        });
    }
}