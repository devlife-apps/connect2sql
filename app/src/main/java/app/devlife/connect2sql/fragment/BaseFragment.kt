package app.devlife.connect2sql.fragment

import android.support.v4.app.Fragment

open class BaseFragment : Fragment() {

    override fun onResume() {
        super.onResume()

        // prevent touches to interact with lower level fragment
        view?.setOnTouchListener { _, _ -> true }
    }
}
