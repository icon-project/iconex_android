package foundation.icon.iconex.view.ui.create

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import foundation.icon.iconex.R

class CreateStep4Fragment : Fragment() {
    val TAG = this@CreateStep4Fragment.javaClass.simpleName

    private var listener: OnCreateStep4Listener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_create_step4, container, false)
        return v
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnCreateStep4Listener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnCreateStep4Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnCreateStep4Listener {
    }

    companion object {
        @JvmStatic
        fun newInstance() = CreateStep4Fragment()
    }
}
