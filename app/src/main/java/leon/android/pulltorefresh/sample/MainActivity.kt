package leon.android.pulltorefresh.sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View

class MainActivity : AppCompatActivity(), View.OnClickListener {
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_ptr -> startActivityInternal(PullToRefreshActivity::class.java)
            R.id.btn_load_more -> startActivityInternal(LoadMoreActivity::class.java)
            R.id.btn_header_footer -> startActivityInternal(HeaderFooterActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.btn_header_footer)?.setOnClickListener(this)
        findViewById<View>(R.id.btn_load_more)?.setOnClickListener(this)
        findViewById<View>(R.id.btn_ptr)?.setOnClickListener(this)
    }

    private fun startActivityInternal(clazz: Class<out Activity>) {
        startActivity(Intent(this, clazz))
    }
}
