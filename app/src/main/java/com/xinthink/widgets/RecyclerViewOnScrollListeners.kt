package com.xinthink.widgets

import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

/**
 * Base class that implements [RecyclerView.OnScrollListener], provides callback for endless list.
 *
 * @param willReachBottom callback when the bottom is approaching (invoked multiple times)
 */
abstract class RecyclerViewOnScrollListener(
    private val willReachBottom: (() -> Unit)? = null
) : RecyclerView.OnScrollListener() {

    private var prevFirstVisiblePosition = 0

    /** number of columns on a row */
    protected open val spanCount: Int = 1

    /** retrieve position of the first visible item */
    protected abstract fun findFirstVisibleItemPosition(): Int

    /** retrieve position of the last item which is fully visible */
    protected abstract fun findLastCompletelyVisibleItemPosition(): Int

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        val itemCount = recyclerView.adapter?.itemCount ?: 0
        if (itemCount <= 0) return

        val lastItemIndex = itemCount - 1
        val lastVisibleIndex = findLastCompletelyVisibleItemPosition()
        val firstVisiblePosition = findFirstVisibleItemPosition()

        if (lastItemIndex > 0 && lastItemIndex - lastVisibleIndex <= DISTANCE_REACHING_BOTTOM * spanCount) {
            Log.v(TAG, "approaching bottom: last=$lastItemIndex lastVisible=$lastVisibleIndex")
            willReachBottom?.invoke()
        }

        prevFirstVisiblePosition = firstVisiblePosition
    }

    companion object {
        const val TAG = "RVScroll"

        /** Distance (rows) approaching the bottom triggers 'reaching bottom' events */
        const val DISTANCE_REACHING_BOTTOM = 2
    }
}

/**
 * [LinearLayoutManager] based implementation of [RecyclerViewOnScrollListener].
 *
 * @param layoutManager [LinearLayoutManager]
 * @param willReachBottom see [RecyclerViewOnScrollListener]
 * @param callback see [RecyclerViewOnScrollListener]
 */
class LinearRecyclerOnScrollListener(
    private val layoutManager: LinearLayoutManager,
    willReachBottom: (() -> Unit)? = null
) : RecyclerViewOnScrollListener(willReachBottom) {

    override val spanCount = (layoutManager as? GridLayoutManager)?.spanCount ?: 1

    override fun findFirstVisibleItemPosition() = layoutManager.findFirstVisibleItemPosition()

    override fun findLastCompletelyVisibleItemPosition() = layoutManager.findLastCompletelyVisibleItemPosition()
}

/**
 * [StaggeredGridLayoutManager] based implementation of [RecyclerViewOnScrollListener].
 *
 * @param layoutManager [StaggeredGridLayoutManager]
 * @param willReachBottom see [RecyclerViewOnScrollListener]
 */
class StaggeredRecyclerOnScrollListener(
    private val layoutManager: StaggeredGridLayoutManager,
    willReachBottom: (() -> Unit)? = null
) : RecyclerViewOnScrollListener(willReachBottom) {

    override val spanCount = layoutManager.spanCount

    override fun findFirstVisibleItemPosition() = IntArray(spanCount).run {
        layoutManager.findFirstVisibleItemPositions(this)
        first()
    }

    override fun findLastCompletelyVisibleItemPosition() = IntArray(spanCount).run {
        layoutManager.findLastCompletelyVisibleItemPositions(this)
        last()
    }
}
