package foundation.icon.iconex.dev_dialogs;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;

import org.jetbrains.annotations.NotNull;

import foundation.icon.iconex.R;
import foundation.icon.iconex.widgets.TTextInputLayout;

public class EditText2Dialog extends MessageDialog{

    private TTextInputLayout mInputText = null;

    public EditText2Dialog(@NotNull Context context, String title) {
        super(context);

        setHeadText(title);

        buildDialog();
    }

    private void buildDialog() {

        // set Content
        View content = View.inflate(getContext(), R.layout.layout_edit_text2_dialog, null);
        setContent(content);

        // load content ui
        mInputText = content.findViewById(R.id.input_text);

        // set Button
        setSingleButton(false);

        // init content ui
    }

    public void setHint(String hint) {
        mInputText.setHint(hint);
    }
}
