package leon.android.pulltorefresh.sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View

class PullToRefreshActivity : AppCompatActivity(), View.OnClickListener {
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_list -> startActivityInternal(ListViewActivity::class.java)
            R.id.btn_recycler -> startActivityInternal(RecyclerPullToRefreshActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pull_to_refresh)
        findViewById<View>(R.id.btn_list).setOnClickListener(this)
        findViewById<View>(R.id.btn_recycler).setOnClickListener(this)
    }

    private fun startActivityInternal(clazz: Class<out Activity>) {
        startActivity(Intent(this, clazz))
    }
}
