package foundation.icon.iconex.view.ui.create

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import foundation.icon.iconex.R

class CreateStep2Fragment : Fragment() {
    private var listener: OnCreateStep2Listener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_create_step2, container, false)
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnCreateStep2Listener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnCreateStep2Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnCreateStep2Listener {
    }

    companion object {
        @JvmStatic
        fun newInstance() = CreateStep2Fragment()
    }
}
