package com.silentquot.socialcomponents.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.silentquot.R;
import com.silentquot.socialcomponents.enums.ConnectionState;
import com.silentquot.socialcomponents.utils.LogUtil;

public class ConnectButton extends AppCompatButton {

    public static final String TAG = ConnectionState.class.getSimpleName();
    public static final int CONNECT_STATE = 1;
    public static final int REQUESTED_STATE = 2;
    public static final int ACCEPT_STATE = 3;
    public static final int CONNECTED_STATE=4;
    public static final int INVISIBLE_STATE = -1;

    private int state;
    public ConnectButton(Context context) {
        super(context);
        init();
    }
    public ConnectButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ConnectButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }



    private void init() {
        state = INVISIBLE_STATE;
        updateButtonState();
    }
    public void setState(ConnectionState connectionState) {
        switch (connectionState) {
            case I_SEND_CONNECT_REQUEST_TO_USER:
                state = REQUESTED_STATE;
                break;
            case CONNECTED_EACH_OTHER:
                state = CONNECTED_STATE;
                break;
            case USER_SEND_CONNECT_REQUEST_TO_ME:
                state = ACCEPT_STATE;
                break;
            case NO_ONE_CONNECTED:
                state = CONNECT_STATE;
                break;
                case MY_PROFILE:
                state = INVISIBLE_STATE;

        }

        updateButtonState();
        LogUtil.logDebug(TAG, "new state code: " + state);
    }



    public void updateButtonState() {
        setClickable(true);

        switch (state) {
            case CONNECT_STATE: {
                setVisibility(VISIBLE);
                setText(R.string.button_connect_state);
                setBackground(ContextCompat.getDrawable(getContext(), R.drawable.follow_button_dark_bg));
                setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                break;
            }

            case ACCEPT_STATE: {
                setVisibility(VISIBLE);
                setText(R.string.button_Accept_state);
                setBackground(ContextCompat.getDrawable(getContext(), R.drawable.follow_button_dark_bg));
                setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                break;
            }

            case CONNECTED_STATE: {
                setVisibility(VISIBLE);
                setText(R.string.button_connected_state);
                setBackground(ContextCompat.getDrawable(getContext(), R.drawable.follow_button_light_bg));
                setTextColor(ContextCompat.getColor(getContext(), R.color.primary_dark_text));
                break;
            }
            case REQUESTED_STATE: {
                setVisibility(VISIBLE);
                setText(R.string.button_requested_state);
                setBackground(ContextCompat.getDrawable(getContext(), R.drawable.follow_button_light_bg));
                setTextColor(ContextCompat.getColor(getContext(), R.color.primary_dark_text));
                break;
            }


            case INVISIBLE_STATE: {
                setVisibility(INVISIBLE);
                setClickable(false);
                break;
            }
        }
    }

    public int getState() {
        return state;
    }


}
