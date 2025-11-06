package com.cs407.knot_client_android.ui.map

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.cs407.knot_client_android.R

class PostMarkerDetailView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val userAvatarImageView: ImageView
    private val userNameTextView: TextView
    private val postTimeTextView: TextView
    private val postTitleTextView: TextView
    private val postContentTextView: TextView
    private val postImageView: ImageView
    private val replyEditText: EditText
    private val sendReplyButton: Button
    private val repliesContainerLayout: LinearLayout
    private val repliesTitleTextView: TextView
    private val cardView: CardView
    private val backgroundView: View

    private var isInInputMode = false

    init {
        LayoutInflater.from(context).inflate(R.layout.marker_post_detail, this, true)

        // Initialize view references
        userAvatarImageView = findViewById(R.id.ivUserAvatar)
        userNameTextView = findViewById(R.id.tvUserName)
        postTimeTextView = findViewById(R.id.tvPostTime)
        postTitleTextView = findViewById(R.id.tvPostTitle)
        postContentTextView = findViewById(R.id.tvPostContent)
        postImageView = findViewById(R.id.ivPostImage)
        replyEditText = findViewById(R.id.etReply)
        sendReplyButton = findViewById(R.id.btnSendReply)
        repliesContainerLayout = findViewById(R.id.llRepliesContainer)
        repliesTitleTextView = findViewById(R.id.tvRepliesTitle)
        cardView = findViewById(R.id.cardView)
        backgroundView = findViewById(R.id.backgroundView)

        setupClickListeners()
        setupFocusListeners()
    }

    private fun setupClickListeners() {
        sendReplyButton.setOnClickListener {
            val replyText = replyEditText.text.toString().trim()
            if (replyText.isEmpty()) {
                Toast.makeText(context, "Please enter a reply", Toast.LENGTH_SHORT).show()
            } else {
                // Add reply to message history
                addReply(replyText)
                // Notify listener
                onReplySentListener?.invoke(replyText)
                // Clear input field
                replyEditText.text.clear()
                hideKeyboard()
                isInInputMode = false
            }
        }

        // Handle background clicks to close the detail view
        backgroundView.setOnClickListener {
            if (isInInputMode) {
                // If in input mode, just hide keyboard
                clearFocusAndHideKeyboard()
            } else {
                // If not in input mode, close the detail view
                visibility = View.GONE
            }
        }

        // Prevent card clicks from closing the view
        cardView.setOnClickListener {
            // Do nothing - consume the click
        }
    }

    private fun setupFocusListeners() {
        replyEditText.setOnFocusChangeListener { _, hasFocus ->
            isInInputMode = hasFocus
        }
    }

    fun setPostData(title: String, content: String, replies: List<Pair<String, String>> = emptyList(), userName: String = "Current User") {
        postTitleTextView.text = title
        postContentTextView.text = content
        userNameTextView.text = userName
        postTimeTextView.text = "Just now"

        repliesContainerLayout.removeAllViews()
        replies.forEach { (user, reply) ->
            addReply(reply, user)
        }

        repliesTitleTextView.visibility = if (replies.isEmpty()) View.GONE else View.VISIBLE
    }

    fun addReply(reply: String, userName: String = "User") {
        val replyView = LayoutInflater.from(context).inflate(R.layout.reply_item, null)

        val replyUserTextView = replyView.findViewById<TextView>(R.id.tvReplyUser)
        val replyTextTextView = replyView.findViewById<TextView>(R.id.tvReplyText)

        replyUserTextView.text = userName
        replyTextTextView.text = reply

        replyView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 4.dpToPx(), 0, 4.dpToPx())
        }

        repliesContainerLayout.addView(replyView)

        repliesTitleTextView.visibility = View.VISIBLE
    }
    fun isInInputMode(): Boolean {
        return isInInputMode
    }

    private fun hideKeyboard() {
        try {
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(replyEditText.windowToken, 0)
        } catch (e: Exception) {
            // Ignore exception
        }
    }

    fun clearFocusAndHideKeyboard() {
        replyEditText.clearFocus()
        hideKeyboard()
        isInInputMode = false
    }

    private fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    private var onReplySentListener: ((String) -> Unit)? = null

    fun setOnReplySentListener(block: (reply: String) -> Unit) {
        onReplySentListener = block
    }
}