package com.thakar.familymedicine;
import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import com.getcapacitor.*;
import com.getcapacitor.annotation.CapacitorPlugin;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;
@CapacitorPlugin(name = "MedAlarm")
public class AlarmPlugin extends Plugin {
  static void set(Context c, int id, long at, String t, String b, boolean daily, boolean strict) {
    Intent i = new Intent(c, AlarmReceiver.class).putExtra("id", id).putExtra("at", at).putExtra("title", t)
      .putExtra("body", b).putExtra("daily", daily).putExtra("strict", strict);
    PendingIntent pi = PendingIntent.getBroadcast(c, id, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    PendingIntent show = PendingIntent.getActivity(c, 0, new Intent(c, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);
    AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
    try { am.setAlarmClock(new AlarmManager.AlarmClockInfo(at, show), pi); } catch (SecurityException e) {}
  }
  @PluginMethod public void schedule(PluginCall call) {
    Context c = getContext();
    SharedPreferences sp = c.getSharedPreferences("med", 0);
    AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
    for (String s : sp.getStringSet("ids", new HashSet<String>())) {
      PendingIntent pi = PendingIntent.getBroadcast(c, Integer.parseInt(s), new Intent(c, AlarmReceiver.class), PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
      if (pi != null) am.cancel(pi);
    }
    Set<String> ids = new HashSet<String>();
    try {
      JSONArray arr = call.getArray("alarms");
      for (int k = 0; k < arr.length(); k++) {
        JSONObject o = arr.getJSONObject(k);
        set(c, o.getInt("id"), o.getLong("at"), o.getString("title"), o.getString("body"), o.optBoolean("daily"), o.optBoolean("strict", true));
        ids.add("" + o.getInt("id"));
      }
    } catch (Exception e) {}
    sp.edit().putStringSet("ids", ids).apply();
    call.resolve();
  }
  @PluginMethod public void stop(PluginCall call) { AlarmReceiver.stop(getContext()); call.resolve(); }
  @PluginMethod public void status(PluginCall call) {
    Context c = getContext(); JSObject r = new JSObject();
    AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
    NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
    PowerManager pm = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
    r.put("exact", Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms());
    r.put("fsi", Build.VERSION.SDK_INT < 34 || nm.canUseFullScreenIntent());
    r.put("battery", pm.isIgnoringBatteryOptimizations(c.getPackageName()));
    call.resolve(r);
  }
  void open(String action) {
    Intent i = new Intent(action).setData(Uri.parse("package:" + getContext().getPackageName())).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    try { getContext().startActivity(i); } catch (Exception e) {}
  }
  @PluginMethod public void openExact(PluginCall call) { if (Build.VERSION.SDK_INT >= 31) open(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM); call.resolve(); }
  @PluginMethod public void openFsi(PluginCall call) { if (Build.VERSION.SDK_INT >= 34) open(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT); call.resolve(); }
  @PluginMethod public void openBattery(PluginCall call) { open(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS); call.resolve(); }
}
