package com.ksolutions.whatNeed.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ksolutions.whatNeed.R
import com.ksolutions.whatNeed.activities.BusinessInfo
import com.ksolutions.whatNeed.models.Product
import com.ksolutions.whatNeed.utils.Constants
import com.shopper.utils.GlideLoader
import kotlinx.android.synthetic.main.product_list_layout.view.*

open class MyProductListAdapter(
    private val context: Context,
    private var list: ArrayList<Product>

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
                R.layout.product_list_layout,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)
    {
        val model = list[position]

        if (holder is MyViewHolder)
        {
            GlideLoader(context).loadProductPicture(model.p_image, holder.itemView.iv_product_image)

            holder.itemView.tv_product_name.text = model.p_title
            holder.itemView.tv_product_price.text = "Rs: " + model.p_price
            holder.itemView.tv_product_business_name.text = model.p_business_name

            holder.itemView.tv_product_business_name.setOnClickListener {
                val intent = Intent(context, BusinessInfo::class.java)
                intent.putExtra(Constants.EXTRA_BUSINESS_ID, model.p_business_id)
                context.startActivity(intent)
            }

            /*holder.itemView.setOnClickListener {
                // Launch Product details screen.
                val intent = Intent(context, ProductDetailsActivity::class.java)
                intent.putExtra(Constants.EXTRA_PRODUCT_ID, model.product_id)
                context.startActivity(intent)
            }*/
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}