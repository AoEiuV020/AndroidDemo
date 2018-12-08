package cc.aoeiuv020.demo

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.util.Log
import android.view.View

import java.lang.ref.WeakReference

class MyBehavior(context: Context, attrs: AttributeSet) : CoordinatorLayout.Behavior<View>(context, attrs) {

    private var dependentView: WeakReference<View>? = null

    private fun getDependentView(): View {
        return dependentView!!.get()!!
    }

    override fun layoutDependsOn(parent: CoordinatorLayout?, child: View?, dependency: View?): Boolean {
        if (dependency != null && dependency.id == R.id.move) {
            dependentView = WeakReference(dependency)
            return true
        }
        return super.layoutDependsOn(parent, child, dependency)
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout?, child: View?, dependency: View?): Boolean {
        child!!.translationY = (dependency!!.height + dependency.translationY)
        return true
    }

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: View, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {

        return axes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
    }

    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: View, target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        val dependentView = getDependentView()
        Log.i("onLayoutChild", "onNestedPreScroll dy=" + dy + "TranslationY='" + dependentView.translationY)

        if (dy < 0) {
            return
        }
        val newTranslateY = dependentView.translationY - dy
        val minHeaderTranslate = (-dependentView.height).toFloat()
        if (newTranslateY >= minHeaderTranslate) {
            dependentView.translationY = newTranslateY
            consumed[1] = dy
        } else {
            if (dependentView.translationY >= -minHeaderTranslate) {
                consumed[1] = (dependentView.translationY - minHeaderTranslate).toInt()
            }
            dependentView.translationY = minHeaderTranslate

        }
    }


    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: View, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int) {
        val dependentView = getDependentView()
        val currentTranslationY = dependentView.translationY
        Log.i("onLayoutChild", "onNestedScroll dyUnconsumed=" + dyUnconsumed + "currentTranslationY=" + currentTranslationY)

        if (dyUnconsumed > 0) {
            return
        }
        val newTranslateY = currentTranslationY - dyUnconsumed
        val maxHeaderTranslate = 0f
        if (newTranslateY <= maxHeaderTranslate) {
            dependentView.translationY = newTranslateY
        } else {
            dependentView.translationY = maxHeaderTranslate
        }


    }
}
