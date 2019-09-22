package foundation.icon.iconex.dialogs;

import android.content.Context;
import android.view.View;

import org.jetbrains.annotations.NotNull;

import foundation.icon.iconex.R;
import foundation.icon.iconex.widgets.TTextInputLayout;
import kotlin.jvm.functions.Function1;

public class EditText2Dialog extends MessageDialog{

    private TTextInputLayout mInputText = null;

    public interface OnConfirmListener { boolean onConfirm(String text);}

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

    public void setText(String text) {
        mInputText.setText(text);
    }

    public void setHint(String hint) {
        mInputText.setHint(hint);
    }

    public void setError(String error) {
        mInputText.setError(error != null, error);
    }

    public void setOnConfirm(OnConfirmListener listner) {
        setOnConfirmClick(new Function1<View, Boolean>() {
            @Override
            public Boolean invoke(View view) {
                return listner.onConfirm(mInputText.getText());
            }
        });
    }
}
