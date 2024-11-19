package im.angry.openeuicc.ui

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import im.angry.easyeuicc.R
import im.angry.openeuicc.util.*
import kotlinx.coroutines.launch

class CompatibilityCheckActivity: AppCompatActivity() {
    private lateinit var compatibilityCheckList: RecyclerView
    private val compatibilityChecks: List<CompatibilityCheck> by lazy { getCompatibilityChecks(this) }
    private val adapter = CompatibilityChecksAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compatibility_check)
        setSupportActionBar(requireViewById(im.angry.openeuicc.common.R.id.toolbar))
        setupToolbarInsets()
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        compatibilityCheckList = requireViewById(R.id.recycler_view)
        compatibilityCheckList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        compatibilityCheckList.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        compatibilityCheckList.adapter = adapter

        setupRootViewInsets(compatibilityCheckList)
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            compatibilityChecks.executeAll { adapter.notifyDataSetChanged() }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    inner class ViewHolder(private val root: View): RecyclerView.ViewHolder(root) {
        private val titleView: TextView = root.requireViewById(R.id.compatibility_check_title)
        private val descView: TextView = root.requireViewById(R.id.compatibility_check_desc)
        private val statusContainer: ViewGroup = root.requireViewById(R.id.compatibility_check_status_container)

        fun bindItem(item: CompatibilityCheck) {
            titleView.text = item.title
            descView.text = item.description

            statusContainer.children.forEach {
                it.visibility = View.GONE
            }

            val viewId = when (item.state) {
                CompatibilityCheck.State.SUCCESS -> R.id.compatibility_check_checkmark
                CompatibilityCheck.State.FAILURE -> R.id.compatibility_check_error
                CompatibilityCheck.State.FAILURE_UNKNOWN -> R.id.compatibility_check_unknown
                else -> R.id.compatibility_check_progress_bar
            }
            root.requireViewById<View>(viewId).visibility = View.VISIBLE
        }
    }

    inner class CompatibilityChecksAdapter: RecyclerView.Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(layoutInflater.inflate(R.layout.compatibility_check_item, parent, false))

        override fun getItemCount(): Int =
            compatibilityChecks.indexOfLast { it.state != CompatibilityCheck.State.NOT_STARTED } + 1

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bindItem(compatibilityChecks[position])
        }
    }
}