package uk.co.darkerwaters.scorepal.ui;

import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.ui.views.CustomSnackbar;

public class PermissionsHandler {

    public static final int PERMISSIONS_REQUEST = 103;

    public interface PermissionsListener {
        void onPermissionsChanged(String[] permissions, int[] grantResults);
    }

    public interface Container {
        PermissionsHandler getPermissionsHandler();
    }

    private final Activity activity;
    private final List<PermissionsListener> listeners;

    public PermissionsHandler(Activity activity) {
        this.activity = activity;
        this.listeners = new ArrayList<>();
    }

    public boolean addListener(PermissionsListener listener) {
        synchronized (this.listeners) {
            return this.listeners.add(listener);
        }
    }

    public boolean removeListener(PermissionsListener listener) {
        synchronized (this.listeners) {
            return this.listeners.remove(listener);
        }
    }

    public void informListeners(String[] permissions, int[] results) {
        synchronized (this.listeners) {
            for (PermissionsListener listener: this.listeners) {
                listener.onPermissionsChanged(permissions, results);
            }
        }
    }

    public boolean isPermissionsGranted(String[] permissions) {
        boolean isPermissionsGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                // this isn't granted, so let's ask for permission here
                isPermissionsGranted = false;
                break;
            }
        }
        return isPermissionsGranted;
    }

    public void checkPermissions(int rationaleString, int iconRes, final String[] permissions, boolean forceShowRationale) {
        if (!isPermissionsGranted(permissions)) {
            // one or more permissions are not granted, so ask here
            if (!forceShowRationale) {
                for (String permission : permissions) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                        // the activity want's us to show why we want this permission
                        forceShowRationale = true;
                        break;
                    }
                }
            }
            if (forceShowRationale) {
                new CustomSnackbar(activity, rationaleString, iconRes, R.string.permissionEnable, new CustomSnackbar.SnackbarListener() {
                    @Override
                    public void onButtonOnePressed() {
                        // pressed 'ENABLE' so request the permission
                        activity.requestPermissions(permissions, PERMISSIONS_REQUEST);
                    }
                    @Override
                    public void onButtonTwoPressed() {
                        // fine, inform this was cancelled
                        informListeners(permissions, null);
                    }
                    @Override
                    public void onDismissed() {
                        // fine, inform this was cancelled
                        informListeners(permissions, null);
                    }
                });
            } else {
                // just request them direct
                activity.requestPermissions(permissions, PERMISSIONS_REQUEST);
            }
        } else {
            // write your logic code if permission already granted
            int[] results = new int[permissions.length];
            for (int i = 0; i < permissions.length; ++i) {
                results[i] = PackageManager.PERMISSION_GRANTED;
            }
            // inform the listeners that everything is ok here
            informListeners(permissions, results);
        }
    }

    public void processPermissionsResult(final String[] permissions, int[] grantResults) {
        // permissions were granted or not
        informListeners(permissions, grantResults);
    }
}
