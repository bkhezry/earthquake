package com.github.bkhezry.earthquake.ui.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.bkhezry.earthquake.R
import com.github.bkhezry.earthquake.util.ElasticDragDismissFrameLayout

class EarthquakeDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_earthquake_detail)
        val dismissFrameLayout =
            findViewById<View>(R.id.draggable_frame) as ElasticDragDismissFrameLayout
        dismissFrameLayout.addListener(object :
            ElasticDragDismissFrameLayout.SystemChromeFader(this) {
            override fun onDragDismissed() {
                super.onDragDismissed()
                finishAfterTransition()
            }
        })
    }
}
