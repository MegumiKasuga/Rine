package edu.carole.rine.ui.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import edu.carole.rine.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_USER = "user"
private const val ARG_TEXT = "text"

/**
 * A simple [Fragment] subclass.
 * Use the [ChatBoxFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatBoxFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var user: String? = null
    private var text: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            user = it.getString(ARG_USER)
            text = it.getString(ARG_TEXT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val myView = inflater.inflate(R.layout.fragment_chat_box, container, false)
        val textBox = myView?.findViewById<TextView>(R.id.chat_text)
        textBox?.text = this.text
        return myView
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ChatBoxFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChatBoxFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USER, param1)
                    putString(ARG_TEXT, param2)
                }
            }
    }
}