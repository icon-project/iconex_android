package foundation.icon.iconex.view.ui.load

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView

import java.util.ArrayList

import foundation.icon.iconex.R

class LoadSelectMethodFragment : Fragment() {

    private val btnKeystore: Button? = null
    private val btnPrivaeKey: Button? = null
    private val btnNext: Button? = null

    private var mListener: OnSelectMethodListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_load_select_method, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnSelectMethodListener) {
            mListener = context
        } else {
            throw RuntimeException("$context must implement OnSelectMethodListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface OnSelectMethodListener {
        fun onNext()
    }

    companion object {

        fun newInstance(): LoadSelectMethodFragment {
            return LoadSelectMethodFragment()
        }
    }
}
