package com.skuvahn.utilities.progress

import android.app.Dialog
import com.skuvahn.R
import com.skuvahn.databinding.LoaderBinding

class ProgressLoader : ProgressBarDialog<LoaderBinding>(){

    override fun getLayoutResourceId(): Int {
        return R.layout.loader
    }

    override fun onDialogCreated(dialog: Dialog) {

    }
}