package edu.carole.rine.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import edu.carole.rine.R
import edu.carole.rine.data.sqlite.DBHelper
import edu.carole.rine.data.zero_tier.NetworkManager

class GalleryFragment : Fragment() {

    private lateinit var networkListView: ListView
    private lateinit var networkAdapter: NetworkAdapter
    private lateinit var networkManager: NetworkManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_network, container, false)

        networkListView = root.findViewById(R.id.network_list)
        val dbHelper = DBHelper(requireContext())
        networkManager = NetworkManager(dbHelper)
        val networks = networkManager.getNetworks()

        networkAdapter = NetworkAdapter(requireContext(), networks)
        networkListView.adapter = networkAdapter

        return root
    }
}