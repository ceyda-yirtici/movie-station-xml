package com.example.movieproject.ui.cast

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.example.movieproject.R
import com.example.movieproject.databinding.FragmentCastBinding
import com.example.movieproject.model.CastPerson
import com.example.movieproject.model.MovieDetail
import com.example.movieproject.ui.MovieRecyclerAdapter
import com.example.movieproject.ui.moviedetail.DetailMovieFragment
import com.example.movieproject.utils.BundleKeys
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale





@AndroidEntryPoint
class CastFragment  : Fragment(R.layout.fragment_cast) {

    private lateinit var binding: FragmentCastBinding
    private val viewModel: CastViewModel by viewModels(ownerProducer = { this })
    private var movieRecyclerAdapter: MovieRecyclerAdapter = MovieRecyclerAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCastBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)

        val id : Int = requireArguments().getInt(BundleKeys.REQUEST_PERSON_ID)
        viewModel.displayMovie(id)
        viewModel.displayCast(id)
        listenViewModel()
    }

    private fun initView(view: View) {
        view.apply {
            val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            binding.recycler.layoutManager = layoutManager
            binding.recycler.adapter = movieRecyclerAdapter
            movieRecyclerAdapter.updateViewType(3)
        }
    }


    private fun listenViewModel() {

        viewModel.apply {
            liveDataCast.observe(viewLifecycleOwner) {
                updateCast(it)
            }
            liveDataLoading.observe(viewLifecycleOwner) {
                binding.loading.visibility = if (it) View.VISIBLE else View.GONE
                for (index in 0 until binding.content.childCount) {
                    val childView = binding.content.getChildAt(index)
                    childView.visibility = if (it) View.GONE else View.VISIBLE
                }
            }
            movieRecyclerAdapter.setOnClickListener(object : MovieRecyclerAdapter.OnClickListener{
                override fun onMovieClick(position: Int, movieView : View,  movieList: MutableList<MovieDetail>) {
                    movieClicked(position, movieView, movieList)
                }
                override fun onHeartButtonClick(
                    adapterPosition: Int,
                    movieView: View,
                    movieList: MutableList<MovieDetail>,
                    heartButton: ImageButton
                ) {
                    return
                }

            })
        }

        binding.toolbar.setNavigationOnClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
        }


    }


    private fun movieClicked(position: Int, movieView:View, movieList: MutableList<MovieDetail>) {
        val clickedMovie = movieList[position]
        val id = clickedMovie.id
        val bundle = Bundle().apply {
            putInt(BundleKeys.REQUEST_MOVIE_ID, id)
            putInt(BundleKeys.position, position)
            putInt(BundleKeys.ACTION_ID, 4)

        }
        val destinationFragment = DetailMovieFragment()
        destinationFragment.arguments = bundle
        findNavController().navigate(R.id.action_detail, bundle) // R.id.action_detail
    }

    @SuppressLint("SetTextI18n")
    private fun updateCast(it: CastPerson) {

        viewModel.liveDataMovieList.observe(viewLifecycleOwner) {
            movieRecyclerAdapter.updateMovieList(it)
            binding.movies.visibility =  if (it.size < 1) View.GONE else View.VISIBLE
        }

        viewModel.liveDataBackDropMovie.observe(viewLifecycleOwner) {
            val backdropURl = it.backdrop_path
            val backdrop = binding.backdropCastPhoto
            Glide.with(this@CastFragment).load(BundleKeys.baseImageUrlForOriginalSize + backdropURl).centerCrop()
                .placeholder(R.drawable.baseline_photo_220dp) // drawable as a placeholder
                .error(R.drawable.baseline_photo_220dp) //  drawable if an error occurs
                .into(backdrop)
        }



        val personalPhoto = binding.castPhoto
        Glide.with(this@CastFragment).load(BundleKeys.baseImageUrl + it.photo_path).circleCrop()
            .placeholder(R.drawable.baseline_person_24) // drawable as a placeholder
            .error(R.drawable.baseline_person_24) //  drawable if an error occurs
            .into(personalPhoto)


        binding.title.text = it.name
        binding.career.text = it.known_for_department
        if (!it.biography.isNullOrEmpty()) binding.biography.text = it.biography else binding.biographyText.visibility = View.GONE
        if (it.birthday != null)  binding.born.text = "BORN   " + it.birthday.let { it1 -> dateConversion(it1) } else binding.born.visibility = View.GONE
        if (it.place_of_birth != null)  binding.from.text =  "FROM   " + it.place_of_birth







    }


    private fun dateConversion(inputDate: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
        val outputFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)

        val date = LocalDate.parse(inputDate, inputFormatter)
        val outputDate = outputFormatter.format(date)


        return outputDate

    }


}



