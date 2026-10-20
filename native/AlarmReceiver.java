package com.thakar.familymedicine;
import android.app.*;
import android.content.*;
import android.media.*;
import android.os.*;
import androidx.core.app.NotificationCompat;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
public class AlarmReceiver extends BroadcastReceiver {
  static MediaPlayer mp; static Vibrator vb;
  static void stopSound() {
    try { if (mp != null) { mp.stop(); mp.release(); } } catch (Exception e) {}
    mp = null;
    try { if (vb != null) vb.cancel(); } catch (Exception e) {}
  }
  public static void stop(Context c) {
    stopSound();
    ((NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
  }
  static boolean answered(JSONObject o) {
    try {
      URL u = new URL("https://firestore.googleapis.com/v1/projects/" + o.getString("pid") + "/databases/(default)/documents/family/" + o.getString("fam") + "?key=" + o.getString("key"));
      HttpURLConnection h = (HttpURLConnection) u.openConnection();
      h.setConnectTimeout(8000); h.setReadTimeout(8000);
      BufferedReader r = new BufferedReader(new InputStreamReader(h.getInputStream()));
      StringBuilder sb = new StringBuilder(); String ln;
      while ((ln = r.readLine()) != null) sb.append(ln);
      r.close();
      JSONObject S = new JSONObject(new JSONObject(sb.toString()).getJSONObject("fields").getJSONObject("j").getString("stringValue"));
      SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
      f.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
      String day = f.format(new Date(o.getLong("due")));
      String s = S.getJSONObject("logs").getJSONObject(day).getJSONObject("" + o.getInt("med")).getString("s");
      return !s.equals("reset");
    } catch (Exception e) { return false; }
  }
  @Override public void onReceive(final Context c, Intent i) {
    final JSONObject o;
    try { o = new JSONObject(i.getStringExtra("j")); } catch (Exception e) { return; }
    try {
      if (o.optBoolean("daily")) {
        JSONObject n = new JSONObject(o.toString());
        n.put("at", o.getLong("at") + 86400000L);
        if (n.has("due")) n.put("due", o.getLong("due") + 86400000L);
        AlarmPlugin.set(c, n);
      }
    } catch (Exception e) {}
    if ("watch".equals(o.optString("type"))) {
      final PendingResult pr = goAsync();
      new Thread(() -> { try { if (!answered(o)) fire(c, o); } finally { pr.finish(); } }).start();
      return;
    }
    fire(c, o);
  }
  static void fire(Context c, JSONObject o) {
    int id = o.optInt("id");
    String t = o.optString("title"), b = o.optString("body");
    boolean strict = o.optBoolean("strict", true);
    NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
    if (Build.VERSION.SDK_INT >= 26) {
      NotificationChannel ch = new NotificationChannel("med_alarm2", "Medicine alarm", NotificationManager.IMPORTANCE_HIGH);
      ch.setSound(null, null); ch.enableVibration(true); ch.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
      nm.createNotificationChannel(ch);
    }
    Intent a = new Intent(c, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    PendingIntent pi = PendingIntent.getActivity(c, id, a, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    Notification n = new NotificationCompat.Builder(c, "med_alarm2").setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
      .setContentTitle(t).setContentText(b).setPriority(NotificationCompat.PRIORITY_MAX)
      .setCategory(NotificationCompat.CATEGORY_ALARM).setFullScreenIntent(pi, true).setContentIntent(pi).setAutoCancel(true).build();
    nm.notify(id, n);
    try { c.startActivity(a); } catch (Exception e) {}
    try {
      stopSound();
      if (strict) {
        mp = new MediaPlayer();
        mp.setDataSource(c, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
        mp.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build());
        mp.setLooping(true); mp.prepare(); mp.start();
        vb = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
        vb.vibrate(new long[]{0, 800, 600}, 0);
        new Handler(Looper.getMainLooper()).postDelayed(() -> stopSound(), 90000);
      } else {
        RingtoneManager.getRingtone(c, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).play();
      }
    } catch (Exception e) {}
  }
}
