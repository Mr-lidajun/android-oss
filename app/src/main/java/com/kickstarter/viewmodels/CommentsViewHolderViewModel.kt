package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.models.OptimizelyFeature
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.takeWhen
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.extensions.userIsCreator
import com.kickstarter.models.Comment
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.models.extensions.assignAuthorBadge
import com.kickstarter.models.extensions.isCommentPendingReview
import com.kickstarter.models.extensions.isCurrentUserAuthor
import com.kickstarter.models.extensions.isReply
import com.kickstarter.services.mutations.PostCommentData
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.viewholders.CommentCardViewHolder
import com.kickstarter.ui.views.CommentCardBadge
import com.kickstarter.ui.views.CommentCardStatus
import org.joda.time.DateTime
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.concurrent.TimeUnit

interface CommentsViewHolderViewModel {
    interface Inputs {
        /** Call when the user clicks learn more about comment guidelines. */
        fun onCommentGuideLinesClicked()

        /** Call when the user clicks retry view to send message */
        fun onRetryViewClicked()

        /** Call when the user clicks reply to comment */
        fun onReplyButtonClicked()

        /** Call when the user clicks view replies to comment */
        fun onViewRepliesButtonClicked()

        /** Call when the user clicks flag comment */
        fun onFlagButtonClicked()

        /** Configure the view model with the [Comment]. */
        fun configureWith(commentCardData: CommentCardData)

        /** Show cancelled comment pledge */
        fun onShowCommentClicked()
    }

    interface Outputs {
        /** Emits the commentCardStatus */
        fun commentCardStatus(): Observable<CommentCardStatus>

        /** Emits the comment replies count. */
        fun commentRepliesCount(): Observable<Int>

        /** Emits the comment Author Name. */
        fun commentAuthorName(): Observable<String>

        /** Emits the comment Author avatar string url. */
        fun commentAuthorAvatarUrl(): Observable<String>

        /** Emits the comment Author avatar string url. */
        fun commentMessageBody(): Observable<String>

        /** Emits the comment post time */
        fun commentPostTime(): Observable<DateTime>

        /** Emits the visibility of the comment card action group */
        fun isReplyButtonVisible(): Observable<Boolean>

        /** Emits the current [Comment] when Comment GuideLines clicked.. */
        fun openCommentGuideLines(): Observable<Comment>

        /** Emits the current [Comment] when Retry clicked.. */
        fun retrySendComment(): Observable<Comment>

        /** Emits the current [Comment] when Reply clicked.. */
        fun replyToComment(): Observable<Comment>

        /** Emits the current [Comment] when flag clicked.. */
        fun flagComment(): Observable<Comment>

        /** Emits the current [Comment] when view replies clicked.. */
        fun viewCommentReplies(): Observable<Comment>

        /** Emits the current [OptimizelyFeature.Key.COMMENT_ENABLE_THREADS] status to the CommentCard UI*/
        fun isCommentEnableThreads(): Observable<Boolean>

        /** Emits if the comment is a reply to root comment */
        fun isCommentReply(): Observable<Void>

        /** Emits when the execution of the post mutation is successful, it will be used to update the main list state for this comment**/
        fun isSuccessfullyPosted(): Observable<Comment>

        /** Emits when the execution of the post mutation is error, it will be used to update the main list state for this comment**/
        fun isFailedToPost(): Observable<Comment>

        /** Emits the current [Comment] when show comment for canceled pledge. */
        fun showCanceledComment(): Observable<Comment>

        fun authorBadge(): Observable<CommentCardBadge>
    }

    class ViewModel(environment: Environment) :
        ActivityViewModel<CommentCardViewHolder>(environment), Inputs, Outputs {
        private val commentInput = PublishSubject.create<CommentCardData>()
        private val onCommentGuideLinesClicked = PublishSubject.create<Void>()
        private val onRetryViewClicked = PublishSubject.create<Void>()
        private val onReplyButtonClicked = PublishSubject.create<Void>()
        private val onFlagButtonClicked = PublishSubject.create<Void>()
        private val onViewCommentRepliesButtonClicked = PublishSubject.create<Void>()
        private val onShowCommentClicked = PublishSubject.create<Void>()

        private val commentCardStatus = BehaviorSubject.create<CommentCardStatus>()
        private val authorBadge = BehaviorSubject.create<CommentCardBadge>()
        private val isReplyButtonVisible = BehaviorSubject.create<Boolean>()
        private val commentAuthorName = BehaviorSubject.create<String>()
        private val commentAuthorAvatarUrl = BehaviorSubject.create<String>()
        private val commentMessageBody = BehaviorSubject.create<String>()
        private val commentRepliesCount = BehaviorSubject.create<Int>()
        private val commentPostTime = BehaviorSubject.create<DateTime>()
        private val openCommentGuideLines = PublishSubject.create<Comment>()
        private val retrySendComment = PublishSubject.create<Comment>()
        private val replyToComment = PublishSubject.create<Comment>()
        private val flagComment = PublishSubject.create<Comment>()
        private val viewCommentReplies = PublishSubject.create<Comment>()
        private val isCommentEnableThreads = PublishSubject.create<Boolean>()
        private val internalError = BehaviorSubject.create<Throwable>()
        private val postedSuccessfully = BehaviorSubject.create<Comment>()
        private val failedToPosted = BehaviorSubject.create<Comment>()
        private val showCanceledComment = PublishSubject.create<Comment>()

        private val isCommentReply = BehaviorSubject.create<Void>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val apolloClient = requireNotNull(environment.apolloClient())
        private val currentUser = requireNotNull(environment.currentUser())

        init {

            val comment = Observable.merge(this.commentInput.distinctUntilChanged().map { it.comment }, postedSuccessfully)
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }

            configureCommentCardWithComment(comment)

            val commentCardStatus = this.commentInput
                .compose(combineLatestPair(currentUser.observable()))
                .distinctUntilChanged()
                .filter { ObjectUtils.isNotNull(it) }
                .map {
                    val commentCardState = cardStatus(it.first, it.second, environment.optimizely()?.isFeatureEnabled(OptimizelyFeature.Key.ANDROID_COMMENT_MODERATION))
                    it.first.toBuilder().commentCardState(commentCardState?.commentCardStatus ?: 0)
                        .build()
                }
            handleStatus(commentCardStatus, environment)

            // - CommentData will hold the information for posting a new comment if needed
            val commentData = this.commentInput
                .distinctUntilChanged()
                .withLatestFrom(currentUser.loggedInUser()) { input, user -> Pair(input, user) }
                .filter { shouldCommentBePosted(it) }
                .map {
                    Pair(requireNotNull(it.first), requireNotNull(it.first.project))
                }

            postComment(commentData, internalError, environment)

            this.internalError
                .compose(combineLatestPair(commentData))
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .delay(1, TimeUnit.SECONDS, environment.scheduler())
                .subscribe {
                    this.commentCardStatus.onNext(CommentCardStatus.FAILED_TO_SEND_COMMENT)
                    this.failedToPosted.onNext(it.second.first.comment)
                }

            comment
                .withLatestFrom(currentUser.observable()) { comment, user -> Pair(comment, user) }
                .map { it.first.assignAuthorBadge(it.second) }
                .compose(bindToLifecycle())
                .subscribe { this.authorBadge.onNext(it) }
        }

        /**
         * Handles the configuration and behaviour for the comment card
         * @param comment the comment observable
         */
        private fun handleStatus(commentCardStatus: Observable<CommentCardData>, environment: Environment) {
            commentCardStatus
                .compose(combineLatestPair(currentUser.observable()))
                .compose(bindToLifecycle())
                .subscribe {
                    this.commentCardStatus.onNext(cardStatus(it.first, it.second, environment.optimizely()?.isFeatureEnabled(OptimizelyFeature.Key.ANDROID_COMMENT_MODERATION)))
                }

            commentCardStatus
                .compose(combineLatestPair(currentUser.observable()))
                .compose(bindToLifecycle())
                .subscribe {
                    this.isReplyButtonVisible.onNext(
                        shouldReplyButtonBeVisible(
                            it.first,
                            it.second
                        )
                    )
                }
        }

        /**
         * Handles the configuration and behaviour for the comment card
         * @param comment the comment observable
         */
        private fun configureCommentCardWithComment(comment: Observable<Comment>) {

            comment
                .filter { it.parentId() > 0 }
                .compose(bindToLifecycle())
                .subscribe {
                    this.isCommentReply.onNext(null)
                }

            comment
                .map { it.repliesCount() }
                .compose(combineLatestPair(this.isCommentEnableThreads))
                .compose(bindToLifecycle())
                .subscribe {
                    this.commentRepliesCount.onNext(it.first)
                }

            comment
                .map { it.author()?.name() }
                .filter { ObjectUtils.isNotNull(it) }
                .compose(bindToLifecycle())
                .subscribe(this.commentAuthorName)

            comment
                .map { it.author()?.avatar()?.medium() }
                .filter { ObjectUtils.isNotNull(it) }
                .compose(bindToLifecycle())
                .subscribe(this.commentAuthorAvatarUrl)

            comment
                .map { it.body() }
                .filter { ObjectUtils.isNotNull(it) }
                .compose(bindToLifecycle())
                .subscribe(this.commentMessageBody)

            comment
                .map { it.createdAt() }
                .filter { ObjectUtils.isNotNull(it) }
                .compose(bindToLifecycle())
                .subscribe(this.commentPostTime)

            comment
                .compose(takeWhen(this.onViewCommentRepliesButtonClicked))
                .compose(bindToLifecycle())
                .subscribe(this.viewCommentReplies)

            comment
                .compose(takeWhen(this.onCommentGuideLinesClicked))
                .compose(bindToLifecycle())
                .subscribe(this.openCommentGuideLines)

            comment
                .compose(takeWhen(this.onReplyButtonClicked))
                .compose(bindToLifecycle())
                .subscribe(this.replyToComment)

            comment
                .compose(takeWhen(this.onRetryViewClicked))
                .doOnNext {
                    this.commentCardStatus.onNext(CommentCardStatus.RE_TRYING_TO_POST)
                }
                .compose(bindToLifecycle())
                .subscribe {
                    this.retrySendComment.onNext(it)
                }

            comment
                .compose(takeWhen(this.onFlagButtonClicked))
                .compose(bindToLifecycle())
                .subscribe(this.flagComment)

            comment
                .compose(takeWhen(this.onShowCommentClicked))
                .compose(bindToLifecycle())
                .subscribe(this.showCanceledComment)
        }

        /**
         * Handles the logic for posting comments (new ones, and the retry attempts)
         * @param commentData will emmit only in case we need to post a new comment
         */
        private fun postComment(
            commentData: Observable<Pair<CommentCardData, Project>>,
            errorObservable: BehaviorSubject<Throwable>,
            environment: Environment
        ) {
            val postCommentData = commentData
                .map {
                    Pair(
                        requireNotNull(it.first.commentableId),
                        requireNotNull(it.first?.comment)
                    )
                }
                .map {
                    PostCommentData(
                        commentableId = it.first,
                        body = it.second.body(),
                        clientMutationId = null,
                        parent = it.second?.parentId()
                            ?.let { id -> it.second.toBuilder().id(id).build() }
                    )
                }
            postCommentData.map {
                executePostCommentMutation(it, errorObservable)
            }
                .switchMap {
                    it
                }
                .compose(bindToLifecycle())
                .subscribe {
                    this.commentCardStatus.onNext(CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS)
                    this.postedSuccessfully.onNext(it)
                    if (it.isReply()) this.isReplyButtonVisible.onNext(false)
                }

            Observable
                .combineLatest(onRetryViewClicked, postCommentData) { _, newData ->
                    return@combineLatest executePostCommentMutation(newData, errorObservable)
                }.switchMap {
                    it
                }.doOnNext {
                    this.commentCardStatus.onNext(CommentCardStatus.POSTING_COMMENT_COMPLETED_SUCCESSFULLY)
                }
                .delay(3000, TimeUnit.MILLISECONDS, environment.scheduler())
                .compose(bindToLifecycle())
                .subscribe {
                    this.commentCardStatus.onNext(CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS)
                    this.postedSuccessfully.onNext(it)
                    if (it.isReply()) this.isReplyButtonVisible.onNext(false)
                }
        }

        /**
         * In order to decide if a comment needs to be posted we need to check:
         * - The comment author is the current user
         * - the comment id is negative, this happens when a comment is created on the app, and has not been posted
         * to the backed yet, otherwise it should have a id bigger than 0
         * - The state we recognize as a comment that needs to be posted is: `TRYING_TO_POST`, no other state is allowed
         */
        private fun shouldCommentBePosted(dataCommentAndUser: Pair<CommentCardData, User>): Boolean {
            var shouldPost = false
            val currentUser = dataCommentAndUser.second
            val comment = dataCommentAndUser.first?.comment?.let { return@let it }
            val status = dataCommentAndUser.first.commentCardState

            comment?.let {
                shouldPost = it.id() < 0 && it.author() == currentUser
            }

            shouldPost = shouldPost && (status == CommentCardStatus.TRYING_TO_POST.commentCardStatus || status == CommentCardStatus.FAILED_TO_SEND_COMMENT.commentCardStatus)

            return shouldPost
        }

        /**
         * Function that will execute the PostCommentMutation
         * @param postCommentData holds the comment body and the commentableId for project or update to be posted
         * @return Observable<Comment>
         */
        private fun executePostCommentMutation(
            postCommentData: PostCommentData,
            errorObservable: BehaviorSubject<Throwable>
        ) =
            this.apolloClient.createComment(
                postCommentData
            ).doOnError {
                errorObservable.onNext(it)
            }
                .onErrorResumeNext(Observable.empty())

        /**
         * Checks if the current user is backing the current project,
         * or the current user is the creator of the project
         *  @param commentCardData
         *  @param featureFlagActive
         *  @param user
         *
         *  @return
         *  true -> if current user is backer and the feature flag is active and the comment is not a reply
         *  false -> any of the previous conditions fails
         */
        private fun shouldReplyButtonBeVisible(
            commentCardData: CommentCardData,
            user: User?
        ) =
            commentCardData.project?.let {
                (it.isBacking() || it.userIsCreator(user)) &&
                    (
                        commentCardData.commentCardState == CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS.commentCardStatus ||
                            commentCardData.commentCardState == CommentCardStatus.COMMENT_WITH_REPLIES.commentCardStatus
                        ) &&
                    (commentCardData.comment?.parentId() ?: -1) < 0
            } ?: false

        /**
         * Updates the status of the current comment card.
         * everytime the state changes.
         */
        private fun cardStatus(commentCardData: CommentCardData, currentUser: User?, isCommentModerationFeatureFlagEnabled: Boolean?) = when {
            commentCardData.comment?.isCommentPendingReview() ?: false && commentCardData.comment?.isCurrentUserAuthor(currentUser) == false && (isCommentModerationFeatureFlagEnabled ?: false) -> CommentCardStatus.FLAGGED_COMMENT
            commentCardData.comment?.deleted() ?: false -> CommentCardStatus.DELETED_COMMENT
            commentCardData.comment?.authorCanceledPledge() ?: false -> checkCanceledPledgeCommentStatus(commentCardData)
            (commentCardData.comment?.repliesCount() ?: 0 != 0) -> CommentCardStatus.COMMENT_WITH_REPLIES
            else -> CommentCardStatus.values().firstOrNull {
                it.commentCardStatus == commentCardData.commentCardState
            }
        }.also {
            this.isCommentEnableThreads.onNext(true)
        }

        private fun checkCanceledPledgeCommentStatus(commentCardData: CommentCardData): CommentCardStatus =
            if (commentCardData.commentCardState != CommentCardStatus.CANCELED_PLEDGE_COMMENT.commentCardStatus)
                CommentCardStatus.CANCELED_PLEDGE_MESSAGE
            else
                CommentCardStatus.CANCELED_PLEDGE_COMMENT

        override fun configureWith(commentCardData: CommentCardData) =
            this.commentInput.onNext(commentCardData)

        override fun onCommentGuideLinesClicked() = this.onCommentGuideLinesClicked.onNext(null)

        override fun onRetryViewClicked() = this.onRetryViewClicked.onNext(null)

        override fun onReplyButtonClicked() = this.onReplyButtonClicked.onNext(null)

        override fun onViewRepliesButtonClicked() =
            this.onViewCommentRepliesButtonClicked.onNext(null)

        override fun onFlagButtonClicked() = this.onFlagButtonClicked.onNext(null)

        override fun onShowCommentClicked() = this.onShowCommentClicked.onNext(null)

        override fun commentCardStatus(): Observable<CommentCardStatus> = this.commentCardStatus

        override fun commentRepliesCount(): Observable<Int> = this.commentRepliesCount

        override fun commentAuthorName(): Observable<String> = this.commentAuthorName

        override fun commentAuthorAvatarUrl(): Observable<String> = this.commentAuthorAvatarUrl

        override fun commentMessageBody(): Observable<String> = this.commentMessageBody

        override fun commentPostTime(): Observable<DateTime> = this.commentPostTime

        override fun isReplyButtonVisible(): Observable<Boolean> = this.isReplyButtonVisible

        override fun openCommentGuideLines(): Observable<Comment> = openCommentGuideLines

        override fun retrySendComment(): Observable<Comment> = retrySendComment

        override fun replyToComment(): Observable<Comment> = replyToComment

        override fun flagComment(): Observable<Comment> = flagComment

        override fun viewCommentReplies(): Observable<Comment> = this.viewCommentReplies

        override fun isCommentEnableThreads(): Observable<Boolean> = this.isCommentEnableThreads

        override fun isCommentReply(): Observable<Void> = this.isCommentReply

        override fun isSuccessfullyPosted(): Observable<Comment> = this.postedSuccessfully

        override fun isFailedToPost(): Observable<Comment> = this.failedToPosted

        override fun showCanceledComment(): Observable<Comment> = this.showCanceledComment

        override fun authorBadge(): Observable<CommentCardBadge> = this.authorBadge
    }
}
