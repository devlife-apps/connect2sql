package app.devlife.connect2sql.ui.connection.form

import android.content.Context
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.util.AttributeSet
import android.widget.EditText
import app.devlife.connect2sql.ui.widget.Toast
import app.devlife.connect2sql.util.ext.findSiblingById
import app.devlife.connect2sql.util.ext.stringValue
import com.gitlab.connect2sql.R

class ActionImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : android.support.v7.widget.AppCompatImageView(context, attrs, defStyleAttr) {

    private val action: Action?
    private val helpMessage: String?
    private val associatedViewId: Int?

    init {
        val styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.ActionImageView, 0, 0)
        try {
            action = styledAttrs
                .getInt(R.styleable.ActionImageView_type, -1)
                .let { Action.fromAttrValue(it) }
            helpMessage = styledAttrs.getString(R.styleable.ActionImageView_helpText)
            associatedViewId = styledAttrs
                .getResourceId(R.styleable.ActionImageView_associatedWith, 0)
                .let { if (it == 0) null else it }
        } finally {
            styledAttrs.recycle()
        }
    }

    override fun performLongClick(): Boolean {
        if (contentDescription?.isNotBlank() == true) {
            Toast.makeText(context, contentDescription, Toast.LENGTH_SHORT).show()
            return true
        }

        return super.performLongClick()
    }

    override fun performClick(): Boolean {
        return when {
            action == Action.KEYBOARD && associatedViewId != null -> {
                findSiblingById<EditText>(associatedViewId)?.also { editText ->
                    toggleAlphaToNumeric(editText, false, true)
                }
                true
            }
            action == Action.OBSCURE && associatedViewId != null -> {
                findSiblingById<EditText>(associatedViewId)?.also { editText ->
                    togglePasswordVisibility(editText)
                    editText.setSelection(editText.stringValue.length)
                }
                true
            }
            action == Action.HELP && helpMessage?.isNotBlank() == true -> {
                AlertDialog.Builder(context)
                    .setTitle(R.string.form_action_help)
                    .setMessage(helpMessage)
                    .setPositiveButton(R.string.help_positive_btn_label) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
                true
            }
            else -> super.performClick()
        }
    }

    private fun toggleAlphaToNumeric(editText: EditText, strict: Boolean, signed: Boolean) {
        val inputType = editText.inputType
        if (FormUtils.hasInputType(inputType, InputType.TYPE_CLASS_NUMBER)) {
            var inputType1 = FormUtils.addInputType(inputType, InputType.TYPE_CLASS_TEXT)
            inputType1 = FormUtils.removeInputType(inputType1, InputType.TYPE_CLASS_NUMBER)
            if (signed) {
                inputType1 = FormUtils.removeInputType(inputType1,
                    InputType.TYPE_NUMBER_FLAG_SIGNED)
            }
            editText.inputType = inputType1
        } else {
            var inputType2 = FormUtils.addInputType(inputType, InputType.TYPE_CLASS_NUMBER)
            if (strict) {
                inputType2 = FormUtils.removeInputType(inputType2, InputType.TYPE_CLASS_TEXT)
            }
            if (signed) {
                inputType2 = FormUtils.addInputType(inputType2, InputType.TYPE_NUMBER_FLAG_SIGNED)
            }
            editText.inputType = inputType2
        }
    }

    private fun togglePasswordVisibility(editText: EditText) {
        val inputType = editText.inputType
        if (FormUtils.hasInputType(inputType, InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            editText.inputType = FormUtils.removeInputType(inputType,
                InputType.TYPE_TEXT_VARIATION_PASSWORD)
        } else {
            editText.inputType = FormUtils.addInputType(inputType,
                InputType.TYPE_TEXT_VARIATION_PASSWORD)
        }
    }

    enum class Action(val attrValue: Int) {
        HELP(0),
        KEYBOARD(1),
        OBSCURE(2);

        companion object {

            fun fromAttrValue(attrValue: Int): Action {
                val actions = values()
                for (action in actions) {
                    if (action.attrValue == attrValue) {
                        return action
                    }
                }

                throw IllegalArgumentException("Action $attrValue not a valid action.")
            }
        }
    }
}