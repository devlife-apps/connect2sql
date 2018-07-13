package me.jromero.connect2sql.ui.connection.form;

import android.view.View;

/**
 * Created by javier.romero on 5/4/14.
 */
public interface Field {

    public void setOnActionClickListener(OnActionClickListener listener);

    public void setOnActionLongClickListener(OnActionLongClickListener listener);

    public static interface OnActionClickListener {
        public void onActionClick(Action action, View actionView, View inputView);
    }

    public static interface OnActionLongClickListener {
        public void onActionLongClick(Action action, View view, View inputView);
    }

    public static enum Action {
        KEYBOARD_INPUT("action_keyboard_input"),
        VISIBLE("action_visible"),
        HELP("action_help");

        private final String mTag;

        private Action(String tag) {
            mTag = tag;
        }

        public String getTag() {
            return mTag;
        }

        public static Action fromTag(String tag) {
            Action[] actions = values();
            for (int i = 0; i < actions.length; i++) {
                Action action = actions[i];
                if (action.getTag().equals(tag)) {
                    return action;
                }
            }

            throw new IllegalArgumentException("Tag " + tag + " not a valid action.");
        }
    }
}
