package app.devlife.connect2sql.ui.query

import android.arch.lifecycle.Observer
import android.support.design.chip.ChipDrawable
import android.support.v4.app.FragmentActivity
import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_INCLUSIVE_EXCLUSIVE
import android.text.style.CharacterStyle
import android.text.style.ClickableSpan
import android.text.style.ImageSpan
import android.view.View
import app.devlife.connect2sql.sql.driver.agent.DriverAgent
import app.devlife.connect2sql.util.ext.appendMultiple
import app.devlife.connect2sql.viewmodel.ConnectionViewModel
import com.gitlab.connect2sql.R

class BreadcrumbsBinder(private val activity: FragmentActivity,
                        private val connectionViewModel: ConnectionViewModel) {

    private fun chipSpan(text: String, backgroundRes: Int = R.color.blueBase): CharacterStyle {
        val chip = ChipDrawable.createFromResource(activity, R.xml.query_breadcrumb_chip)
        chip.setChipBackgroundColorResource(backgroundRes)
        chip.setText(text)
        chip.setBounds(0, 0, chip.intrinsicWidth, chip.intrinsicHeight)
        return ImageSpan(chip)
    }

    private fun clickSpan(listener: () -> Unit): CharacterStyle = object : ClickableSpan() {
        override fun onClick(widget: View) {
            listener.invoke()
        }
    }

    private fun separatorSpan(): CharacterStyle = ImageSpan(activity,
        R.drawable.ic_breadcrumbs_separator,
        ImageSpan.ALIGN_BASELINE)

    var onBreadcrumbClicked: () -> Unit = {}

    var onBreadcrumbsGenerated: (CharSequence) -> Unit = {}
        set(value) {
            field = value
            generate(
                connectionViewModel.selectedDatabase.value,
                connectionViewModel.selectedTable.value)
        }

    init {
        connectionViewModel.selectedDatabase.observe(activity, Observer { database ->
            generate(database, null)
        })

        connectionViewModel.selectedTable.observe(activity, Observer { table ->
            generate(connectionViewModel.selectedDatabase.value, table)
        })
    }

    private fun generate(database: DriverAgent.Database?, table: DriverAgent.Table?) {
        val builder = SpannableStringBuilder()

        when {
            database == null ->
                builder.appendMultiple(" ",
                    listOf(
                        chipSpan(
                            activity.getString(R.string.query_no_database_selected),
                            R.color.greyDark),
                        clickSpan { onBreadcrumbClicked.invoke() }
                    ),
                    SPAN_INCLUSIVE_EXCLUSIVE)
            table == null -> {
                builder.append(" ",
                    chipSpan(database.name),
                    SPAN_INCLUSIVE_EXCLUSIVE)
            }
            else -> {
                builder.appendMultiple(" ",
                    listOf(
                        chipSpan(database.name),
                        clickSpan { onBreadcrumbClicked.invoke() }
                    ),
                    SPAN_INCLUSIVE_EXCLUSIVE)
                builder.append(" ",
                    separatorSpan(),
                    SPAN_INCLUSIVE_EXCLUSIVE)
                builder.appendMultiple(" ",
                    listOf(
                        chipSpan(table.name),
                        clickSpan { onBreadcrumbClicked.invoke() }
                    ),
                    SPAN_INCLUSIVE_EXCLUSIVE)
            }
        }

        onBreadcrumbsGenerated.invoke(builder)
    }
}