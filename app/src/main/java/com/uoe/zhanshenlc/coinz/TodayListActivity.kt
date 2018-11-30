package com.uoe.zhanshenlc.coinz

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView

class TodayListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_today_list)

        val listView = findViewById<ListView>(R.id.listView_coinTodayList)
        listView.adapter = MyCustomAdapter(this)
    }

    private class MyCustomAdapter(context: Context): BaseAdapter() {
        private val mContext: Context = context

        override fun getCount(): Int {
            return 10
        }

        override fun getItemId(position: Int): Long {
            return 1
        }

        override fun getItem(position: Int): Any {
            return 2
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val layoutInflater = LayoutInflater.from(mContext)
            val rowMain = layoutInflater.inflate(R.layout.today_listview_item, parent, false)
            return rowMain
        }
    }
}
