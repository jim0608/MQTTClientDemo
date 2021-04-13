package com.jimz.mqtt.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.jimz.mqtt.R
import com.jimz.mqtt.adapter.NavigationDrawerAdapter
import com.jimz.mqtt.databinding.FragmentHomeBinding
import com.jimz.mqtt.ui.home.HomeViewModel
import java.util.ArrayList

class GalleryFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var dataBinding: FragmentHomeBinding
    private val data: MutableList<String> = ArrayList()
    private var adapter: NavigationDrawerAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        dataBinding = DataBindingUtil.bind(root)!!
        initViewModel()
        initList()
        initClick()
        return dataBinding.root
    }


    private fun initViewModel() {
        viewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        viewModel.text.observe(viewLifecycleOwner, {
            dataBinding.textHome.text = it
        })
        viewModel.mqttState.observe(viewLifecycleOwner) {
            data.add(it)
            adapter?.notifyItemInserted(data.size)
        }
    }

    private fun initList() {
        adapter = NavigationDrawerAdapter(context, data)
        dataBinding.recycler.adapter = adapter
        dataBinding.recycler.layoutManager = LinearLayoutManager(activity)
    }

    private fun initClick() {
        dataBinding.btnInitMqtt.setOnClickListener {
            viewModel.initMqtt(requireContext(),msg())
        }
        dataBinding.btnConnectMqtt.setOnClickListener {
            viewModel.connect(msg())
        }
        dataBinding.btnPublish.setOnClickListener { viewModel.publish(msg()) }
        dataBinding.btnSubscribe.setOnClickListener { viewModel.subscribe() }
    }

    private fun msg(): String = dataBinding.etMqttInput.text.toString()
}