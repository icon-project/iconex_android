package foundation.icon.iconex.view.ui.load

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import foundation.icon.iconex.R

class LoadBundleFragment : Fragment() {
    private var listener: OnLoadBundleListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_load_bundle, container, false)
        return v
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnLoadBundleListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnLoadBundleListener {
    }

    companion object {
        @JvmStatic
        fun newInstance(): LoadBundleFragment {
            return LoadBundleFragment()
        }
    }
}
