package com.kickstarter.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Pair
import android.webkit.WebView
import com.kickstarter.R
import com.kickstarter.databinding.UpdateLayoutBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.KSString
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.extensions.getProjectIntent
import com.kickstarter.libs.utils.extensions.isProjectUpdateCommentsUri
import com.kickstarter.libs.utils.extensions.isProjectUpdateUri
import com.kickstarter.libs.utils.extensions.isProjectUri
import com.kickstarter.models.Update
import com.kickstarter.services.RequestHandler
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.views.KSWebView
import com.kickstarter.viewmodels.UpdateViewModel
import okhttp3.Request

@RequiresActivityViewModel(UpdateViewModel.ViewModel::class)
class UpdateActivity : BaseActivity<UpdateViewModel.ViewModel?>(), KSWebView.Delegate {
    private lateinit var ksString: KSString
    private lateinit var binding: UpdateLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UpdateLayoutBinding.inflate(layoutInflater)

        setContentView(binding.root)
        ksString = environment().ksString()

        binding.updateWebView.setDelegate(this)
        binding.updateWebView.registerRequestHandlers(
            listOf(
                RequestHandler({ uri: Uri?, webEndpoint: String ->
                    (uri?.let { it } ?: Uri.EMPTY).isProjectUpdateUri(webEndpoint)
                }) { request: Request, _ -> handleProjectUpdateUriRequest(request) },
                RequestHandler({ uri: Uri?, webEndpoint: String ->
                    (uri?.let { it } ?: Uri.EMPTY).isProjectUpdateCommentsUri(
                        webEndpoint
                    )
                }) { request: Request, _ ->
                    handleProjectUpdateCommentsUriRequest(request)
                },
                RequestHandler({ uri: Uri?, webEndpoint: String ->
                    (uri?.let { it } ?: Uri.EMPTY).isProjectUri(webEndpoint)
                }) { request: Request, webView: WebView -> handleProjectUriRequest(request, webView) }
            )
        )

        // - this.viewModel instantiated in super.onCreate it will never be null at this point
        val viewModel = requireNotNull(this.viewModel)

        viewModel.outputs.openProjectExternally()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { projectUrl ->
                openProjectExternally(projectUrl)
            }

        viewModel.outputs.hasCommentsDeepLinks()
            .filter { it == true }
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                viewModel.inputs.goToCommentsActivity()
            }

        viewModel.outputs.deepLinkToThreadActivity()
            .filter { it.second == true }
            .map { it.first }
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                viewModel.inputs.goToCommentsActivityToDeepLinkThreadActivity(it)
            }

        viewModel.outputs.startRootCommentsActivity()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { update ->
                startRootCommentsActivity(update)
            }

        viewModel.outputs.startRootCommentsActivityToDeepLinkThreadActivity()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe {
                startRootCommentsActivityToDeepLinkThreadActivity(it)
            }

        viewModel.outputs.startProjectActivity()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { uriAndRefTag ->
                startProjectActivity(uriAndRefTag.first, uriAndRefTag.second, uriAndRefTag.third)
            }

        viewModel.outputs.startShareIntent()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { updateAndShareUrl ->
                startShareIntent(updateAndShareUrl)
            }

        viewModel.outputs.updateSequence()
            .compose(bindToLifecycle())
            .compose(observeForUI())
            .subscribe { updateSequence ->
                binding.updateActivityToolbar.updateToolbar.setTitle(ksString.format(resources.getString(R.string.social_update_number), "update_number", updateSequence))
            }

        binding.updateActivityToolbar.shareIconButton.setOnClickListener {
            viewModel.inputs.shareIconButtonClicked()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.updateWebView.setDelegate(null)
        this.viewModel = null
    }

    override fun onResume() {
        super.onResume()

        // - When pressing the url within this webview for seeing updates for a concrete project, this same activity is presented again.
        // we need to reload the webview with the updates url to refresh the UI
        this.viewModel?.let { vm ->
            vm.webViewUrl()
                .take(1)
                .compose(bindToLifecycle())
                .compose(observeForUI())
                .subscribe { url ->
                    url?.let {
                        binding.updateWebView.loadUrl(it)
                    }
                }
        }
    }

    private fun handleProjectUpdateCommentsUriRequest(request: Request): Boolean {
        this.viewModel?.inputs?.goToCommentsRequest(request)
        return true
    }

    private fun handleProjectUpdateUriRequest(request: Request): Boolean {
        this.viewModel?.inputs?.goToUpdateRequest(request)
        return false
    }

    private fun handleProjectUriRequest(request: Request, webView: WebView): Boolean {
        this.viewModel?.inputs?.goToProjectRequest(request)
        return true
    }

    private fun openProjectExternally(projectUrl: String) {
        ApplicationUtils.openUrlExternally(this, projectUrl)
    }

    private fun startRootCommentsActivity(update: Update) {
        val intent = Intent(this, CommentsActivity::class.java)
            .putExtra(IntentKey.UPDATE, update)
        startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    private fun startRootCommentsActivityToDeepLinkThreadActivity(data: Pair<String, Update>) {
        val intent = Intent(this, CommentsActivity::class.java)
            .putExtra(IntentKey.COMMENT, data.first)
            .putExtra(IntentKey.UPDATE, data.second)

        startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    private fun startProjectActivity(uri: Uri, refTag: RefTag, isProjectPageEnabled: Boolean) {
        val intent = Intent().getProjectIntent(this, isProjectPageEnabled)
            .setData(uri)
            .putExtra(IntentKey.REF_TAG, refTag)
        startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    private fun startShareIntent(updateAndShareUrl: Pair<Update, String>) {
        val update = updateAndShareUrl.first
        val shareUrl = updateAndShareUrl.second
        val shareMessage = (
            ksString.format(resources.getString(R.string.activity_project_update_update_count), "update_count", NumberUtils.format(update.sequence())) +
                ": " + update.title()
            )
        val intent = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, "$shareMessage $shareUrl")
        startActivity(Intent.createChooser(intent, getString(R.string.Share_update)))
    }

    override fun exitTransition(): Pair<Int, Int>? {
        return TransitionUtils.slideInFromLeft()
    }

    override fun externalLinkActivated(url: String) {
        this.viewModel?.inputs?.externalLinkActivated()
    }

    override fun pageIntercepted(url: String) {}
    override fun onReceivedError(url: String) {}
}
