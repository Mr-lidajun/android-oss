package com.kickstarter.ui.fragments.projectpage

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.KSApplication
import com.kickstarter.R
import com.kickstarter.databinding.FragmentProjectEnvironmentalCommitmentsBinding
import com.kickstarter.libs.Configure
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.adapters.EnvironmentalCommitmentsAdapter
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.extensions.makeLinks
import com.kickstarter.ui.extensions.parseHtmlTag
import com.kickstarter.viewmodels.projectpage.ProjectEnvironmentalCommitmentsViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class ProjectEnvironmentalCommitmentsFragment :
    Fragment(),
    Configure {

    private var binding: FragmentProjectEnvironmentalCommitmentsBinding? = null

    private var environmentalCommitmentsAdapter = EnvironmentalCommitmentsAdapter()

    private val compositeDisposable = CompositeDisposable()

    private lateinit var viewModel: ProjectEnvironmentalCommitmentsViewModel
    private lateinit var viewModelFactory: ProjectEnvironmentalCommitmentsViewModel.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val application = context?.applicationContext as KSApplication
        val environment = application.component().environment()
        viewModelFactory = ProjectEnvironmentalCommitmentsViewModel.Factory(environment)

        viewModel = ViewModelProvider(
            this,
            viewModelFactory
        ).get(
            ProjectEnvironmentalCommitmentsViewModel::class.java
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentProjectEnvironmentalCommitmentsBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupVisitOurEnvironmentalResourcesCenter()

        compositeDisposable.add(
            this.viewModel.outputs.projectEnvironmentalCommitment()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    environmentalCommitmentsAdapter.takeData(it)
                    Timber.d(" ${this.javaClass} :Subscription Fragment.onViewCreated in thread: ${Thread.currentThread()}")
                }
        )

        compositeDisposable.add(
            this.viewModel.outputs.openVisitOurEnvironmentalResourcesCenter()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    context?.let { context ->
                        ApplicationUtils.openUrlExternally(context, it)
                    }
                    Timber.d(" ${this.javaClass} :Subscription Fragment.onViewCreated in thread: ${Thread.currentThread()}")
                }
        )
    }

    private fun setupRecyclerView() {
        binding?.environmentalCommitmentsRecyclerView?.overScrollMode = View.OVER_SCROLL_NEVER
        binding?.environmentalCommitmentsRecyclerView?.adapter = environmentalCommitmentsAdapter
    }

    @SuppressLint("SetTextI18n")
    private fun setupVisitOurEnvironmentalResourcesCenter() {
        binding?.visitOurEnvironmentalResourcesCenterTv?.text =
            getString(R.string.Visit_our_Environmental_Resources_Center) + " " + getString(
            R.string
                .To_learn_how_Kickstarter_encourages_sustainable_practices
        )

        binding?.visitOurEnvironmentalResourcesCenterTv?.parseHtmlTag()
        binding?.visitOurEnvironmentalResourcesCenterTv?.makeLinks(
            Pair(
                getString(R.string.Visit_our_Environmental_Resources_Center),
                View.OnClickListener {
                    viewModel.inputs.onVisitOurEnvironmentalResourcesCenterClicked()
                }
            ),
            linkColor = R.color.kds_create_700,
            isUnderlineText = true
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Timber.d(" ${this.javaClass} Cleaning up, the FragmentView has been detached with this amount of subscriptions: ${compositeDisposable.size()}")
        compositeDisposable.clear()
    }

    override fun configureWith(projectData: ProjectData) {
        this.viewModel?.inputs?.configureWith(projectData)
    }

    companion object {
        @JvmStatic
        fun newInstance(position: Int): ProjectEnvironmentalCommitmentsFragment {
            val fragment = ProjectEnvironmentalCommitmentsFragment()
            val bundle = Bundle()
            bundle.putInt(ArgumentsKey.PROJECT_PAGER_POSITION, position)
            fragment.arguments = bundle
            return fragment
        }
    }
}
