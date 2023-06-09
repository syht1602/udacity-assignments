/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.bases.BaseAdapter
import com.example.android.trackmysleepquality.database.SleepDatabase
import com.example.android.trackmysleepquality.databinding.FragmentSleepTrackerBinding
import com.google.android.material.snackbar.Snackbar

/**
 * A fragment with buttons to record start and end times for sleep, which are saved in
 * a database. Cumulative data is displayed in a simple scrollable TextView.
 * (Because we have not learned about RecyclerView yet.)
 */
class SleepTrackerFragment : Fragment() {

    private lateinit var viewModel: SleepTrackerViewModel

    /**
     * Called when the Fragment is ready to display content to the screen.
     *
     * This function uses DataBindingUtil to inflate R.layout.fragment_sleep_quality.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {

        // Get a reference to the binding object and inflate the fragment views.
        val binding = FragmentSleepTrackerBinding.inflate(inflater, container, false)

        val application = requireNotNull(requireActivity().application)

        val sleepDatabase = SleepDatabase.getInstance(application).sleepDatabaseDao

        val viewModelFactory = SleepTrackerViewModelFactory(sleepDatabase, application)

        viewModel = ViewModelProvider(
            this, viewModelFactory
        )[SleepTrackerViewModel::class.java]

        binding.sleepTrackerViewModel = viewModel

        binding.lifecycleOwner = this

        val manager = GridLayoutManager(requireActivity(), 3)
        binding.sleepMyList.layoutManager = manager

        val adapter = SleepNightAdapter(SleepNightClickListener {
            Toast.makeText(requireContext(), "Id : $it", Toast.LENGTH_SHORT).show()
            viewModel.onSleepNightClicked(it)
        })

        val header: BaseAdapter.DataItem.Header = BaseAdapter.DataItem.Header("Header")
        adapter.setHeader(header)
        manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int) =  when (position) {
                0 -> 3
                else -> 1
            }
        }

        binding.sleepMyList.adapter = adapter

        with(viewModel) {
            navigateToQuality.observe(viewLifecycleOwner) {
                it?.let {
                    findNavController().navigate(
                        SleepTrackerFragmentDirections.actionSleepTrackerFragmentToSleepQualityFragment(
                            it.nightID
                        )
                    )
                    doneNavigating()
                }
            }

            nights.observe(viewLifecycleOwner) {
                it?.let {
                    adapter.addHeaderAndSubmitList(it)
                }
            }

            navigateToSleepDataQuality.observe(viewLifecycleOwner) {
                it?.let {
                    findNavController().navigate(
                        SleepTrackerFragmentDirections.actionSleepTrackerFragmentToSleepDetailFragment(
                            it
                        )
                    )
                    onSleepNavigated()
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewModel) {
            showSnakeBarEvent.observe(viewLifecycleOwner) {
                if (it) {
                    Snackbar.make(
//                        requireActivity().findViewById(android.R.id.content),
                        requireView(),
                        getString(R.string.cleared_message),
                        Snackbar.LENGTH_SHORT
                    ).show()
                    doneNavigating()
                }
            }
        }
    }
}
