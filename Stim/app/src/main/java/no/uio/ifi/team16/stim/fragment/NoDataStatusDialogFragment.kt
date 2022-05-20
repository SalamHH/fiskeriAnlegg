package no.uio.ifi.team16.stim.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import no.uio.ifi.team16.stim.R

/**
 * Dialogfragment som viser at dataen som lastes ikke enda er tilgjenglig
 */

class NoDataStatusDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return requireActivity().layoutInflater.inflate(
            R.layout.fragment_dialog_status_no_data,
            container
        )
    }
}
