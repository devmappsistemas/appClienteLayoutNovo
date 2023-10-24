package com.vamosja;
import android.location.Location;

/**
 * Created by DEV on 16/01/2018.
 */

public interface LocationUpdateListener {

    void updateLocation(Location location);

    void lastLocation(Location location);

    void errorLocation(int statusCode);
}
