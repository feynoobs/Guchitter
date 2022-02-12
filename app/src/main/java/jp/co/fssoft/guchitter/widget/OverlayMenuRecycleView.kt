package jp.co.fssoft.guchitter.widget

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import jp.co.fssoft.guchitter.R

/**
 * Menu view holder
 *
 * @constructor
 *
 * @param view
 */
class OverlayMenuViewHolder(view: View) : RecyclerView.ViewHolder(view)
{
    companion object
    {
        /**
         *
         */
        private val TAG = OverlayMenuViewHolder::class.qualifiedName
    }

    /**
     * Image
     */
    public val image: ImageView = view.findViewById(R.id.overlay_recycler_view_image)

    /**
     * Text
     */
    public val text: TextView = view.findViewById(R.id.overlay_recycle_view_text)

    /**
     * Layout
     */
    public val layout: LinearLayout = view.findViewById(R.id.overlay_recycle_view_layout)

}

/**
 * Overlay menu recycle view
 *
 * @property callback
 * @constructor Create empty Overlay menu recycle view
 */
class OverlayMenuRecyclerView(private val callback: (Int)->Unit) : RecyclerView.Adapter<OverlayMenuViewHolder>()
{

    companion object
    {
        /**
         *
         */
        private val TAG = OverlayMenuRecyclerView::class.qualifiedName
    }

    /**
     * Overlay menu data
     *
     * @property id
     * @property resourceId
     * @property text
     * @constructor Create empty Overlay menu data
     */
    data class OverlayMenuData(val id: Int, val resourceId: Int, val text: String)

    public var overlayMenuObjects: List<OverlayMenuData> = mutableListOf()

    /**
     * On create view holder
     *
     * @param parent
     * @param viewType
     * @return
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OverlayMenuViewHolder
    {
        Log.d(TAG, "[START]onCreateViewHolder(${parent}, ${viewType})")
        return OverlayMenuViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.overlay_view_item, parent, false))
    }

    /**
     * On bind view holder
     *
     * @param holder
     * @param position
     */
    override fun onBindViewHolder(holder: OverlayMenuViewHolder, position: Int)
    {
        Log.d(TAG, "[START]onBindViewHolder(${holder}, ${position})")
        holder.image.setImageResource(overlayMenuObjects[position].resourceId)
        holder.text.text = overlayMenuObjects[position].text
        holder.layout.id = overlayMenuObjects[position].id
        holder.layout.setOnClickListener {
            callback(it.id)
        }
    }

    /**
     * Get item count
     *
     * @return
     */
    override fun getItemCount(): Int
    {
        Log.d(TAG, "[START]getItemCount()")
        return overlayMenuObjects.size
    }
}

