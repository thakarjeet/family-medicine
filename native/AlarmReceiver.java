package com.thakar.familymedicine;
import android.app.*;
import android.content.*;
import android.media.*;
import android.os.*;
import androidx.core.app.NotificationCompat;
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
  @Override public void onReceive(Context c, Intent i) {
    int id = i.getIntExtra("id", 0); long at = i.getLongExtra("at", 0);
    String t = i.getStringExtra("title"), b = i.getStringExtra("body");
    boolean strict = i.getBooleanExtra("strict", true);
    if (i.getBooleanExtra("daily", false)) AlarmPlugin.set(c, id, at + 86400000L, t, b, true, strict);
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
