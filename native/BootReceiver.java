package com.thakar.familymedicine;
import android.content.*;
import org.json.*;
public class BootReceiver extends BroadcastReceiver {
  @Override public void onReceive(Context c, Intent i) {
    try {
      JSONArray a = new JSONArray(c.getSharedPreferences("med", 0).getString("alarms", "[]"));
      long now = System.currentTimeMillis();
      for (int k = 0; k < a.length(); k++) {
        JSONObject o = a.getJSONObject(k);
        long at = o.getLong("at");
        if (o.optBoolean("daily")) {
          while (at <= now) { at += 86400000L; if (o.has("due")) o.put("due", o.getLong("due") + 86400000L); }
          o.put("at", at);
          AlarmPlugin.set(c, o);
        } else if (at > now) AlarmPlugin.set(c, o);
      }
    } catch (Exception e) {}
  }
}
