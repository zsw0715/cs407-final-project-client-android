package com.cs407.knot_client_android.ui.map

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.cs407.knot_client_android.R

class AddPostView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val etTitle: EditText
    private val etContent: EditText
    private val btnSubmit: Button
    private val btnCancel: Button
    private val cardView: CardView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_add_post, this, true)
        etTitle = findViewById(R.id.etTitle)
        etContent = findViewById(R.id.etContent)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnCancel = findViewById(R.id.btnCancel)
        cardView = findViewById(R.id.cardView)

        // Set initial state as hidden
        visibility = GONE
        alpha = 0f
        cardView.scaleX = 0.8f
        cardView.scaleY = 0.8f

        setupClickListeners()
        setupInputBehavior()
    }

    private fun setupClickListeners() {
        btnSubmit.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val content = etContent.text.toString().trim()
            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(context, "Please enter title and content", Toast.LENGTH_SHORT).show()
            } else {
                hideKeyboard()
                // Notify that post is ready for marker placement
                onPostReadyListener?.invoke(title, content)
                hide()
            }
        }

        btnCancel.setOnClickListener {
            hideKeyboard()
            hide()
        }

        // Click background to hide
        setOnClickListener {
            hideKeyboard()
            hide()
        }

        // Prevent card clicks from triggering background click
        cardView.setOnClickListener {
            // Do nothing, just prevent event propagation
        }
    }

    private fun setupInputBehavior() {
        // Set single line behavior for title
        etTitle.maxLines = 1

        // Handle keyboard navigation
        etTitle.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_NEXT) {
                etContent.requestFocus()
                true
            } else {
                false
            }
        }

        etContent.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                btnSubmit.performClick() // Simulate post button click
                true
            } else {
                false
            }
        }
    }

    private fun hideKeyboard() {
        try {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(windowToken, 0)
        } catch (e: Exception) {
            // Keyboard might not be open, ignore error
        }
    }

    private var onPostReadyListener: ((String, String) -> Unit)? = null

    fun setOnPostReadyListener(block: (title: String, content: String) -> Unit) {
        onPostReadyListener = block
    }

    private var listener: ((String, String) -> Unit)? = null

    fun setOnSubmitListener(block: (title: String, content: String) -> Unit) {
        listener = block
    }

    fun show() {
        visibility = VISIBLE
        // Clear input fields
        etTitle.text.clear()
        etContent.text.clear()

        // Fade in and scale animation
        animate()
            .alpha(1f)
            .setDuration(300)
            .start()

        cardView.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(300)
            .start()

        // Request focus to title and show keyboard
        etTitle.requestFocus()
        showKeyboard()
    }

    private fun showKeyboard() {
        try {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(etTitle, InputMethodManager.SHOW_IMPLICIT)
        } catch (e: Exception) {
            // Ignore if keyboard cannot be shown
        }
    }

    fun hide() {
        // Fade out and scale animation
        animate()
            .alpha(0f)
            .setDuration(300)
            .start()

        cardView.animate()
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setDuration(300)
            .withEndAction {
                visibility = GONE
                hideKeyboard()
            }
            .start()
    }
}