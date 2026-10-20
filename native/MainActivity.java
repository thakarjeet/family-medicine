package com.thakar.familymedicine;
import android.os.Bundle;
import com.getcapacitor.BridgeActivity;
public class MainActivity extends BridgeActivity {
  @Override public void onCreate(Bundle b) {
    registerPlugin(AlarmPlugin.class);
    super.onCreate(b);
    setShowWhenLocked(true);
    setTurnScreenOn(true);
  }
}
