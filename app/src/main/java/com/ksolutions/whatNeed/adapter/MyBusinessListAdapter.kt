package com.ksolutions.whatNeed.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ksolutions.whatNeed.R
import com.ksolutions.whatNeed.activities.BusinessInfo
import com.ksolutions.whatNeed.models.BusinessModel
import com.ksolutions.whatNeed.utils.CompareDist
import com.ksolutions.whatNeed.utils.Constants
import com.ksolutions.whatNeed.utils.PublicValues
import com.shopper.utils.GlideLoader
import kotlinx.android.synthetic.main.business_list_layout.view.*
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin

open class MyBusinessListAdapter(
    private val context: Context,
    private var list: ArrayList<BusinessModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    /**
     * Inflates the item views which is designed in xml layout file
     *
     * create a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.business_list_layout,
                parent,
                false
            )
        )
    }

    /**
     * Binds each item in the ArrayList to a view
     *
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)
    {
        val model = list[position]

        if (holder is MyViewHolder)
        {
            val dist = distance(model.latitude, model.longitude, PublicValues.userLatitude, PublicValues.userLongitude)
            GlideLoader(context).loadProductPicture(model.image, holder.itemView.iv_business_item_image)

            holder.itemView.tv_business_item_title.text = model.title
            holder.itemView.tv_business_item_address.text = model.address
            holder.itemView.tv_business_item_distance.text = dist.toString() + " KM"


            // TODO Step 4: Assigning the click event to the delete button.
            // START
            /*holder.itemView.btn_business_detail_map.setOnClickListener {
                val intent = Intent(context, MapsActivity::class.java)
                context.startActivity(intent)
            }*/

            holder.itemView.setOnClickListener {
                val intent = Intent(context, BusinessInfo::class.java)
                // TODO Step 4: Pass the business id to the business details screen through intent.
                intent.putExtra(Constants.USER_ID, model.user_id)
                intent.putExtra(Constants.EXTRA_BUSINESS_ID, model.business_id)
                intent.putExtra(Constants.CATEGORY,model.category)
                context.startActivity(intent)
            }
        }
    }

    private fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val theta = lon1 - lon2
        var dis = (sin(CompareDist.deg2rad(lat1))
                * sin(CompareDist.deg2rad(lat2))
                + (cos(CompareDist.deg2rad(lat1))
                * cos(CompareDist.deg2rad(lat2))
                * cos(CompareDist.deg2rad(theta))))
        dis = acos(dis)
        dis = CompareDist.rad2deg(dis)
        dis *= 60 * 1.1515
        return round(dis.toFloat()*100)/100
    }

    private fun deg2rad(deg: Double): Double {
        return deg * Math.PI / 180.0
    }

    private fun rad2deg(rad: Double): Double {
        return rad * 180.0 / Math.PI
    }

    /**
     * Gets the number of items in the list
     */
    override fun getItemCount(): Int {
        return list.size
    }
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}