package edu.carole.rine.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import androidx.fragment.app.Fragment
import edu.carole.rine.R
import edu.carole.rine.data.sqlite.DBHelper
import edu.carole.rine.data.zero_tier.NetworkManager

class GalleryFragment : Fragment(R.layout.fragment_network) {

    private lateinit var networkManager: NetworkManager
    private lateinit var listView: ListView
    private lateinit var addButton: Button
    private lateinit var adapter: NetworkAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dbHelper = DBHelper(requireContext())
        networkManager = NetworkManager(dbHelper)
        listView = view.findViewById(R.id.network_list)
        addButton = view.findViewById(R.id.add_network_button)
        
        adapter = NetworkAdapter(requireContext(), networkManager.getNetworks())
        listView.adapter = adapter

        addButton.setOnClickListener {
            networkManager.addRandomNetwork()
            adapter = NetworkAdapter(requireContext(), networkManager.getNetworks())
            listView.adapter = adapter
        }
    }
}