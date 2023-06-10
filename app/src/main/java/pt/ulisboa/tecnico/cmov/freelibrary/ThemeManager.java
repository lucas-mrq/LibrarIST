package pt.ulisboa.tecnico.cmov.freelibrary;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.Button;

public class ThemeManager {
    private static boolean isDarkTheme = false;

    public static boolean isDarkThemeEnabled() {
        return isDarkTheme;
    }

    public static void setDarkThemeEnabled(boolean darkTheme) {
        isDarkTheme = darkTheme;
    }

    public static void setThemeButton(Button themeButton) {
        if (ThemeManager.isDarkThemeEnabled()) {
            themeButton.setText("☼");
        } else {
            themeButton.setText("☽");
        }

        themeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean newTheme = !ThemeManager.isDarkThemeEnabled();
                ThemeManager.setDarkThemeEnabled(newTheme);

                restartActivity(themeButton.getContext());
            }
        });
    }

    private static void restartActivity(Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            activity.finish();
            activity.startActivity(activity.getIntent());
        }
    }
}
