package foundation.icon.iconex.dialogs;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

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
    }

    public void setText(String text) {
        mInputText.setText(text);
    }

    public String getText() { return mInputText.getText(); }

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

    public void setSelection(int index) {
        mInputText.setSelection(index);
    }

    public void setOnTextChangedListener(OnTextChangedListener listener) {
        mInputText.setOnTextChangedListener(new TTextInputLayout.OnTextChanged() {
            @Override
            public void onChanged(@NotNull CharSequence s) {
                setConfirmEnable(listener.onChangeText(s.toString()));
            }
        });
    }

    public interface OnTextChangedListener {
        // confirm button enable;
        boolean onChangeText(String s);
    }

    @Override
    public void show() {
        super.show();
        mInputText.setFocus(true);
    }
}
