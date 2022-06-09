package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectEnvironmentalCommitmentFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.models.EnvironmentalCommitment
import com.kickstarter.viewmodels.projectpage.ProjectEnvironmentalCommitmentsViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test

class ProjectEnvironmentalCommitmentsViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: ProjectEnvironmentalCommitmentsViewModel

    private val projectEnvironmentalCommitment = TestSubscriber.create<List<EnvironmentalCommitment>>()
    private val openVisitOurEnvironmentalResourcesCenter = TestSubscriber.create<String>()
    private val compositeDisposable = CompositeDisposable()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = ProjectEnvironmentalCommitmentsViewModel(environment)

        compositeDisposable.add(this.vm.outputs.projectEnvironmentalCommitment().subscribe {
            projectEnvironmentalCommitment.onNext(it)
        })

        compositeDisposable.add(
            this.vm.outputs.openVisitOurEnvironmentalResourcesCenter().subscribe {
                openVisitOurEnvironmentalResourcesCenter.onNext(it)
            },
        )
    }

    @After
    fun cleanUp() {
        compositeDisposable.clear()
    }

    @Test
    fun testBindProjectEnvironmentalCommitmentList() {
        setUpEnvironment(environment())
        val environmentalCommitmentList = ProjectEnvironmentalCommitmentFactory.getEnvironmentalCommitments()

        this.vm.configureWith(ProjectDataFactory.project(ProjectFactory.project()))

        this.projectEnvironmentalCommitment.assertValue(environmentalCommitmentList)
    }

    @Test
    fun testOpenVisitOurEnvironmentalResourcesCenter() {
        setUpEnvironment(environment())

        this.vm.inputs.onVisitOurEnvironmentalResourcesCenterClicked()
        this.openVisitOurEnvironmentalResourcesCenter.assertValueCount(1)
    }
}
