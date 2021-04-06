package ncl.csc3122.spendguru;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * Skeleton class for yes or no option popup window to extend elsewhere.
 */
public class PopUp {

    // Views
    private Button noButton;
    private Button yesButton;
    private TextView header;
    private PopupWindow popupWindow;

    // Primitive and Other Types
    private boolean result = false;
    private String headerText;
    private String yesText;
    private String noText;

    public PopUp() {}

    public PopUp(String headerText, String yesText, String noText) {
        this.headerText = headerText;
        this.yesText = yesText;
        this.noText = noText;
    }

    public PopupWindow getPopupWindow() {
        return popupWindow;
    }

    public boolean isResult() {
        return result;
    }

    public Button getNoButton() {
        return noButton;
    }

    public Button getYesButton() {
        return yesButton;
    }

    public TextView getHeader() {
        return header;
    }

    public void showPopupWindow(final View view) {
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window, null);

        int width = 600;
        int height = 600;
        popupWindow = new PopupWindow(popupView, width, height, true);

        //Set the location of the window on the screen
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        // Set XML components
        header = popupView.findViewById(R.id.confirmationText);
        yesButton = popupView.findViewById(R.id.yesButton);
        noButton = popupView.findViewById(R.id.noButton);

        setValues();

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setYesButtonListener();
            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNoButtonListener();
            }
        });

        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                setPopupWindowListener();
                return true;
            }
        });
    }

    private void setValues() {
        if (headerText != null) header.setText(headerText);
        if (yesText != null) yesButton.setText(yesText);
        if (noText != null) noButton.setText(noText);
    }

    public void setPopupWindowListener() {}

    public void setYesButtonListener() {}

    public void setNoButtonListener() {}

}