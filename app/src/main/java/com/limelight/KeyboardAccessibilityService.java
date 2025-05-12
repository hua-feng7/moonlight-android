package com.limelight;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

/**
 * 键盘无障碍服务类
 * 用于拦截和处理键盘事件，特别是将返回键映射为ESC键，以及处理组合键
 *
 * @author hanbo && huafeng0
 * @since 2024/1/16
 */
public class KeyboardAccessibilityService extends AccessibilityService {
    private static final List BLACKLIST_KEYS = Arrays.asList(
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_POWER,
            KeyEvent.KEYCODE_HOME
    );

    private boolean isAltPressed = false;
    private boolean isCtrlPressed = false;
    private boolean isShiftPressed = false;
    private boolean isMetaPressed = false;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
    }

    @Override
    public void onInterrupt() {
    }

    private int escNum = 0;
    private long escClickTime = 0;

    @Override
    public boolean onKeyEvent(KeyEvent keyEvent) {
        int action = keyEvent.getAction();
        int keyCode = keyEvent.getKeyCode();
        if (action == KeyEvent.ACTION_DOWN) {
            Log.i("hh","KeyboardAccessibilityService press keyevent-->" + keyEvent);

            switch (keyCode) {
                case KeyEvent.KEYCODE_ALT_LEFT:
                case KeyEvent.KEYCODE_ALT_RIGHT:
                    isAltPressed = true;
                    break;
                case KeyEvent.KEYCODE_CTRL_LEFT:
                case KeyEvent.KEYCODE_CTRL_RIGHT:
                    isCtrlPressed = true;
                    break;
                case KeyEvent.KEYCODE_SHIFT_LEFT:
                case KeyEvent.KEYCODE_SHIFT_RIGHT:
                    isShiftPressed = true;
                    break;
                case KeyEvent.KEYCODE_META_LEFT:
                case KeyEvent.KEYCODE_META_RIGHT:
                    isMetaPressed = true;
                    break;
            }
        } else if (action == KeyEvent.ACTION_UP) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_ALT_LEFT:
                case KeyEvent.KEYCODE_ALT_RIGHT:
                    isAltPressed = false;
                    break;
                case KeyEvent.KEYCODE_CTRL_LEFT:
                case KeyEvent.KEYCODE_CTRL_RIGHT:
                    isCtrlPressed = false;
                    break;
                case KeyEvent.KEYCODE_SHIFT_LEFT:
                case KeyEvent.KEYCODE_SHIFT_RIGHT:
                    isShiftPressed = false;
                    break;
                case KeyEvent.KEYCODE_META_LEFT:
                case KeyEvent.KEYCODE_META_RIGHT:
                    isMetaPressed = false;
                    break;
            }
        }

        Game game = Game.instance;
        if (game != null && game.connected && !BLACKLIST_KEYS.contains(Integer.valueOf(keyCode))) {
            // 检测组合键情况
            boolean isModifierKey = keyCode == KeyEvent.KEYCODE_ALT_LEFT ||
                    keyCode == KeyEvent.KEYCODE_ALT_RIGHT ||
                    keyCode == KeyEvent.KEYCODE_CTRL_LEFT ||
                    keyCode == KeyEvent.KEYCODE_CTRL_RIGHT ||
                    keyCode == KeyEvent.KEYCODE_SHIFT_LEFT ||
                    keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT ||
                    keyCode == KeyEvent.KEYCODE_META_LEFT ||
                    keyCode == KeyEvent.KEYCODE_META_RIGHT;

            // 处理Alt+Tab组合键
            if (isAltPressed && keyCode == KeyEvent.KEYCODE_TAB) {
                if (action == KeyEvent.ACTION_DOWN) {
                    Game.instance.handleKeyDown(keyEvent);
                    return true; 
                } else if (action == KeyEvent.ACTION_UP) {
                    Game.instance.handleKeyUp(keyEvent);
                    return true; 
                }
            }

            // 处理其他可能的组合键
            // 如果当前有修饰键被按下，并且当前按键不是修饰键，则认为是组合键
            if ((isAltPressed || isCtrlPressed || isShiftPressed || isMetaPressed) && !isModifierKey) {
                if (action == KeyEvent.ACTION_DOWN) {
                    Game.instance.handleKeyDown(keyEvent);
                    return true; 
                } else if (action == KeyEvent.ACTION_UP) {
                    Game.instance.handleKeyUp(keyEvent);
                    return true; 
                }
            }

            // 处理返回键，将其转换为ESC键 此处解决小米平板esc问题
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                keyEvent = new KeyEvent(keyEvent.getDownTime(), 
                keyEvent.getEventTime(), 
                keyEvent.getAction(),
                KeyEvent.KEYCODE_ESCAPE, 
                keyEvent.getRepeatCount(), 
                keyEvent.getMetaState(), 
                keyEvent.getDeviceId(),
                keyEvent.getScanCode(), 
                keyEvent.getFlags(), 
                keyEvent.getSource());
            }

            // 处理普通按键
            if (action == KeyEvent.ACTION_DOWN) {
                Game.instance.handleKeyDown(keyEvent);
                return true;
            } else if (action == KeyEvent.ACTION_UP) {
                Game.instance.handleKeyUp(keyEvent);
                return true;
            }
        }
        return super.onKeyEvent(keyEvent);
    }

   /* private KeyEvent processBack(KeyEvent keyEvent) {
        // ... existing code ...
    }*/

   /* 已在xml配置@Override
    public void onServiceConnected() {
        // ... existing code ...
    }*/
}
