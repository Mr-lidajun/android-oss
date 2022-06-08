package com.kickstarter.viewmodels.projectpage

import androidx.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.UrlUtils
import com.kickstarter.models.EnvironmentalCommitment
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.fragments.projectpage.ProjectEnvironmentalCommitmentsFragment
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

class ProjectEnvironmentalCommitmentsViewModel {
    interface Inputs {
        /** Configure with current [ProjectData]. */
        fun configureWith(projectData: ProjectData)
        fun onVisitOurEnvironmentalResourcesCenterClicked()
    }

    interface Outputs {
        /** Emits the current list [EnvironmentalCommitment]. */
        fun projectEnvironmentalCommitment(): Observable<List<EnvironmentalCommitment>>

        fun openVisitOurEnvironmentalResourcesCenter(): Observable<String>
    }

    class ViewModel(@NonNull val environment: Environment) :
        FragmentViewModel<ProjectEnvironmentalCommitmentsFragment>(environment),
        Inputs,
        Outputs {
        val inputs: Inputs = this
        val outputs: Outputs = this

        private val projectDataInput = BehaviorSubject.create<ProjectData>()
        private val onVisitOurEnvironmentalResourcesCenterClicked = BehaviorSubject.create<Any>()

        private val projectEnvironmentalCommitment = BehaviorSubject.create<List<EnvironmentalCommitment>>()
        private val openVisitOurEnvironmentalResourcesCenter = BehaviorSubject.create<String>()
        private val compositeDisposable = CompositeDisposable()

        init {
            val project = projectDataInput
                .map { it.project() }
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }

            compositeDisposable.add(project.map { project ->
                project.envCommitments()?.sortedBy { it.id }
            }.filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }
                .subscribe { this.projectEnvironmentalCommitment.onNext(it) })

            compositeDisposable.add(onVisitOurEnvironmentalResourcesCenterClicked
                .subscribe {
                    this.openVisitOurEnvironmentalResourcesCenter.onNext(
                        UrlUtils
                            .appendPath(
                                environment.webEndpoint(),
                                ENVIROMENT
                            )
                    )
                })
        }

        override fun onDetach() {
            super.onDetach()
            Timber.d(" ${this.javaClass} Cleaning up, the ViewModel has been detached with this amount of subscriptions: ${compositeDisposable.size()}")
            compositeDisposable.clear()
        }

        override fun configureWith(projectData: ProjectData) =
            this.projectDataInput.onNext(projectData)

        override fun onVisitOurEnvironmentalResourcesCenterClicked() =
            this.onVisitOurEnvironmentalResourcesCenterClicked.onNext(Any())

        @NonNull
        override fun projectEnvironmentalCommitment(): Observable<List<EnvironmentalCommitment>> = this.projectEnvironmentalCommitment
        @NonNull
        override fun openVisitOurEnvironmentalResourcesCenter(): Observable<String> = this.openVisitOurEnvironmentalResourcesCenter
    }

    companion object {
        const val ENVIROMENT = "environment"
    }
}
