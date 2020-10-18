package com.nicathaciyev.todoapp.fragments.list

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.nicathaciyev.todoapp.R
import com.nicathaciyev.todoapp.data.models.ToDoData
import com.nicathaciyev.todoapp.data.viewmodel.SharedViewModel
import com.nicathaciyev.todoapp.data.viewmodel.ToDoViewModel
import com.nicathaciyev.todoapp.databinding.FragmentListBinding
import com.nicathaciyev.todoapp.fragments.list.adapter.ListAdapter
import jp.wasabeef.recyclerview.animators.LandingAnimator


class ListFragment : Fragment(), SearchView.OnQueryTextListener {
    private val mToDoViewModel: ToDoViewModel by viewModels()
    private val mSharedViewModel: SharedViewModel by viewModels()
    private val adapter: ListAdapter by lazy { ListAdapter() }

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        // Data binding
        _binding = FragmentListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.mSharedViewModel = mSharedViewModel

        // Setup RecyclerView
        setupRecyclerView()

        mToDoViewModel.getAllData.observe(viewLifecycleOwner, { data ->
            mSharedViewModel.checkDatabaseEmpty(data)
            adapter.setData(data)
        })


        //Set menu
        setHasOptionsMenu(true)





        return binding.root
    }

    private fun setupRecyclerView() {
        val recyclerView = binding.recyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.itemAnimator = LandingAnimator().apply {
            addDuration = 300
        }

        // Swipe  to Delete
        swipeToDelete(recyclerView)

    }

    private fun swipeToDelete(recyclerView: RecyclerView) {

        val swipeToDeleteCallback = object : SwipeToDelete() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deletedItem = adapter.dataList[viewHolder.adapterPosition]

                // Delete Item
                mToDoViewModel.deleteItem(deletedItem)
                adapter.notifyItemRemoved(viewHolder.adapterPosition)

                // Restore Item
                restoreDeletedItem(viewHolder.itemView, deletedItem)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun restoreDeletedItem(view: View, deletedItem: ToDoData) {
        val snackBar = Snackbar.make(
            view,
            "Deleted '${deletedItem.title}'",
            Snackbar.LENGTH_SHORT
        )
        snackBar.setAction("UNDO") {
            mToDoViewModel.insertData(deletedItem)

        }
        snackBar.show()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_fragment_menu, menu)


        // SearchView logic
        val search = menu.findItem(R.id.menu_search)
        val searchView = search.actionView as? SearchView
        searchView?.isSubmitButtonEnabled = true
        searchView?.setOnQueryTextListener(this)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_deleteAll -> confirmItemRemoval()
            R.id.menu_high_priority -> mToDoViewModel.sortByHighPriority.observe(this, {
                adapter.setData(it)
            })
            R.id.menu_low_priority -> mToDoViewModel.sortByLowPriority.observe(this, {
                adapter.setData(it)
            })

        }

        return super.onOptionsItemSelected(item)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            searchThroughDatabase(query)
        }
        return true
    }


    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
            searchThroughDatabase(newText)
        }
        return true
    }

    private fun searchThroughDatabase(query: String) {
        val searchQuery = "%$query%"


        mToDoViewModel.searchDatabase(searchQuery).observe(this, { list ->
            list?.let {
                adapter.setData(it)
            }

        })

    }


    // Show AlertDialog to confirm  removal  of all item  from database table
    private fun confirmItemRemoval() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") { _, _ ->
            mToDoViewModel.deleteAll()
            Toast.makeText(
                requireContext(),
                "Successfully removed everything!", Toast.LENGTH_SHORT
            ).show()


        }
        builder.setNegativeButton("No") { _, _ -> }

        builder.setTitle("")
        builder.setTitle("Delete everything")
        builder.setMessage("Do you want to delete everything?")
        builder.create().show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}